package com.willam.chatnotes

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SummaryWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    @Volatile private var client: LlmClient? = null
    override fun onStopped() { client?.cancel() }
    override fun doWork(): Result {
        val graph = AppGraph.get(applicationContext); val db = graph.db
        val id = inputData.getString("job_id") ?: return Result.failure()
        val started = System.currentTimeMillis()
        var callsThisRun = 0
        try {
            var job = db.job(id) ?: return Result.failure()
            if (job.state == "saved") return Result.success()
            val inputJson = JSONObject(job.snapshot)
            val snapshot = ConversationSnapshot.from(inputJson)
            db.updateJob(id, "running")
            val result = if (job.draft.isNotEmpty()) SummaryResult.parse(job.draft) else {
                val config = graph.config.read()
                require(inputJson.text("api_base", config.baseUrl) == config.baseUrl) {
                    "服务地址已更改，为避免把原文发送到未确认的地址，任务已暂停。请对该会话重新发起总结。"
                }
                val api = LlmClient(config.copy(model = inputJson.text("api_model", config.model))).also { client = it }
                // Retrieval-based category injection (stage 2): candidates come from
                // semantic top-k over notes when embedding is configured, keyword
                // top-k otherwise; full list only as last resort.
                val candidates = categoryCandidates(graph, snapshot)
                val input = snapshot.messages.joinToString("\n\n") {
                    "[消息 ${it.id}；${it.role}；状态 ${it.status}]\n${it.text}"
                }
                require(input.length <= 250000) { "会话超过本版自动整理上限（25 万字符）；原文已保存，可导出后分段整理" }
                fun call(key: String, content: String, merge: Boolean): SummaryResult {
                    db.part(id, key)?.let { return SummaryResult.parse(it) }
                    if (isStopped) throw Paused()
                    // Checkpoint before approaching WorkManager's normal execution time budget.
                    if (callsThisRun >= 3 || System.currentTimeMillis() - started > 210000) throw Paused()
                    callsThisRun++
                    val partial = api.summarize(content, merge, candidates)
                    db.savePart(id, key, partial.json())
                    return partial
                }
                if (input.length <= 24000) call("final", input, false) else {
                    var summaries = split(input, 12000).mapIndexed { i, part -> call("source:$i", part, false).markdown }
                    var round = 0
                    while (summaries.size > 1) {
                        // Pairing bounds each merge to <= 24000 characters and strictly reduces the count.
                        summaries = summaries.chunked(2).mapIndexed { i, pair ->
                            if (pair.size == 1) pair[0]
                            else call("merge:$round:$i", pair.joinToString("\n\n"), true).markdown
                        }
                        round++
                    }
                    call("final", summaries.single(), true)
                }
            }
            if (isStopped) throw Paused()
            // Persist the validated response BEFORE writing the note, so disk failures do not repeat API calls.
            db.updateJob(id, "writing", draft = result.json())
            val warningText = snapshot.warnings.joinToString("；").ifBlank { "未发现已知采集缺口；仍建议核对原文" }
            val source = "\n\n---\n\n## 来源与采集说明\n- 平台：ChatGPT\n- 会话 ID：${snapshot.id}\n" +
                "- 原始会话：${snapshot.url.ifBlank { "本地会话，请在“已保存对话”中查看" }}\n" +
                "- 本次快照：$id\n- 原文消息数：${snapshot.messages.size}\n- 采集说明：$warningText\n" +
                "- 本笔记由模型整理，未经过事实核实；原文保存在本应用的会话记录中。\n"
            val file = graph.notes.writeNote(result, id, "# ${result.title}\n\n${result.markdown}$source")
            db.updateJob(id, "saved", note = file.absolutePath)
            // 笔记有变更 → 触发增量同步（未配置同步时 SyncWorker 内部直接跳过）
            SyncWorker.enqueueAfterNoteChange(applicationContext)
            return Result.success()
        } catch (_: Paused) {
            db.updateJob(id, "queued", error = "已保存处理进度，等待继续")
            return Result.retry()
        } catch (e: Exception) {
            val transient = e is ApiFailure && e.retryable || e is IOException && e !is ApiFailure
            val retry = transient && runAttemptCount < 3 && !isStopped
            val message = when (e) {
                is ApiFailure -> e.message ?: "API 调用失败"
                is IOException -> "网络或文件写入失败，原文与已生成结果已保留"
                else -> e.message?.take(300) ?: "整理失败，原文已保留"
            }
            db.updateJob(id, if (retry) "queued" else "failed", error = message)
            return if (retry) Result.retry() else Result.failure()
        } finally { client = null }
    }
    private class Paused : RuntimeException()
    companion object {
        /**
         * Retrieval-based category candidates for the summarizer prompt.
         * Ranking: embedding top-k when configured (semantic=true, cosine score),
         * FTS keyword top-k otherwise; full list as last resort (score=0).
         * Each candidate carries up to [samples] existing note titles under it.
         */
        internal fun categoryCandidates(
            graph: AppGraph, snapshot: ConversationSnapshot,
            max: Int = 12, samples: Int = 5
        ): List<CategoryCandidate> {
            val queryText = snapshot.messages.filter { it.role == "user" }
                .joinToString(" ") { it.text }.take(500)
            val byCategory = runCatching {
                graph.search.ensureIndexed(graph.notes, graph.db)
                graph.search.titlesByCategory()
            }.getOrDefault(emptyMap())
            if (queryText.isBlank()) return fullList(graph, byCategory, max, samples)
            val picked = LinkedHashMap<String, CategoryCandidate>() // key -> candidate
            fun add(kind: String, pathKey: String, path: List<String>, score: Float, semantic: Boolean) {
                if (picked.containsKey(pathKey) || path.size !in 2..4) return
                val titles = byCategory[pathKey]?.take(samples) ?: emptyList()
                picked[pathKey] = CategoryCandidate(path, titles, score, semantic)
            }
            // 1) semantic top-k
            val embed = runCatching { graph.embedApi() }.getOrNull()
            if (embed != null) {
                runCatching {
                    val vec = embed.embed(listOf(queryText)).first()
                    val ranked = graph.search.vectorSearch(vec, embed.modelId)
                    for ((kind, id, score) in ranked) {
                        if (picked.size >= max) break
                        val doc = graph.search.rawQueryDocument(kind, id) ?: continue
                        if (doc.kind != "note") continue
                        val parts = doc.category.split('/').filter { it.isNotBlank() }
                        add(doc.kind, parts.joinToString("/"), parts, score, true)
                    }
                }
            }
            // 2) keyword fallback / supplement
            if (picked.size < max / 2) {
                runCatching {
                    val kwQuery = SearchLogic.terms(queryText).take(4).joinToString(" ")
                    for (hit in graph.search.query(kwQuery)) {
                        if (picked.size >= max) break
                        if (hit.kind != "note") continue
                        val parts = hit.category.split('/').filter { it.isNotBlank() }
                        add(hit.kind, parts.joinToString("/"), parts, 0f, false)
                    }
                }
            }
            if (picked.isEmpty()) return fullList(graph, byCategory, max, samples)
            return picked.values.toList()
        }
        private fun fullList(graph: AppGraph, byCategory: Map<String, List<String>>, max: Int, samples: Int): List<CategoryCandidate> =
            byCategory.entries.take(max).map { (key, titles) ->
                CategoryCandidate(key.split('/').filter { it.isNotBlank() }, titles.take(samples), 0f, false)
            }.ifEmpty {
                runCatching { graph.notes.categoryPaths() }.getOrDefault(emptyList()).take(max)
                    .map { CategoryCandidate(it, emptyList(), 0f, false) }
            }

        fun enqueue(context: Context, jobId: String) {
            val request = OneTimeWorkRequestBuilder<SummaryWorker>()
                .setInputData(Data.Builder().putString("job_id", jobId).build())
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
                .addTag("chatnotes-summary").build()
            WorkManager.getInstance(context).enqueueUniqueWork("summary-$jobId", ExistingWorkPolicy.KEEP, request)
        }
        internal fun split(text: String, limit: Int): List<String> {
            val result = mutableListOf<String>(); var start = 0
            while (start < text.length) {
                var end = (start + limit).coerceAtMost(text.length)
                if (end < text.length && Character.isHighSurrogate(text[end - 1])) end--
                result.add(text.substring(start, end)); start = end
            }
            return result
        }
    }
}
