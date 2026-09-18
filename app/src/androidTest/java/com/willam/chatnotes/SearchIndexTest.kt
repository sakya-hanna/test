package com.willam.chatnotes

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * On-device search index tests: FTS5 availability, query recall for Chinese
 * and technical terms, MATCH safety, incremental sync. Requires a real
 * device or emulator (SQLite behaviour differs from JVM stubs).
 *
 * SearchIndex is exercised through the REAL app graph repositories backed by
 * throwaway files, so the full pipeline (scan -> upsert -> FTS) is covered.
 */
@RunWith(AndroidJUnit4::class)
class SearchIndexTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var index: SearchIndex
    private lateinit var indexName: String
    private lateinit var notes: NotesRepo
    private lateinit var notesRoot: File
    private lateinit var chat: ChatStore
    private lateinit var chatName: String

    @Before fun setup() {
        indexName = "test-search-${UUID.randomUUID()}.db"
        index = SearchIndex(context, indexName)
        notesRoot = File(context.filesDir, "test-notes-${UUID.randomUUID()}")
        notes = NotesRepo(context, notesRoot.name)
        chatName = "test-chat-${UUID.randomUUID()}.db"
        chat = ChatStore(context, chatName)
    }
    @After fun teardown() {
        notesRoot.deleteRecursively()
        context.deleteDatabase(chatName)
        File(context.filesDir, indexName).delete()
    }

    @Test fun fts5AvailableOnDevice() {
        val status = index.status()
        // Modern devices ship FTS5; if false, LIKE fallback is in use —
        // the assertion documents the mode for the validation log.
        assertTrue("FTS5 not available; LIKE fallback in use", status.fts)
    }

    @Test fun chineseShortQueryAndErrorCodeRecall() {
        putMessage("c1", "u1", "", "user", "WebView 出现白屏怎么办")
        putMessage("c1", "a1", "u1", "assistant", "白屏通常是 GPU 加速问题，错误码 0x80004002")
        index.rebuild(notes, chat)
        val hits = index.query("白屏")
        assertTrue(hits.isNotEmpty())
        assertTrue(hits.any { it.conversationId == "c1" })
        val code = index.query("0x80004002")
        assertTrue(code.isNotEmpty())
        assertTrue(code.any { it.snippet.contains("0x80004002") })
    }

    @Test fun matchSyntaxCharactersAreInert() {
        putMessage("c2", "u1", "", "user", "正常内容")
        index.rebuild(notes, chat)
        // None of these may crash or throw; result presence is irrelevant.
        index.query("\"quoted\" *")
        index.query("OR AND NOT")
        assertTrue(index.query("").isEmpty())
        assertTrue(index.query("   ").isEmpty())
        assertTrue(index.query("，。！？").isEmpty())
    }

    @Test fun incrementalSyncPicksUpNewMessages() {
        putMessage("c3", "u1", "", "user", "初始消息增量同步测试")
        index.rebuild(notes, chat)
        assertTrue(index.query("增量同步测试").isNotEmpty())
        putMessage("c3", "u2", "u1", "user", "追加的消息含有独特的词组甲乙丙丁")
        index.ensureIndexed(notes, chat)
        assertTrue(index.query("甲乙丙丁").isNotEmpty())
    }

    @Test fun deletedConversationLeavesTheIndex() {
        putMessage("c4", "u1", "", "user", "这条会话将被删除孤立词戊己庚辛")
        index.rebuild(notes, chat)
        assertTrue(index.query("戊己庚辛").isNotEmpty())
        chat.writableDatabase.execSQL("DELETE FROM conversations WHERE id='c4'")
        chat.writableDatabase.execSQL("DELETE FROM messages WHERE cid='c4'")
        index.ensureIndexed(notes, chat)
        assertTrue(index.query("戊己庚辛").isEmpty())
    }

    private fun putMessage(cid: String, id: String, parent: String, role: String, text: String) {
        chat.applyEvent(JSONObject().put("type", "message").put("conversationId", cid)
            .put("msgId", id).put("parentId", parent).put("role", role).put("text", text)
            .put("status", "complete").put("source", "network").put("createdAt", 100).put("select", true))
    }
}
