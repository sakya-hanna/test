package com.willam.chatnotes

import org.junit.Assert.*
import org.junit.Test

class SummaryFormatTest {
    private val good = """{"path":["计算机","编程"],"title":"标题","markdown":"内容"}"""
    @Test fun acceptsJsonAndFencedJson() {
        assertEquals("内容", SummaryResult.parse(good).markdown)
        assertEquals("内容", SummaryResult.parse("```JSON\n$good\n```").markdown)
    }
    @Test fun rejectsInvalidDirectoryAndEmptyBody() {
        for (raw in listOf(good.replace("计算机", ".."), good.replace("内容", ""), good.replace("[\"计算机\",\"编程\"]", "[]"))) {
            assertTrue(runCatching { SummaryResult.parse(raw) }.isFailure)
        }
    }
    @Test fun fingerprintsChangeForNewContentButNotMetadata() {
        val m = ChatMessage("a", "u", "assistant", "第一版", "complete", "network", false, 1)
        val a = ConversationSnapshot("c", "标题", "", listOf(m), emptyList(), 1)
        assertEquals(a.fingerprint, a.copy(title = "新标题", revision = 99).fingerprint)
        assertNotEquals(a.fingerprint, a.copy(messages = listOf(m.copy(text = "第二版"))).fingerprint)
        assertNotEquals(a.fingerprint, a.copy(id = "另一会话").fingerprint)
    }
    @Test fun splittingPreservesUnicodeAndAllText() {
        val text = "a🙂汉字".repeat(150)
        val chunks = SummaryWorker.split(text, 12)
        assertEquals(text, chunks.joinToString(""))
        assertTrue(chunks.all { it.length <= 12 && !Character.isHighSurrogate(it.last()) })
    }
}
