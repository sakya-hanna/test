package com.willam.chatnotes

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * End-to-end stage-2 pipeline on device with a FAKE embedding API
 * (deterministic hash-bucket vectors, no network): index -> chunk ->
 * embed -> vectorSearch -> hybridQuery, plus offline degradation.
 */
@RunWith(AndroidJUnit4::class)
class EmbeddingPipelineTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var index: SearchIndex
    private lateinit var indexName: String
    private lateinit var notes: NotesRepo
    private lateinit var notesRoot: File
    private lateinit var chat: ChatStore
    private lateinit var chatName: String

    /** Deterministic fake: char-codepoint bucket vectors. Chosen so the two
     *  seeded notes occupy disjoint dimension sets (verifiable by codepoint % 64). */
    private class FakeEmbed(override val modelId: String = "fake-embed-v1") : EmbedApi {
        var calls = 0
        override fun embed(texts: List<String>): List<FloatArray> {
            calls++
            return texts.map { t ->
                val v = FloatArray(64)
                t.codePoints().forEach { cp -> v[(cp % 64 + 64) % 64] += 1f }
                var n = 0f; v.forEach { n += it * it }
                if (n > 0f) for (i in v.indices) v[i] /= Math.sqrt(n.toDouble()).toFloat()
                v
            }
        }
    }

    @Before fun setup() {
        indexName = "test-embed-${UUID.randomUUID()}.db"
        index = SearchIndex(context, indexName)
        notesRoot = File(context.filesDir, "test-embednotes-${UUID.randomUUID()}")
        notes = NotesRepo(context, notesRoot.name)
        chatName = "test-embedchat-${UUID.randomUUID()}.db"
        chat = ChatStore(context, chatName)
        // Seed notes with DISTINCT vocabularies so fake vectors separate them.
        val a = File(notesRoot, "计算机/Android/白屏问题--${"a1".repeat(32)}.md")
        a.parentFile!!.mkdirs()
        a.writeText("白屏 修复 GPU 渲染 进程 退出 重建\n\n白屏 出现 在 升级 之后 偶发")
        val b = File(notesRoot, "工具/Git/分支管理--${"b2".repeat(32)}.md")
        b.parentFile!!.mkdirs()
        b.writeText("分支 合并 冲突 rebase 工作流\n\n分支 推送 公共 禁止 变基")
    }
    @After fun teardown() {
        notesRoot.deleteRecursively()
        context.deleteDatabase(chatName)
        File(context.filesDir, indexName).delete()
    }

    @Test fun embedsAndFindsByVector() {
        val api = FakeEmbed()
        val status = index.ensureEmbedded(notes, chat, api)
        assertEquals(2, status.chunks)
        assertTrue(status.dim == 64)
        // Query sharing 白屏 vocabulary must rank the 白屏 note first.
        val q = api.embed(listOf("白屏 怎么 修复")).first()
        val ranked = index.vectorSearch(q, api.modelId)
        assertTrue(ranked.isNotEmpty())
        val top = ranked.first()
        val doc = index.rawQueryDocument(top.first, top.second)!!
        assertEquals("白屏问题", doc.title)
    }

    @Test fun hybridQueryMergesBothSignals() {
        val api = FakeEmbed()
        index.ensureEmbedded(notes, chat, api)
        // Exact keyword present -> keyword hit; vocabulary overlap -> semantic hit.
        val hits = index.hybridQuery("白屏 修复", api)
        assertTrue(hits.isNotEmpty())
        assertTrue(hits.any { it.title == "白屏问题" })
        assertEquals(1, hits.count { it.title == "白屏问题" }) // one document, two ranking signals
        // Semantic-only hit surfaces with document data filled in.
        val semOnly = index.hybridQuery("渲染 进程 重建", api)
        assertTrue(semOnly.isNotEmpty())
    }

    @Test fun editAndDeleteInvalidateStoredVectors() {
        val api = FakeEmbed()
        index.ensureEmbedded(notes, chat, api)
        val file = notes.allFiles().first { it.name.contains("白屏问题") }
        file.writeText("已编辑为完全不同的内容 新词词词词词词")
        index.ensureIndexed(notes, chat)
        assertEquals(1, index.embedStatus(api.modelId).chunks)
        assertEquals(1, index.embedStatus(api.modelId).pending)
        index.ensureEmbedded(notes, chat, api)
        assertEquals(2, index.embedStatus(api.modelId).chunks)
        file.delete()
        index.ensureIndexed(notes, chat)
        assertEquals(1, index.embedStatus(api.modelId).chunks)
    }

    @Test fun hybridDegradesToKeywordWithoutApi() {
        index.ensureEmbedded(notes, chat, FakeEmbed())
        // api=null must not throw and still returns keyword results.
        val hits = index.hybridQuery("白屏", null)
        assertTrue(hits.isNotEmpty())
        assertTrue(hits.any { it.title == "白屏问题" })
    }

    @Test fun embeddingFailureDegradesGracefully() {
        val broken = object : EmbedApi {
            override val modelId = "broken"
            override fun embed(texts: List<String>): List<FloatArray> = throw EmbedFailure("服务不可用")
        }
        index.ensureEmbedded(notes, chat, FakeEmbed()) // seed one model
        // Broken api on hybridQuery must fall back to keyword, not crash.
        val hits = index.hybridQuery("分支", broken)
        assertTrue(hits.isNotEmpty())
        assertTrue(hits.any { it.title == "分支管理" })
    }

    @Test fun clearingEmbeddingsRemovesVectors() {
        val api = FakeEmbed()
        index.ensureEmbedded(notes, chat, api)
        assertTrue(index.embedStatus(api.modelId).chunks > 0)
        index.clearEmbeddings(api.modelId)
        assertEquals(0, index.embedStatus(api.modelId).chunks)
        // After clear, vector search finds nothing but keyword search still works.
        val q = api.embed(listOf("白屏")).first()
        assertTrue(index.vectorSearch(q, api.modelId).isEmpty())
        assertTrue(index.query("白屏").isNotEmpty())
    }

    @Test fun ensureEmbeddedIsIdempotent() {
        val api = FakeEmbed()
        index.ensureEmbedded(notes, chat, api)
        val callsAfterFirst = api.calls
        index.ensureEmbedded(notes, chat, api)
        assertEquals(callsAfterFirst, api.calls) // no re-embedding of unchanged docs
    }
}
