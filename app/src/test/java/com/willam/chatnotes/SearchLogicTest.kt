package com.willam.chatnotes

import org.junit.Assert.*
import org.junit.Test

class SearchLogicTest {
    @Test fun cjkIsIndexedAsSingleCharacters() {
        assertEquals("白 屏 修 复", SearchLogic.tokenize("白屏修复"))
        assertEquals("han 字", SearchLogic.tokenize("han字")) // ASCII word + CJK chars
    }
    @Test fun technicalTokensStayIntact() {
        assertEquals("c++ c# foo_bar", SearchLogic.tokenize("C++ C# foo_bar"))
        // Version numbers and paths split per component, never lost.
        assertEquals(listOf("0x80004002"), SearchLogic.tokens("0x80004002"))
        assertEquals(listOf("app", "build", "gradle", "kts"), SearchLogic.tokens("app/build.gradle.kts"))
    }
    @Test fun matchQueryQuotesEverythingSoMetaCharactersAreInert() {
        assertEquals("\"白 屏\"", SearchLogic.matchQuery("白屏"))
        assertEquals("\"0x80004002\"", SearchLogic.matchQuery("0x80004002"))
        // MATCH syntax characters must not change query structure: every word
        // becomes a quoted term joined by AND — never raw operators.
        val q = SearchLogic.matchQuery("a OR b AND NOT")!!
        assertEquals("\"a\" AND \"or\" AND \"b\" AND \"and\" AND \"not\"", q)
        val quoted = SearchLogic.matchQuery("\"quoted\" *")!!
        assertTrue(quoted.contains("\"quoted\""))
        assertFalse(quoted.contains("*"))
    }
    @Test fun matchQueryRejectsEmptyAndPunctuationOnly() {
        assertNull(SearchLogic.matchQuery(""))
        assertNull(SearchLogic.matchQuery("   "))
        assertNull(SearchLogic.matchQuery("，。！？"))
    }
    @Test fun likePatternEscapesWildcards() {
        assertEquals("%100\\%done\\_yes\\\\%", SearchLogic.likePattern("100%done_yes\\"))
    }
    @Test fun longQueriesAreTruncated() {
        val q = (1..50).joinToString(" ") { "词$it" }
        val terms = SearchLogic.matchQuery(q)!!.split(" AND ")
        assertEquals(8, terms.size)
    }
    @Test fun snippetHighlightsFirstHitWithEllipses() {
        val body = "A".repeat(100) + "WebView 白屏的修复方法" + "B".repeat(100)
        val s = SearchLogic.snippet(body, "白屏")!!
        assertTrue(s.first.startsWith("…") && s.first.endsWith("…"))
        // hitStart/hitEnd index into the snippet text itself (ellipsis included).
        assertEquals("白屏", s.first.substring(s.second, s.third))
        assertEquals(s.first.indexOf("白屏"), s.second)
    }
    @Test fun snippetReturnsNullWhenNothingMatches() {
        assertNull(SearchLogic.snippet("完全无关的内容", "白屏"))
    }
    @Test fun snippetOfShortTextHasNoEllipsesAndAsciiMatchesCaseInsensitive() {
        val s = SearchLogic.snippet("fix WebView crash", "webview")!!
        assertFalse(s.first.startsWith("…"))
        assertEquals("WebView", s.first.substring(s.second, s.third))
    }
    @Test fun surrogatePairsDoNotBreakWindows() {
        val body = "x".repeat(10) + "🙂问题在🙂这里" + "y".repeat(60)
        val s = SearchLogic.snippet(body, "问题")!!
        assertEquals("问题", s.first.substring(s.second, s.third))
    }
    @Test fun snippetsFindsAllNonOverlappingWindows() {
        val body = "开头 白屏 第一次。" + "x".repeat(120) + "中间 白屏 第二次。" + "y".repeat(120) + "结尾 白屏 第三次。"
        val all = SearchLogic.snippets(body, "白屏")
        assertEquals(3, all.size)
        assertEquals("白屏", all[0].first.substring(all[0].second, all[0].third))
        assertEquals("白屏", all[1].first.substring(all[1].second, all[1].third))
        assertEquals("白屏", all[2].first.substring(all[2].second, all[2].third))
        // Windows advance: each snippet contains its ordinal marker.
        assertTrue(all[0].first.contains("第一次"))
        assertTrue(all[1].first.contains("第二次"))
        assertTrue(all[2].first.contains("第三次"))
    }
    @Test fun snippetsRespectsMaxAndReturnsEmptyWithoutHits() {
        val body = (1..10).joinToString(" ") { "hit$it " + "pad".repeat(30) }
        assertEquals(3, SearchLogic.snippets(body, "hit", max = 3).size)
        assertTrue(SearchLogic.snippets("无关内容", "白屏").isEmpty())
        // snippet() stays consistent with snippets()'s first window.
        val s = SearchLogic.snippet(body, "hit3")
        assertEquals(s, SearchLogic.snippets(body, "hit3").first())
    }
    @Test fun indexAndQuerySegmentationAreConsistent() {
        // The core recall invariant: whatever the user can type must be findable
        // in the token stream produced at index time.
        val docs = listOf(
            "WebView 白屏怎么办",
            "如何避免同名笔记被覆盖",
            "error 0x80004002 while reading C++ file",
            "C# 与 C++ 的互操作",
            "聊天一多网页就很卡"
        ).map { SearchLogic.tokenize(it) }
        val queries = listOf("白屏", "覆盖", "0x80004002", "c++", "C#", "很卡", "笔记")
        for (q in queries) {
            val match = SearchLogic.matchQuery(q)!!
            val terms = match.split(" AND ").map { it.trim('"') }
            assertTrue("query $q recalled nothing", docs.any { d -> terms.all { t -> d.contains(t) } })
        }
    }
}
