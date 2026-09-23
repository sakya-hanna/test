package com.willam.chatnotes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.willam.chatnotes.shared.sync.DeleteMark
import com.willam.chatnotes.shared.sync.DocKind
import com.willam.chatnotes.shared.sync.SyncDoc
import com.willam.chatnotes.shared.sync.SyncLogic
import com.willam.chatnotes.shared.sync.SyncState
import com.willam.chatnotes.shared.sync.sha256Hex16
import kotlinx.serialization.json.Json
import java.io.File

/** 同步配置（SharedPreferences；token 走 ConfigStore 加密存储） */
data class SyncConfig(val baseUrl: String, val token: String)

/**
 * 同步任务（形态 A）：
 * 打开 app 或笔记变更后由 MainActivity/SummaryWorker 触发。
 * 流程 = push 本地新增/变更 → pull 服务器增量 → 应用写入与墓碑 → 保存光标。
 * 全程幂等，失败不打扰用户；服务器不可达时静默保留本地（下次触发重试）。
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val graph = AppGraph.get(applicationContext)
        val config = graph.config.syncConfig() ?: return Result.success() // 未配置同步：直接完成
        val engine = SyncEngine(config.baseUrl, config.token, deviceId(applicationContext))
        fun log(m: String) = android.util.Log.d("ChatNotes", "sync: $m")

        // 0) 配置对账：LLM/embed 设置在服务器存一份（LWW），换机/重装自动恢复
        reconcileConfig(engine, ::log)

        // 1) 全量对账：pull 服务器增量（其他设备写入 + 删除墓碑）
        var cursor = graph.config.syncCursor()
        var nPulled = 0
        var nDeleted = 0
        val writeFailures = mutableListOf<String>()
        while (true) {
            val (outcome, resp) = engine.pull(cursor)
            when (outcome) {
                is SyncOutcome.Unreachable -> { log("unreachable: ${outcome.cause}"); return Result.success() } // 静默：下次再同步
                is SyncOutcome.Rejected -> { log("rejected: ${outcome.detail}"); return Result.failure() }      // 配置问题：不重试
                is SyncOutcome.Ok -> {}
            }
            val body = resp!!
            val localHashes = localHashes(graph)
            val (w, d) = SyncLogic.applyPull(body.docs, localHashes)
            nPulled += body.docs.size
            // 先应用写入；有任何失败则光标停在变更前，下次重拉（拉取与写入均幂等）
            val failures = applyWrites(graph, w)
            writeFailures += failures
            for (t in d) if (deleteLocalNote(graph, t.docId)) nDeleted++
            cursor = if (failures.isEmpty()) body.cursor else cursor
            log("pull batch: docs=${body.docs.size} writes=${w.size} fails=${failures.size} cursor=$cursor")
            if (!body.hasMore) break
        }
        if (writeFailures.isNotEmpty()) log("WARN write failures: $writeFailures")
        log("pull done: nPulled=$nPulled nDeleted=$nDeleted cursor=$cursor")

        // 2) push：本地新增/有变化的笔记
        val synced = graph.config.syncState()
        val locals = localDocs(graph)
        val toPush = SyncLogic.planPush(locals, synced.hashes)
        val docs = toPush.map { d ->
            SyncDoc(
                kind = DocKind.note, docId = d.docId, category = d.category,
                title = d.title, content = d.content, updatedAt = System.currentTimeMillis(),
                deviceId = engine.deviceId,
                // 线上校验仍是纯内容 hash（服务器 bad_hash）；memo 指纹只用于本地脏检查
                contentHash = sha256Hex16(d.content),
            )
        }
        val pushOutcome = engine.push(docs)
        log("push: n=${docs.size} -> $pushOutcome")

        // 3) 广播本地删除（pull 阶段没覆盖的：本地刚删、服务器还不知道）
        val delIds = SyncLogic.planDeletes(locals.map { it.docId }.toSet(), synced.hashes.keys)
        val delOutcome = engine.delete(delIds.map { DeleteMark(DocKind.note, it, System.currentTimeMillis(), engine.deviceId) })

        // 4) 合并状态：push/delete 都被服务器确认后，才把这些 ID 记为已同步
        var state = synced.copy(cursor = cursor)
        if (pushOutcome is SyncOutcome.Ok) {
            state = state.copy(hashes = state.hashes + toPush.associate { d -> d.docId to d.hash })
        }
        if (delOutcome is SyncOutcome.Ok) {
            state = state.copy(hashes = state.hashes - delIds.toSet())
        }
        graph.config.saveSyncState(state)
        if (pushOutcome is SyncOutcome.Ok && delOutcome is SyncOutcome.Ok && writeFailures.isEmpty()) {
            applicationContext.getSharedPreferences("config", Context.MODE_PRIVATE)
                .edit().putLong("sync_last_ok", System.currentTimeMillis()).apply()
        }
        return Result.success()
    }

    /**
     * 配置对账（LWW）：本地版本较新 → push 服务器（服务器拒旧时以其返回值回填本地）；
     * 服务器较新 → 应用到本地（半段合并：服务器空字段不动本地对应值）。
     * 本机从未编辑过（updatedAt=0）视为纯恢复端，即使字段全空也不 push 覆盖服务器。
     * 服务器不可达时静默跳过，下次同步再对账。
     */
    private fun reconcileConfig(engine: SyncEngine, log: (String) -> Unit) {
        val cfg = AppGraph.get(applicationContext).config
        val local = cfg.exportConfig()
        val (outcome, resp) = engine.fetchConfig()
        when (outcome) {
            is SyncOutcome.Unreachable -> { log("config: unreachable ${outcome.cause}"); return }
            is SyncOutcome.Rejected -> { log("config: rejected ${outcome.detail}"); return }
            is SyncOutcome.Ok -> {}
        }
        val server = resp!!.config
        val serverEmpty = server.updatedAt == 0L
        when {
            // 播种：服务器从没存过配置而本地已有 → 首次同步即建立备份（老安装 cfg_updated_at=0 也能上）
            serverEmpty && (local.llmBaseUrl.isNotBlank() || local.llmModel.isNotBlank() || local.llmApiKey.isNotBlank() ||
                local.embedBaseUrl.isNotBlank() || local.embedModel.isNotBlank() || local.embedApiKey.isNotBlank()) -> {
                val seeded = local.copy(updatedAt = maxOf(local.updatedAt, System.currentTimeMillis()))
                val (_, saveResp) = engine.saveConfig(seeded)
                val effective = saveResp?.config ?: return
                if (effective.updatedAt > cfg.configUpdatedAt()) cfg.applyServerConfig(effective)
                log("config: seeded server to v${effective.updatedAt}")
            }
            local.updatedAt > server.updatedAt -> {
                if (local.llmBaseUrl.isBlank() && local.llmModel.isBlank() && local.llmApiKey.isBlank() &&
                    local.embedBaseUrl.isBlank() && local.embedModel.isBlank() && local.embedApiKey.isBlank()) {
                    log("config: local newer but empty, skip push"); return
                }
                val (_, saveResp) = engine.saveConfig(local)
                val effective = saveResp?.config ?: return
                if (effective.updatedAt > cfg.configUpdatedAt()) cfg.applyServerConfig(effective)
                log("config: pushed local v${local.updatedAt}, server now v${effective.updatedAt}")
            }
            server.updatedAt > cfg.configUpdatedAt() -> {
                cfg.applyServerConfig(server)
                log("config: applied server v${server.updatedAt}")
            }
            else -> log("config: in sync (v${server.updatedAt})")
        }
    }

    // ---- 本地文件系统视图 ----

    private fun localDocs(graph: AppGraph): List<SyncLogic.LocalDoc> =
        graph.notes.allFiles().mapNotNull { f ->
            val id = noteId(f) ?: return@mapNotNull null
            val content = runCatching { graph.notes.readNote(f) }.getOrNull() ?: return@mapNotNull null
            SyncLogic.LocalDoc(
                docId = id, content = content,
                category = f.parentFile?.let { p -> runCatching { p.relativeTo(graph.notes.root).path }.getOrNull() }.orEmpty(),
                title = f.nameWithoutExtension.replace(Regex("--[a-f0-9]{64}$"), ""),
            )
        }

    private fun localHashes(graph: AppGraph): Map<String, String> =
        localDocs(graph).associate { it.docId to it.hash }

    private fun noteId(f: File): String? {
        val m = Regex("--([a-f0-9]{64})\\.md$").find(f.name) ?: return null
        return m.groupValues[1]
    }

    private fun deleteLocalNote(graph: AppGraph, docId: String): Boolean =
        graph.notes.allFiles().firstOrNull { noteId(it) == docId }?.let { it.delete() } ?: false

    /** 写入远端下发的笔记；返回失败的 docId（调用方据此不推进光标，下次重拉） */
    private fun applyWrites(graph: AppGraph, writes: List<SyncDoc>): List<String> {
        val failed = mutableListOf<String>()
        val files = com.willam.chatnotes.NoteFiles(File(graph.app.filesDir, "notes"))
        for (d in writes) {
            val cats = if (d.category.isBlank()) emptyList() else d.category.split('/', '\\').filter { it.isNotBlank() }
            try {
                // 远端改名/移动：同 ID 旧文件（旧标题/旧目录）先移除，否则 write 的拒覆盖语义会留下两份
                graph.notes.allFiles().firstOrNull { noteId(it) == d.docId }?.delete()
                files.write(cats, d.title, d.docId, d.content)
            } catch (e: Exception) {
                android.util.Log.w("ChatNotes", "sync: write failed ${d.docId}: ${e.message}")
                failed.add(d.docId)
            }
        }
        return failed
    }

    private fun deviceId(context: Context): String {
        val prefs = context.getSharedPreferences("sync", Context.MODE_PRIVATE)
        prefs.getString("device_id", null)?.let { return it }
        val id = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
        prefs.edit().putString("device_id", id).apply()
        return id
    }

    companion object {
        /** 打开 app 时全量对账 */
        fun enqueueOnAppOpen(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync-open", ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<SyncWorker>().build(),
            )
        }

        /** 笔记整理完成后增量同步 */
        fun enqueueAfterNoteChange(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync-change", ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>().build(),
            )
        }
    }
}
