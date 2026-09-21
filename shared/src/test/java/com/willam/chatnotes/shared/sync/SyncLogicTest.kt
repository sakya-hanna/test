package com.willam.chatnotes.shared.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncLogicTest {

    private fun local(id: String, content: String = "内容-$id") =
        SyncLogic.LocalDoc(docId = id, category = "编程", title = "标题$id", content = content)

    @Test
    fun `planPush 新增与变更都进批次`() {
        val synced = mapOf("a" to sha256Hex16("内容-a"), "b" to "旧hash")
        val push = SyncLogic.planPush(listOf(local("a"), local("b"), local("c")), synced)
        assertEquals(setOf("b", "c"), push.map { it.docId }.toSet())
    }

    @Test
    fun `planPush 无变化时空批次`() {
        val synced = mapOf("a" to sha256Hex16("内容-a"))
        assertTrue(SyncLogic.planPush(listOf(local("a")), synced).isEmpty())
    }

    @Test
    fun `planDeletes 只含已同步且本地消失的`() {
        val deletes = SyncLogic.planDeletes(localIds = setOf("a", "c"), syncedIds = setOf("a", "b"))
        assertEquals(listOf("b"), deletes)
    }

    @Test
    fun `applyPull 区分写入与墓碑`() {
        val docs = listOf(
            SyncDoc(DocKind.note, "w1", category = "", title = "t", content = "新内容", updatedAt = 1, deviceId = "pad", contentHash = sha256Hex16("新内容")),
            SyncDoc(DocKind.note, "w2", category = "", title = "t", content = "相同内容", updatedAt = 1, deviceId = "pad", contentHash = sha256Hex16("相同内容")),
            SyncDoc(DocKind.note, "d1", category = "", title = "", content = "", updatedAt = 2, deviceId = "pad", contentHash = "x"),
        )
        val localHashes = mapOf("w2" to sha256Hex16("相同内容"), "d1" to "本地还有")
        val (writes, deletes) = SyncLogic.applyPull(docs, localHashes)
        assertEquals(listOf("w1"), writes.map { it.docId })
        assertEquals(listOf("d1"), deletes.map { it.docId })
    }

    @Test
    fun `applyPull 未知墓碑忽略`() {
        val docs = listOf(SyncDoc(DocKind.note, "ghost", content = "", title = "", updatedAt = 1, deviceId = "pad", contentHash = "x"))
        val (writes, deletes) = SyncLogic.applyPull(docs, emptyMap())
        assertTrue(writes.isEmpty() && deletes.isEmpty())
    }

    @Test
    fun `mergePushResult 只记 accepted 与 same`() {
        val state = SyncState(hashes = mapOf("old" to "h"))
        val pushed = listOf(local("ok1"), local("same1"), local("older1"), local("bad1"))
        val results = listOf(
            PushResultItem("ok1", "new"),
            PushResultItem("same1", "same"),
            PushResultItem("older1", "older"),
            PushResultItem("bad1", "bad_hash"),
        )
        val merged = SyncLogic.mergePushResult(state, pushed, results)
        assertEquals(setOf("old", "ok1", "same1"), merged.hashes.keys)
    }
}
