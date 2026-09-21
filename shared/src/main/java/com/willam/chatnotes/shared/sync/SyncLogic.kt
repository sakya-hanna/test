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
 * 笔记文件不可变（文件名含 64hex jobId），变更只有 新增/删除；内容变化视为该 ID 的新版本。
 */
object SyncLogic {

    /** 本地文档的精简描述（由 app 层从文件系统提取） */
    data class LocalDoc(
        val docId: String,
        val category: String,
        val title: String,
        val content: String,
    ) {
        val hash: String get() = sha256Hex16(content)
    }

    /** 需要推送的文档：本地新增（不在已同步集合）或 hash 与已同步不同 */
    fun planPush(local: List<LocalDoc>, syncedHashes: Map<String, String>): List<LocalDoc> =
        local.filter { syncedHashes[it.docId] != it.hash }

    /** 需要广播删除的文档：上次已同步存在、现在本地已不在 */
    fun planDeletes(localIds: Set<String>, syncedIds: Set<String>): List<String> =
        (syncedIds - localIds).sorted()

    /** pull 应用决策：返回 (应写入本地的文档, 应删除本地的墓碑) */
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
            } else if (localHashes[d.docId] != d.contentHash) {
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
