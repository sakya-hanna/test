package com.willam.chatnotes

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

data class SearchHit(
    val kind: String,          // "note" | "conv"
    val file: File,            // note file (kind=note) or empty (kind=conv)
    val title: String,
    val category: String,
    val updatedAt: Long,
    val snippet: String,
    val hitStart: Int,
    val hitEnd: Int,
    val score: Int,
    val conversationId: String,
    val conversationTitle: String,
    val moreHits: Int = 0      // additional hit windows hidden behind "展开 N 处"
)

/**
 * Stage-1 full-text search over note files and raw conversations.
 *
 * `documents` keeps RAW text (used by the LIKE fallback and by snippets).
 * When the runtime supports FTS5, `doc_fts` mirrors each row as a regular
 * FTS5 table (kind/id UNINDEXED) over the tokenized text — regular tables
 * allow plain DELETE, which keeps upserts simple on every API level.
 * Devices without FTS5 search the same rows with LIKE; recall differs but
 * the two modes share all parsing and ranking code.
 */
class SearchIndex(private val context: Context, name: String = "search.db") {
    private val db: SQLiteDatabase by lazy {
        SQLiteDatabase.openOrCreateDatabase(File(context.filesDir, name), null)
    }
    private var ftsMode = false
    private var schemaReady = false
    @Volatile private var dirty = true

    // ---- vector layer (stage 2) ----
    data class EmbedStatus(val model: String, val dim: Int, val chunks: Int, val pending: Int, val failed: Int)

