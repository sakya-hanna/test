package com.willam.chatnotes.shared.sync

import kotlinx.serialization.Serializable
import java.security.MessageDigest

fun sha256Hex16(content: String): String =
    MessageDigest.getInstance("SHA-256").digest(content.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }.take(16)

/** 端上持久化的同步状态：服务器光标 + 已确认同步的 docId→hash */
@Serializable
data class SyncState(
    val cursor: Long = 0,
    val hashes: Map<String, String> = emptyMap(),
)

/**
 * 同步决策（纯函数，无 IO，JVM 可测）：
 * 笔记文件名含 64hex jobId（ID 不变），P1 起支持改名/移动——变更检测必须覆盖标题与分类。
 */
object SyncLogic {

    enum class PullAction { NONE, WRITE_REMOTE, DELETE_LOCAL, KEEP_LOCAL, COPY_REMOTE, COPY_LOCAL_THEN_DELETE }

    /** Decide from the last acknowledged version, not merely the two current versions.
     * A pending local deletion must not be undone by an older server change.
     * Divergent edits are retained as separate notes before either side is replaced.
     */
    fun pullAction(localHash: String?, acknowledgedHash: String?, remoteHash: String?,
                   pendingDelete: Boolean, pendingRestore: Boolean = false): PullAction {
        if (pendingRestore && localHash != null) return PullAction.KEEP_LOCAL
        if (remoteHash == null) {
            if (pendingDelete || localHash == null) return PullAction.NONE
            return if (localHash != acknowledgedHash) PullAction.COPY_LOCAL_THEN_DELETE else PullAction.DELETE_LOCAL
        }
        if (pendingDelete) return if (remoteHash == acknowledgedHash) PullAction.KEEP_LOCAL else PullAction.COPY_REMOTE
        if (localHash == remoteHash) return PullAction.NONE
        if (localHash == null) return PullAction.WRITE_REMOTE
        if (localHash != acknowledgedHash) {
            return if (remoteHash == acknowledgedHash) PullAction.KEEP_LOCAL else PullAction.COPY_REMOTE
        }
        return PullAction.WRITE_REMOTE
    }

    /** 本地文档的精简描述（由 app 层从文件系统提取） */
    data class LocalDoc(
        val docId: String,
        val category: String,
        val title: String,
        val content: String,
    ) {
        /** 脏检查指纹：分类+标题+内容全量。只算内容会漏掉改名/移动（服务端将永远收不到新标题）。 */
        val hash: String get() = memoHash(category, title, content)
    }

    /**
     * 端上脏检查指纹（仅存于本地 sync_state 与 pull 对比，不上线）。
     * 线上 contentHash 保持纯内容 sha256（服务器 bad_hash 校验与协议不变）。
     */
    fun memoHash(category: String, title: String, content: String): String =
        sha256Hex16(category + "\u0000" + title + "\u0000" + content)

    /** 需要推送的文档：本地新增（不在已同步集合）或指纹与已同步不同 */
    fun planPush(local: List<LocalDoc>, syncedHashes: Map<String, String>): List<LocalDoc> =
        local.filter { syncedHashes[it.docId] != it.hash }

    /** 需要广播删除的文档：上次已同步存在、现在本地已不在 */
    fun planDeletes(localIds: Set<String>, syncedIds: Set<String>): List<String> =
        (syncedIds - localIds).sorted()

    /** pull 应用决策：返回 (应写入本地的文档, 应删除本地的墓碑)。对比同样用全量指纹——远端改名/移动也要落到本机 */
    fun applyPull(
        docs: List<SyncDoc>,
        localHashes: Map<String, String>,
    ): Pair<List<SyncDoc>, List<SyncDoc>> {
        val writes = mutableListOf<SyncDoc>()
        val deletes = mutableListOf<SyncDoc>()
        for (d in docs) {
            if (d.content.isEmpty() && d.title.isEmpty()) {
                // 墓碑：仅在本地还有该文件且未发生本地修改时执行删除
                if (localHashes.containsKey(d.docId)) deletes.add(d)
            } else if (localHashes[d.docId] != memoHash(d.category, d.title, d.content)) {
                writes.add(d)
            }
        }
        return writes to deletes
    }

    /** 合并 push 结果到状态：accepted/same 视为已同步；older/conflict/bad_hash 保留旧状态待重试 */
    fun mergePushResult(state: SyncState, pushed: List<LocalDoc>, results: List<PushResultItem>): SyncState {
        val byId = results.associateBy { it.docId }
        val hashes = state.hashes.toMutableMap()
        for (p in pushed) {
            when (byId[p.docId]?.status) {
                "new", "same" -> hashes[p.docId] = p.hash
            }
        }
        return state.copy(hashes = hashes)
    }
}
