package com.willam.chatnotes

/**
 * Pure text logic shared by the index writer and the query builder.
 * Index time and query time MUST use the same segmentation, or recall breaks.
 * No Android imports: unit-testable on the JVM.
 */
object SearchLogic {
    // CJK ideographs, extensions and kana are indexed as single characters so
    // short Chinese queries ("屏") still recall; ASCII runs stay whole tokens
    // so technical identifiers survive intact.
    fun isCjk(cp: Int): Boolean =
        cp in 0x3040..0x30FF || cp in 0x3400..0x4DBF || cp in 0x4E00..0x9FFF ||
            cp in 0xF900..0xFAFF || cp in 0x20000..0x2FA1F

    // Letters/digits keep '_', '+', '#' so C++, C# and foo_bar stay one token.
    // '.', '-', '/' split: version numbers and paths match per component.
    fun isTokenChar(cp: Int): Boolean =
        Character.isLetterOrDigit(cp) || cp == '_'.code || cp == '+'.code || cp == '#'.code

    fun tokens(text: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val c = text.codePointAt(i)
            when {
                isCjk(c) -> {
                    out.add(String(Character.toChars(c))); i += Character.charCount(c)
                }
                isTokenChar(c) -> {
                    val sb = StringBuilder()
                    while (i < text.length) {
                        val d = text.codePointAt(i)
                        if (isTokenChar(d) && !isCjk(d)) { sb.appendCodePoint(d); i += Character.charCount(d) }
                        else break
                    }
                    out.add(sb.toString().lowercase())
                }
                else -> i += Character.charCount(c)
            }
        }
        return out
    }

    /** Space-joined token stream stored in FTS columns. */
    fun tokenize(text: String): String = tokens(text).joinToString(" ")

    /**
     * Build a safe FTS5 MATCH expression from a raw user query.
     * Contiguous CJK runs become one phrase ("白 屏"); ASCII tokens become
     * quoted terms; everything is quoted so MATCH metacharacters are inert.
     */
    fun matchQuery(query: String, maxTerms: Int = 8): String? {
        val parts = mutableListOf<String>()
        var i = 0
        while (i < query.length && parts.size < maxTerms) {
            val c = query.codePointAt(i)
            when {
                isCjk(c) -> {
                    val sb = StringBuilder()
                    while (i < query.length && isCjk(query.codePointAt(i))) {
                        sb.appendCodePoint(query.codePointAt(i)); i += Character.charCount(query.codePointAt(i))
                    }
                    val spaced = sb.toString().codePoints().toArray().joinToString(" ") { String(Character.toChars(it)) }
                    parts.add("\"$spaced\"")
                }
                isTokenChar(c) -> {
                    val sb = StringBuilder()
                    while (i < query.length) {
                        val d = query.codePointAt(i)
                        if (isTokenChar(d) && !isCjk(d)) { sb.appendCodePoint(d); i += Character.charCount(d) }
                        else break
                    }
                    parts.add("\"${sb.toString().lowercase()}\"")
                }
                else -> i += Character.charCount(c)
            }
        }
        if (parts.isEmpty()) return null
        return parts.joinToString(" AND ")
    }

    /** LIKE pattern with escaped wildcards for the no-FTS fallback. */
    fun likePattern(query: String): String =
        "%" + query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"

    /** Search needles derived from the query, used to locate the first hit for snippets. */
    fun terms(query: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < query.length) {
            val c = query.codePointAt(i)
            if (isCjk(c)) {
                val sb = StringBuilder()
                while (i < query.length && isCjk(query.codePointAt(i))) {
                    sb.appendCodePoint(query.codePointAt(i)); i += Character.charCount(query.codePointAt(i))
                }
                out.add(sb.toString())
            } else if (isTokenChar(c)) {
                val sb = StringBuilder()
                while (i < query.length) {
                    val d = query.codePointAt(i)
                    if (isTokenChar(d) && !isCjk(d)) { sb.appendCodePoint(d); i += Character.charCount(d) }
                    else break
                }
                out.add(sb.toString().lowercase())
            } else i += Character.charCount(c)
        }
        return out
    }

    /**
     * Window around the first query hit, plus the highlighted range inside it.
     * Returns null when nothing matches (caller then shows the note title only).
     */
    fun snippet(original: String, query: String, radius: Int = 48): Triple<String, Int, Int>? {
        val lower = original.lowercase()
        var bestStart = -1; var bestLen = 0
        for (term in terms(query)) {
            val idx = lower.indexOf(term.lowercase())
            if (idx >= 0 && (bestStart < 0 || idx < bestStart)) { bestStart = idx; bestLen = term.length }
        }
        if (bestStart < 0) return null
        var start = (bestStart - radius).coerceAtLeast(0)
        var end = (bestStart + bestLen + radius).coerceAtMost(original.length)
        if (start > 0 && Character.isHighSurrogate(original[start])) start++
        if (end < original.length && Character.isHighSurrogate(original[end - 1])) end--
        val lead = if (start > 0) 1 else 0
        val text = (if (start > 0) "…" else "") + original.substring(start, end) + (if (end < original.length) "…" else "")
        val hitStart = lead + (bestStart - start)
        return Triple(text, hitStart, hitStart + bestLen)
    }
}