    @Synchronized fun ensureEmbedded(
        notes: NotesRepo, chat: ChatStore, api: EmbedApi,
        budget: Int = 25, // max embeddings per call (one batch)
        progress: (Int, Int) -> Unit = { _, _ -> }
    ): EmbedStatus {
        ensureIndexed(notes, chat) // documents must be current first
        val model = api.modelId
        // Pending chunk rows: added when a document is (re)embedded.
        val rows = db.rawQuery(
            "SELECT d.kind,d.id,d.title,d.body FROM documents d " +
                "LEFT JOIN chunks c ON c.kind=d.kind AND c.doc_id=d.id AND c.model=? " +
                "WHERE c.kind IS NULL", arrayOf(model)
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(arrayOf(c.getString(0), c.getString(1), c.getString(2), c.getString(3)))
            }
        }
        var done = 0; val total = rows.size
        var batch = mutableListOf<Triple<String, String, String>>() // kind,id -> chunkText
        val docChunks = mutableListOf<Pair<Pair<String, String>, String>>()
        fun flush() {
            if (batch.isEmpty()) return
            val vectors = api.embed(batch.map { it.third })
            require(vectors.size == batch.size) { "向量返回数量不一致" }
            db.beginTransaction()
            try {
                batch.forEachIndexed { i, (kind, id, _) ->
                    val seq = nextSeq(kind, id, model)
                    db.execSQL("INSERT OR REPLACE INTO chunks(kind,doc_id,seq,model,dim,vector,input_hash) VALUES(?,?,?,?,?,?,?)",
                        arrayOf(kind, id, seq, model, vectors[i].size, VectorCodec.encode(vectors[i]),
                            sha256(docChunks[i].second)))
                }
                db.setTransactionSuccessful()
            } finally { db.endTransaction() }
            batch = mutableListOf(); docChunks.clear()
        }
        for (r in rows) {
            if (Thread.currentThread().isInterrupted) break
            val kind = r[0]; val id = r[1]; val title = r[2]; val body = r[3]
            val parts = Chunker.chunk(title, body)
            if (parts.isEmpty()) { markEmbeddedEmpty(kind, id, model); continue }
            for (p in parts) {
                if (batch.size >= budget) flush()
                batch.add(Triple(kind, id, p)); docChunks.add((kind to id) to p)
                if (batch.size >= budget) flush()
            }
            done++
            if (done % 5 == 0) progress(done, total)
        }
        flush()
        return embedStatus(model)
    }

    private fun nextSeq(kind: String, id: String, model: String): Int =
        db.rawQuery("SELECT COALESCE(MAX(seq),-1)+1 FROM chunks WHERE kind=? AND doc_id=? AND model=?",
            arrayOf(kind, id, model)).use { it.moveToFirst(); it.getInt(0) }

    private fun markEmbeddedEmpty(kind: String, id: String, model: String) {
        db.execSQL("INSERT OR REPLACE INTO chunks(kind,doc_id,seq,model,dim,vector,input_hash) VALUES(?,?,0,?,0,X'','')",
            arrayOf(kind, id, model))
    }

    @Synchronized fun embedStatus(model: String): EmbedStatus {
        ensureSchema()
        fun count(sql: String, args: Array<String>): Int =
            db.rawQuery(sql, args).use { it.moveToFirst(); it.getInt(0) }
        val dim = count("SELECT COUNT(*) FROM chunks WHERE model=? AND dim>0", arrayOf(model)).let {
            if (it > 0) db.rawQuery("SELECT dim FROM chunks WHERE model=? AND dim>0 LIMIT 1", arrayOf(model))
                .use { c -> c.moveToFirst(); c.getInt(0) } else 0
        }
        val chunks = count("SELECT COUNT(*) FROM chunks WHERE model=? AND dim>0", arrayOf(model))
        val pending = count(
            "SELECT COUNT(*) FROM documents d LEFT JOIN chunks c ON c.kind=d.kind AND c.doc_id=d.id AND c.model=? WHERE c.kind IS NULL",
            arrayOf(model))
        return EmbedStatus(model, dim, chunks, pending, 0)
    }

    /** Cosine top-k over stored vectors for one model. Returns (kind,id,score) ranked. */
    @Synchronized fun vectorSearch(queryVec: FloatArray, model: String, limit: Int = 30): List<Triple<String, String, Float>> {
        ensureSchema()
        // Best chunk per document; documents may repeat per chunk, keep max.
        val best = HashMap<String, Float>()
        db.rawQuery("SELECT kind,doc_id,vector FROM chunks WHERE model=? AND dim>0", arrayOf(model)).use { c ->
            while (c.moveToNext()) {
                val blob = c.getBlob(2) ?: continue
                if (blob.size / 4 != queryVec.size) continue
                val score = VectorCodec.cosine(queryVec, VectorCodec.decode(blob))
                val key = "${c.getString(0)}:${c.getString(1)}"
                best.merge(key, score) { a, b -> if (a >= b) a else b }
            }
        }
        return best.entries.sortedByDescending { it.value }.take(limit)
            .map { Triple(it.key.substringBefore(':'), it.key.substringAfter(':'), it.value) }
    }

    /** Wipe all vectors for a model (model changed / rebuild). */
    @Synchronized fun clearEmbeddings(model: String) {
        ensureSchema()
        db.execSQL("DELETE FROM chunks WHERE model=?", arrayOf(model))
    }

    /**
     * Hybrid search: keyword (FTS5/LIKE) and semantic rankings fused with RRF.
     * Falls back to keyword-only when the API/embedding is unavailable —
     * offline keyword search must keep working (design requirement).
     */
    fun hybridQuery(rawQuery: String, api: EmbedApi?, limit: Int = 100): List<SearchHit> {
        val keyword = query(rawQuery, limit)
        if (api == null) return keyword
        val semantic = runCatching {
            val vec = api.embed(listOf(rawQuery.trim())).first()
            vectorSearch(vec, api.modelId)
        }.getOrDefault(emptyList())
        if (semantic.isEmpty()) return keyword
        val keyRank = keyword.map { "${it.kind}:${it.file.absolutePath}:${it.conversationId}" }
        val semRank = semantic.map { "${it.first}:${it.second}" }
        val fused = Rrf.fuse(listOf(keyRank, semRank)).take(limit * 2).toMap()
        // Materialize fused order: keyword hits carry full data; semantic-only
        // hits are filled from documents.
        val byKey = HashMap<String, SearchHit>()
        keyword.forEach { byKey["${it.kind}:${it.file.absolutePath}:${it.conversationId}"] = it }
        val result = mutableListOf<SearchHit>()
        for ((key, _) in fused) {
            val hit = byKey[key] ?: semanticHit(key, rawQuery) ?: continue
            result.add(hit)
            if (result.size >= limit) break
        }
        // Keyword-only results that fell out of the fusion list still surface.
        for (h in keyword) {
            if (result.size >= limit) break
            val k = "${h.kind}:${h.file.absolutePath}:${h.conversationId}"
            if (fused.containsKey(k) && byKey.values.none { it === h }) continue
            if (result.none { it.kind == h.kind && it.file == h.file && it.conversationId == h.conversationId }) result.add(h)
        }
        return result
    }

    /** Raw document lookup used by retrieval-based category injection. */
    @Synchronized fun rawQueryDocument(kind: String, id: String): Doc? =
        db.rawQuery("SELECT kind,id,title,category,body,payload,src,updated FROM documents WHERE kind=? AND id=?",
            arrayOf(kind, id)).use { c ->
            if (!c.moveToFirst()) null else Doc(c.getString(0), c.getString(1), c.getString(2),
                c.getString(3), c.getString(4) ?: "", c.getString(5) ?: "", c.getString(6) ?: "", c.getLong(7))
        }

    /** Note titles grouped by category path, for category-injection samples. */
    @Synchronized fun titlesByCategory(limitPerCategory: Int = 8): Map<String, List<String>> {
        ensureSchema()
        val out = LinkedHashMap<String, MutableList<String>>()
        db.rawQuery("SELECT category,title FROM documents WHERE kind='note' ORDER BY updated DESC", null).use { c ->
            while (c.moveToNext()) {
                val cat = c.getString(0) ?: ""
                if (cat.isBlank()) continue
                val list = out.getOrPut(cat) { mutableListOf() }
                if (list.size < limitPerCategory) list.add(c.getString(1))
            }
        }
        return out
    }

    private fun semanticHit(key: String, rawQuery: String): SearchHit? {
        val kind = key.substringBefore(':'); val id = key.substringAfter(':')
        return db.rawQuery(
            "SELECT title,category,body,payload,src,updated FROM documents WHERE kind=? AND id=?",
            arrayOf(kind, id)
        ).use { c ->
            if (!c.moveToFirst()) return null
            val body = c.getString(2) ?: ""
            val w = SearchLogic.snippets(body, rawQuery).firstOrNull()
            SearchHit(kind, File(c.getString(3) ?: ""), c.getString(0), c.getString(1), c.getLong(5),
                w?.first ?: c.getString(0), w?.second ?: 0, w?.third ?: 0, 0,
                if (kind == "conv") id else (c.getString(4) ?: ""), "", 0)
        }
    }

    data class Doc(
        val kind: String,        // "note" | "conv"
        val id: String,          // job id / file identity (notes) | conversation id
        val title: String,
        val category: String,    // note relative dir | conversation title
        val body: String,        // searchable raw text
        val payload: String,     // note file absolute path | ""
        val src: String,         // conversation id (notes) | ""
        val updatedAt: Long
    )

    data class Status(val indexedNotes: Int, val indexedConversations: Int,
        val fts: Boolean, val lastIndex: Long, val dirty: Boolean)

    @Synchronized private fun ensureSchema() {
        if (schemaReady) return
        ftsMode = runCatching {
            db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS probe_fts USING fts5(x)")
            db.execSQL("DROP TABLE probe_fts")
            true
        }.getOrDefault(false)
        db.execSQL("CREATE TABLE IF NOT EXISTS documents(" +
            "kind TEXT NOT NULL,id TEXT NOT NULL," +
            "title TEXT NOT NULL,category TEXT NOT NULL," +
            "body TEXT NOT NULL,payload TEXT NOT NULL," +
            "src TEXT NOT NULL DEFAULT '',updated INTEGER NOT NULL," +
            "size INTEGER NOT NULL DEFAULT 0," +
            "PRIMARY KEY(kind,id))")
        if (ftsMode) {
            db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS doc_fts USING fts5(" +
                "title_t,body_t,category_t,kind UNINDEXED,id UNINDEXED)")
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS meta(key TEXT PRIMARY KEY,value TEXT NOT NULL)")
        // Vector layer: one row per (document, chunk, model). dim=0 marks an
        // empty/failed marker so the document is not retried forever.
        db.execSQL("CREATE TABLE IF NOT EXISTS chunks(" +
            "kind TEXT NOT NULL,doc_id TEXT NOT NULL,seq INTEGER NOT NULL," +
            "model TEXT NOT NULL,dim INTEGER NOT NULL,vector BLOB NOT NULL," +
            "input_hash TEXT NOT NULL DEFAULT '',PRIMARY KEY(kind,doc_id,model,seq))")
        schemaReady = true
    }

    private fun meta(key: String): String? = db.rawQuery("SELECT value FROM meta WHERE key=?", arrayOf(key))
        .use { if (it.moveToFirst()) it.getString(0) else null }
    private fun metaPut(key: String, value: String) =
        db.execSQL("INSERT OR REPLACE INTO meta(key,value) VALUES(?,?)", arrayOf(key, value))

    @Synchronized private fun upsert(doc: Doc) {
        if (ftsMode) db.execSQL("DELETE FROM doc_fts WHERE kind=? AND id=?", arrayOf(doc.kind, doc.id))
        db.execSQL("INSERT OR REPLACE INTO documents(kind,id,title,category,body,payload,src,updated,size) VALUES(?,?,?,?,?,?,?,?,?)",
            arrayOf(doc.kind, doc.id, doc.title.take(300), doc.category.take(300),
                doc.body.take(500000), doc.payload, doc.src, doc.updatedAt, doc.body.length))
        if (ftsMode) db.execSQL("INSERT INTO doc_fts(title_t,body_t,category_t,kind,id) VALUES(?,?,?,?,?)",
            arrayOf(SearchLogic.tokenize(doc.title.take(300)), SearchLogic.tokenize(doc.body.take(500000)),
                SearchLogic.tokenize(doc.category.take(300)), doc.kind, doc.id))
    }

    @Synchronized private fun remove(kind: String, id: String) {
        if (ftsMode) db.execSQL("DELETE FROM doc_fts WHERE kind=? AND id=?", arrayOf(kind, id))
        db.execSQL("DELETE FROM documents WHERE kind=? AND id=?", arrayOf(kind, id))
    }

    /** Mark stale so the next ensureIndexed() refreshes before searching. */
    fun markDirty() { dirty = true }

    /**
     * Incremental sync: notes by (mtime,size), conversations by updated stamp.
     * Orphan note rows (file deleted) are removed. Safe to run repeatedly.
     */
    @Synchronized fun ensureIndexed(notes: NotesRepo, chat: ChatStore): Status {
        ensureSchema()
        // Always run the stamp comparison — it is cheap (mtime/size/updated per row)
        // and short-circuiting on a "dirty" flag misses writes that happen after
        // the last sync (found by the on-device incremental sync test).
        val t0 = System.currentTimeMillis()
        // --- notes ---
        val files = notes.allFiles()
        val known = HashMap<String, Pair<Long, Int>>() // payload -> (updated, size)
        db.rawQuery("SELECT payload,updated,size FROM documents WHERE kind='note'", null).use { c ->
            while (c.moveToNext()) known[c.getString(0)] = c.getLong(1) to c.getInt(2)
        }
        val jobSource = HashMap<String, String>() // note path -> conversation id
        chat.jobs().forEach { j -> if (j.notePath.isNotEmpty()) jobSource[j.notePath] = j.conversationId }
        val seenPayloads = HashSet<String>()
        for (f in files) {
            val path = f.canonicalPath
            seenPayloads.add(path)
            val stamp = f.lastModified().coerceAtLeast(1L)
            val size = f.length().toInt()
            if (known[path] == stamp to size) continue
            val text = runCatching { notes.readNote(f) }.getOrDefault("")
            val name = f.nameWithoutExtension
            val jobId = name.substringAfterLast("--", "")
            val hasId = jobId.length == 64 && jobId.all { it in '0'..'9' || it in 'a'..'f' }
            // Strip "--<64hex>" fully (66 chars), not 65 — off-by-one left a trailing '-'.
            val title = if (hasId) name.dropLast(66).ifEmpty { name } else name
            upsert(Doc("note", if (hasId) jobId else "file:$path", title,
                f.parentFile?.relativeTo(notes.root)?.path ?: "", text, path,
                jobSource[path] ?: "", stamp))
        }
        known.keys.filter { it !in seenPayloads }.forEach { payload ->
            db.rawQuery("SELECT id FROM documents WHERE kind='note' AND payload=?", arrayOf(payload)).use { c ->
                if (c.moveToFirst()) remove("note", c.getString(0))
            }
        }
        // --- conversations ---
        val knownConvs = HashMap<String, Long>()
        db.rawQuery("SELECT id,updated FROM documents WHERE kind='conv'", null).use { c ->
            while (c.moveToNext()) knownConvs[c.getString(0)] = c.getLong(1)
        }
        val liveConvs = HashSet<String>()
        for (conv in chat.conversations()) {
            liveConvs.add(conv.id)
            if (knownConvs[conv.id] == conv.updatedAt) continue
            val body = buildString {
                chat.allMessages(conv.id).forEach { m ->
                    append(m.text.replace(Regex("[\\r\\n\\t]+"), " ")); append(' ')
                }
            }
            if (body.isBlank()) remove("conv", conv.id)
            else upsert(Doc("conv", conv.id, conv.title, conv.title, body, "", "", conv.updatedAt))
        }
        knownConvs.keys.filter { it !in liveConvs }.forEach { remove("conv", it) }
        dirty = false
        metaPut("lastIndex", System.currentTimeMillis().toString())
        return status().also {
            android.util.Log.d("SearchIndex", "sync ${System.currentTimeMillis() - t0}ms notes=${it.indexedNotes} convs=${it.indexedConversations}")
        }
    }

    @Synchronized fun status(): Status {
        ensureSchema()
        fun Cursor.count(): Int { moveToFirst(); return getInt(0) }
        val notes = db.rawQuery("SELECT COUNT(*) FROM documents WHERE kind='note'", null).use { it.count() }
        val convs = db.rawQuery("SELECT COUNT(*) FROM documents WHERE kind='conv'", null).use { it.count() }
        return Status(notes, convs, ftsMode, meta("lastIndex")?.toLong() ?: 0L, dirty)
    }

    /** Full rebuild: drop FTS content and re-scan everything. */
    @Synchronized fun rebuild(notes: NotesRepo, chat: ChatStore): Status {
        ensureSchema()
        db.beginTransaction()
        try {
            if (ftsMode) db.execSQL("DELETE FROM doc_fts")
            db.execSQL("DELETE FROM documents")
            dirty = true
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        return ensureIndexed(notes, chat)
    }

    fun query(rawQuery: String, limit: Int = 100): List<SearchHit> {
        ensureSchema()
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty() || trimmed.length > 200) return emptyList()
        val match = SearchLogic.matchQuery(trimmed) ?: return emptyList()
        val rows: List<Array<Any>> = if (ftsMode) {
            db.rawQuery(
                "SELECT d.kind,d.id,d.title,d.category,d.body,d.payload,d.src,d.updated,bm25(doc_fts) AS rank " +
                    "FROM doc_fts JOIN documents d ON d.kind=doc_fts.kind AND d.id=doc_fts.id " +
                    "WHERE doc_fts MATCH ? ORDER BY rank LIMIT ?",
                arrayOf(match, (limit * 2).toString())
            ).use { collect(it) }
        } else {
            val like = SearchLogic.likePattern(trimmed)
            db.rawQuery(
                "SELECT kind,id,title,category,body,payload,src,updated,0 FROM documents " +
                    "WHERE title LIKE ? ESCAPE '\\' OR body LIKE ? ESCAPE '\\' OR category LIKE ? ESCAPE '\\' " +
                    "ORDER BY updated DESC LIMIT ?",
                arrayOf(like, like, like, (limit * 2).toString())
            ).use { collect(it) }
        }
        // Same (kind,id) may match in several columns; keep the best entry, note kind wins ties.
        val merged = LinkedHashMap<String, SearchHit>()
        for (r in rows) {
            val key = "${r[0]}:${r[1]}"
            val hit = toHit(r, trimmed)
            val prev = merged[key]
            if (prev == null || hit.score > prev.score) merged[key] = hit
        }
        // A conversation already covered by one of its notes collapses into the note card.
        val noteConvs = merged.values.filter { it.kind == "note" }.map { it.conversationId }.toSet()
        return merged.values.filter { it.kind == "note" || it.conversationId !in noteConvs }.take(limit)
    }

    private fun collect(c: Cursor): List<Array<Any>> = buildList {
        while (c.moveToNext()) add(arrayOf(c.getString(0), c.getString(1), c.getString(2), c.getString(3),
            c.getString(4) ?: "", c.getString(5) ?: "", c.getString(6) ?: "", c.getLong(7), c.getDouble(8)))
    }

    private fun toHit(r: Array<Any>, trimmed: String): SearchHit {
        val kind = r[0] as String
        val title = r[2] as String
        val body = r[4] as String
        val windows = SearchLogic.snippets(body, trimmed)
        val first = windows.firstOrNull()
        // bm25(): more negative = better; normalize to a descending score.
        val score = (1000000 - ((r[8] as Double).absoluteValueSafe() * 1000).toInt()).coerceAtLeast(0)
        return SearchHit(kind, File(r[5] as String), title, r[3] as String, r[7] as Long,
            first?.first ?: title, first?.second ?: 0, first?.third ?: 0,
            score, if (kind == "conv") r[1] as String else r[6] as String,
            if (kind == "conv") title else "",
            (windows.size - 1).coerceAtLeast(0))
    }

    private fun Double.absoluteValueSafe(): Double = if (this < 0) -this else this
}
