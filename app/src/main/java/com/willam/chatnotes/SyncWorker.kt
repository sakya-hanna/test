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

        // 1) 全量对账：先 pull 服务器增量（其他设备/本机删除的墓碑）
        var cursor = graph.config.syncCursor()
        var nPulled = 0
        var nDeleted = 0
        val writes = mutableListOf<SyncDoc>()
        val tombstones = mutableListOf<com.willam.chatnotes.shared.sync.SyncDoc>()
        while (true) {
            val (outcome, resp) = engine.pull(cursor)
            when (outcome) {
                is SyncOutcome.Unreachable -> return Result.success() // 静默：下次再同步
                is SyncOutcome.Rejected -> return Result.failure()   // 配置问题：不重试
                is SyncOutcome.Ok -> {}
            }
            val body = resp!!
            val localHashes = localHashes(graph)
            val (w, d) = SyncLogic.applyPull(body.docs, localHashes)
            writes += w
            tombstones += d
            nPulled += body.docs.size
            cursor = body.cursor
            if (!body.hasMore) break
        }
        // 应用 pull 结果：墓碑删除本地文件；写入仅落盘（索引重建由现有入口覆盖）
        for (t in tombstones) if (deleteLocalNote(graph, t.docId)) nDeleted++
        for (w in writes) writeRemoteNote(graph, w)

        // 2) push：本地新增/有变化的笔记
        val synced = graph.config.syncState()
        val locals = localDocs(graph)
        val toPush = SyncLogic.planPush(locals, synced.hashes)
        val docs = toPush.map { d ->
            SyncDoc(
                kind = DocKind.note, docId = d.docId, category = d.category,
                title = d.title, content = d.content, updatedAt = System.currentTimeMillis(),
                deviceId = engine.deviceId, contentHash = d.hash,
            )
        }
        val pushOutcome = engine.push(docs)

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
        return Result.success()
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

    private fun writeRemoteNote(graph: AppGraph, d: SyncDoc) {
        val cats = if (d.category.isBlank()) emptyList() else d.category.split('/', '\\').filter { it.isNotBlank() }
        val files = com.willam.chatnotes.NoteFiles(File(graph.app.filesDir, "notes"))
        runCatching { files.write(cats, d.title, d.docId, d.content) }
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
