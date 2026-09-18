package com.willam.chatnotes

import org.junit.Assert.*

import org.junit.Test

class EmbeddingLogicTest {
    // ---- Chunker ----
    @Test fun chunkerCarriesTitleAndRespectsFences() {
        val body = "第一段简介。\n\n```kotlin\nval a = 1\nval b = 2\nval c = 3\nval d = 4\n```\n\n结尾一段。"
        val chunks = Chunker.chunk("测试标题", body, target = 40)
        assertTrue(chunks.isNotEmpty())
        // Every chunk carries the title as context.
        assertTrue(chunks.all { it.startsWith("## 测试标题") })
        // The code fence content stays in one chunk (never split mid-fence).
        val fenceChunk = chunks.firstOrNull { it.contains("val a = 1") }
        assertNotNull(fenceChunk)
        assertTrue(fenceChunk!!.contains("val d = 4"))
    }
    @Test fun chunkerHardSplitsRunawayParagraphs() {
        val one = "字".repeat(3000)
        val chunks = Chunker.chunk("T", one, target = 500)
        assertTrue(chunks.size >= 5)
        assertTrue(chunks.sumOf { it.length } >= 3000)
    }
    @Test fun chunkerBoundsTotalChunksAndBody() {
        val huge = (1..2000).joinToString("\n\n") { "段落$it 内容" }
        val chunks = Chunker.chunk("T", huge)
        assertTrue(chunks.size <= 40)
    }
    @Test fun chunkerEmptyBodyYieldsNothing() {
        assertTrue(Chunker.chunk("T", "   \n\n  ").isEmpty())
    }

    // ---- VectorCodec ----
    @Test fun vectorRoundTripAndCosine() {
        val v = floatArrayOf(1f, 0f, 0.5f, -0.25f)
        val bytes = VectorCodec.encode(v)
        assertEquals(v.toList(), VectorCodec.decode(bytes).toList())
        assertEquals(1.0f, VectorCodec.cosine(v, v), 1e-5f)
        assertEquals(0.0f, VectorCodec.cosine(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f)), 1e-5f)
        // Dimension mismatch must be rejected, not silently averaged.
        val dimMismatch = runCatching { VectorCodec.cosine(floatArrayOf(1f), floatArrayOf(1f, 2f)) }
        assertTrue(dimMismatch.isFailure && dimMismatch.exceptionOrNull() is IllegalArgumentException)
        // Zero vector scores 0 instead of NaN.
        assertEquals(0.0f, VectorCodec.cosine(floatArrayOf(0f, 0f), floatArrayOf(1f, 1f)), 1e-6f)
    }
    @Test fun decodeRejectsCorruptBlobs() {
        assertTrue(runCatching { VectorCodec.decode(byteArrayOf(1, 2, 3)) }.isFailure)
        assertTrue(runCatching { VectorCodec.decode(ByteArray(0)) }.isFailure)
    }

    // ---- RRF ----
    @Test fun rrfFusesWithoutMixingScoreScales() {
        val kw = listOf("a", "b", "c")          // bm25 ranks
        val sem = listOf("c", "d", "a")         // cosine ranks
        val fused = Rrf.fuse(listOf(kw, sem))
        // "a" (1st + 3rd) and "c" (3rd + 1st) tie at the top; "d" (semantic-only)
        // must appear; order among non-overlaps follows contribution.
        val keys = fused.map { it.first }
        assertTrue(keys.containsAll(listOf("a", "b", "c", "d")))
        assertEquals(setOf("a", "c"), setOf(fused[0].first, fused[1].first))
        assertTrue(fused[0].second > fused[3].second)
    }
    @Test fun rrfEmptyInputs() {
        assertTrue(Rrf.fuse(listOf()).isEmpty())
        assertTrue(Rrf.fuse(listOf(emptyList(), emptyList())).isEmpty())
    }
    @Test fun rrfSingleListPreservesOrder() {
        val fused = Rrf.fuse(listOf(listOf("x", "y", "z")))
        assertEquals(listOf("x", "y", "z"), fused.map { it.first })
    }
}
