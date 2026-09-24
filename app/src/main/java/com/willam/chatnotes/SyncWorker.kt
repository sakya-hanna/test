package com.willam.chatnotes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.willam.chatnotes.shared.sync.DeleteMark
import com.willam.chatnotes.shared.sync.DocKind
import com.willam.chatnotes.shared.sync.SyncDoc
import com.willam.chatnotes.shared.sync.SyncLogic
import com.willam.chatnotes.shared.sync.sha256Hex16
import java.io.File
import java.util.concurrent.TimeUnit

/** 同步配置（SharedPreferences；token 走 ConfigStore 加密存储） */
data class SyncConfig(val baseUrl: String, val token: String)

/**
 * 同步任务（形态 A）：
 * 打开 app 或笔记变更后由 MainActivity/SummaryWorker 触发。
 * Compare every pulled version with the last acknowledged version. Preserve
 * concurrent edits as separate conflict notes before changing an existing note.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val graph = AppGraph.get(applicationContext)
        val config = graph.config.syncConfig() ?: return Result.success() // 未配置同步：直接完成
        val engine = SyncEngine(applicationContext, config.baseUrl, config.token, deviceId(applicationContext))
        fun log(m: String) = android.util.Log.d("ChatNotes", "sync: $m")
        when (val handshake = engine.hello("0.2.1")) {
            is SyncOutcome.Ok -> Unit
            is SyncOutcome.Unreachable -> { log("handshake unreachable: ${handshake.cause}"); return Result.retry() }
            is SyncOutcome.Rejected -> { log("handshake rejected: ${handshake.detail}"); return Result.failure() }
        }

        // 0) 配置对账：LLM/embed 设置在服务器存一份（LWW），换机/重装自动恢复
        reconcileConfig(engine, ::log)

        // Capture pending deletions before pulling. A delayed echo of our own
        // push must never recreate a note the user just moved to the trash.
        var state = graph.config.syncState()
        var cursor = state.cursor
        val pendingDeletes = SyncLogic.planDeletes(localDocs(graph).map { it.docId }.toSet(), state.hashes.keys).toSet()
        val prefs = applicationContext.getSharedPreferences("config", Context.MODE_PRIVATE)
        val pendingRestores = prefs.getStringSet("pending_restores", emptySet()).orEmpty().toSet()
        var nPulled = 0
        var nDeleted = 0
        while (true) {
            val (outcome, resp) = engine.pull(cursor)
            when (outcome) {
                is SyncOutcome.Unreachable -> { log("unreachable: ${outcome.cause}"); return Result.retry() }
                is SyncOutcome.Rejected -> { log("rejected: ${outcome.detail}"); return Result.failure() }      // 配置问题：不重试
                is SyncOutcome.Ok -> {}
            }
            val body = resp!!
            nPulled += body.docs.size
            val hashes = state.hashes.toMutableMap()
            val localById = localDocs(graph).associateBy { it.docId }.toMutableMap()
            try {
                for (d in body.docs) {
                    if (d.kind != DocKind.note) continue
                    val local = localById[d.docId]
                    val remoteHash = if (d.content.isEmpty() && d.title.isEmpty()) null
                        else SyncLogic.memoHash(d.category, d.title, d.content)
                    val action = SyncLogic.pullAction(local?.hash, hashes[d.docId], remoteHash,
                        d.docId in pendingDeletes, d.docId in pendingRestores)
                    when (action) {
                        SyncLogic.PullAction.WRITE_REMOTE -> {
                            applyRemote(graph, d); hashes[d.docId] = remoteHash!!
                            localById[d.docId] = SyncLogic.LocalDoc(d.docId, d.category, d.title, d.content)
                        }
                        SyncLogic.PullAction.DELETE_LOCAL -> {
                            if (deleteLocalNote(graph, d.docId)) nDeleted++
                            hashes.remove(d.docId); localById.remove(d.docId)
                        }
                        SyncLogic.PullAction.COPY_LOCAL_THEN_DELETE -> {
                            copyConflict(graph, requireNotNull(local))
                            if (deleteLocalNote(graph, d.docId)) nDeleted++
                            hashes.remove(d.docId); localById.remove(d.docId)
                        }
                        SyncLogic.PullAction.COPY_REMOTE -> {
                            copyConflict(graph, SyncLogic.LocalDoc(d.docId, d.category, d.title, d.content))
                        }
                        SyncLogic.PullAction.NONE -> if (remoteHash == null) hashes.remove(d.docId)
                            else if (d.docId !in pendingDeletes) hashes[d.docId] = remoteHash
                        SyncLogic.PullAction.KEEP_LOCAL -> Unit
                    }
                }
            } catch (e: Exception) {
                log("pull write failed, cursor retained: ${e.message}")
                return Result.failure() // Never spin on a failed page with hasMore=true.
            }
            if (body.hasMore && body.cursor <= cursor) {
                log("server returned a non-progressing pull cursor"); return Result.failure()
            }
            cursor = body.cursor
            state = state.copy(cursor = cursor, hashes = hashes)
            graph.config.saveSyncState(state) // Commit a fully applied page before requesting the next.
            log("pull batch: docs=${body.docs.size} cursor=$cursor")
            if (!body.hasMore) break
        }
        log("pull done: nPulled=$nPulled nDeleted=$nDeleted cursor=$cursor")

        // 2) push：本地新增/有变化的笔记
        val locals = localDocs(graph)
        val toPush = SyncLogic.planPush(locals, state.hashes)
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
        when (pushOutcome) {
            is SyncOutcome.Ok -> state = SyncLogic.mergePushResult(state, toPush, pushOutcome.results)
            is SyncOutcome.Unreachable -> return Result.retry()
            is SyncOutcome.Rejected -> return Result.failure()
        }
        graph.config.saveSyncState(state)
        if (pendingRestores.isNotEmpty()) {
            val confirmed = locals.filter { state.hashes[it.docId] == it.hash }.map { it.docId }.toSet()
            check(prefs.edit().putStringSet("pending_restores", pendingRestores - confirmed).commit()) {
                "恢复状态保存失败"
            }
        }

        // 3) 广播本地删除（pull 阶段没覆盖的：本地刚删、服务器还不知道）
        val delIds = SyncLogic.planDeletes(locals.map { it.docId }.toSet(), state.hashes.keys)
        val delOutcome = engine.delete(delIds.map { DeleteMark(DocKind.note, it, System.currentTimeMillis(), engine.deviceId) })
        when (delOutcome) {
            is SyncOutcome.Ok -> state = state.copy(hashes = state.hashes - delIds.toSet())
            is SyncOutcome.Unreachable -> return Result.retry()
            is SyncOutcome.Rejected -> return Result.failure()
        }
        graph.config.saveSyncState(state)
        applicationContext.getSharedPreferences("config", Context.MODE_PRIVATE)
            .edit().putLong("sync_last_ok", System.currentTimeMillis()).apply()
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
            // Unreadable existing files must not be mistaken for local deletions.
            val content = graph.notes.readNote(f)
            SyncLogic.LocalDoc(
                docId = id, content = content,
                category = f.parentFile?.let { p -> runCatching { p.relativeTo(graph.notes.root).path }.getOrNull() }.orEmpty(),
                title = f.nameWithoutExtension.replace(Regex("--[a-f0-9]{64}$"), ""),
            )
        }

    private fun noteId(f: File): String? {
        val m = Regex("--([a-f0-9]{64})\\.md$").find(f.name) ?: return null
        return m.groupValues[1]
    }

    private fun deleteLocalNote(graph: AppGraph, docId: String): Boolean {
        val oldFiles = graph.notes.allFiles().filter { noteId(it) == docId }
        for (old in oldFiles) if (!old.delete()) throw java.io.IOException("无法删除本地笔记 ${old.name}")
        if (oldFiles.isEmpty()) return false
        return true
    }

    private fun categories(raw: String): List<String> = raw.split('/', '\\').filter { it.isNotBlank() }

    private fun copyConflict(graph: AppGraph, d: SyncLogic.LocalDoc) {
        val cats = categories(d.category).takeIf { parts ->
            parts.size in 2..4 && parts.all { runCatching { com.willam.chatnotes.NoteFiles.segment(it, 60) }.isSuccess }
        } ?: listOf("同步冲突", "待整理")
        val id = sha256("sync-conflict\n${d.docId}\n${d.hash}")
        com.willam.chatnotes.NoteFiles(File(graph.app.filesDir, "notes"))
            .write(cats, "${d.title.ifBlank { "无标题" }}（冲突副本）", id, d.content)
    }

    /** Write the new file first, then remove the old path after success. */
    private fun applyRemote(graph: AppGraph, d: SyncDoc) {
        require(d.contentHash == sha256Hex16(d.content)) { "远端内容校验失败" }
        val cats = categories(d.category)
        val files = com.willam.chatnotes.NoteFiles(File(graph.app.filesDir, "notes"))
        val oldFiles = graph.notes.allFiles().filter { noteId(it) == d.docId }
        val target = File(cats.fold(graph.notes.root) { dir, part -> File(dir, com.willam.chatnotes.NoteFiles.segment(part, 60)) },
            com.willam.chatnotes.NoteFiles.segment(d.title, 40) + "--${d.docId}.md")
        if (oldFiles.any { it.canonicalFile == target.canonicalFile }) {
            files.overwrite(cats, d.title, d.docId, d.content)
        } else {
            files.write(cats, d.title, d.docId, d.content)
        }
        for (old in oldFiles) if (old.canonicalFile != target.canonicalFile && !old.delete())
            throw java.io.IOException("新文件已写入，但旧文件删除失败：${old.name}")
    }

    private fun deviceId(context: Context): String {
        val prefs = context.getSharedPreferences("sync", Context.MODE_PRIVATE)
        prefs.getString("device_id", null)?.let { return it }
        val id = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
        prefs.edit().putString("device_id", id).apply()
        return id
    }

    companion object {
        private fun work() = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        /** 打开 app 时全量对账 */
        fun enqueueOnAppOpen(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync", ExistingWorkPolicy.KEEP,
                work(),
            )
        }

        /** 笔记整理完成后增量同步 */
        fun enqueueAfterNoteChange(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync", ExistingWorkPolicy.APPEND_OR_REPLACE,
                work(),
            )
        }
    }
}
