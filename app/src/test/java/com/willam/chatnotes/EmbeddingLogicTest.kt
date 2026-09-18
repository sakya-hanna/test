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

class CategoryContextTest {
    @Test fun rendersCandidatesWithSamplesAndRelevance() {
        val cs = listOf(
            CategoryCandidate(listOf("计算机", "Android"), listOf("WebView 白屏修复", "渲染进程退出处理"), 0.72f, true),
            CategoryCandidate(listOf("工具", "Git"), listOf("rebase 工作流"), 0.41f, true),
            CategoryCandidate(listOf("未分类", "杂项"), emptyList(), 0f, false)
        )
        val text = renderCategoryContext(cs)
        assertTrue(text.contains("计算机 / Android（高度相关）"))
        assertTrue(text.contains("｜已有笔记：WebView 白屏修复、渲染进程退出处理"))
        assertTrue(text.contains("工具 / Git（较相关）"))
        // keyword-only candidates get no relevance tag
        assertTrue(text.contains("未分类 / 杂项\n") || text.contains("未分类 / 杂项\r") || Regex("未分类 / 杂项($|｜)").containsMatchIn(text))
        // overlap-warning rule only when samples exist
        assertTrue(text.contains("与《标题》相关，可考虑合并"))
        // boundaries
        assertTrue(renderCategoryContext(emptyList()).isEmpty())
    }
    @Test fun boundsInjectedLines() {
        val many = (1..30).map { CategoryCandidate(listOf("a$it", "b$it"), listOf("t$it"), 0.6f, true) }
        val text = renderCategoryContext(many)
        assertTrue(text.lines().count { it.startsWith("- ") } <= 16)
        val longTitle = CategoryCandidate(listOf("x".repeat(300), "y"), listOf("z".repeat(300)), 0.9f, true)
        assertTrue(renderCategoryContext(listOf(longTitle)).lines().none { it.length > 400 })
    }
    @Test fun lowRelevanceBracketAndNoSamplesRule() {
        val cs = listOf(CategoryCandidate(listOf("a", "b"), emptyList(), 0.2f, true))
        val text = renderCategoryContext(cs)
        assertTrue(text.contains("a / b（低相关）"))
        // no samples anywhere -> generic synonym rule instead of merge hint
        assertTrue(text.contains("不要新建与上面列表同义"))
        assertFalse(text.contains("可考虑合并"))
    }
}
