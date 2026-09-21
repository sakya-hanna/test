package com.willam.chatnotes

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

/** All raw messages, branches, immutable summary inputs and retry checkpoints live here. */
class ChatStore(context: Context, name: String = "chatnotes.db") : SQLiteOpenHelper(context, name, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE conversations(id TEXT PRIMARY KEY,title TEXT NOT NULL DEFAULT '',url TEXT NOT NULL DEFAULT '',updated INTEGER NOT NULL,revision INTEGER NOT NULL DEFAULT 0,leaf TEXT NOT NULL DEFAULT '',coverage TEXT NOT NULL DEFAULT 'live')")
        db.execSQL("CREATE TABLE messages(cid TEXT NOT NULL,id TEXT NOT NULL,parent TEXT NOT NULL,role TEXT NOT NULL,body TEXT NOT NULL,status TEXT NOT NULL,source TEXT NOT NULL,attachments INTEGER NOT NULL,created INTEGER NOT NULL,PRIMARY KEY(cid,id))")
        db.execSQL("CREATE INDEX messages_order ON messages(cid,created,id)")
        db.execSQL("CREATE TABLE aliases(old TEXT PRIMARY KEY,target TEXT NOT NULL)")
        db.execSQL("CREATE TABLE requests(id TEXT PRIMARY KEY,cid TEXT NOT NULL,owner TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE TABLE jobs(id TEXT PRIMARY KEY,cid TEXT NOT NULL,snapshot TEXT NOT NULL,state TEXT NOT NULL,draft TEXT NOT NULL DEFAULT '',note_path TEXT NOT NULL DEFAULT '',error TEXT NOT NULL DEFAULT '',created INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE summary_parts(job TEXT NOT NULL,part TEXT NOT NULL,result TEXT NOT NULL,PRIMARY KEY(job,part))")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Never drop user content. Future schema versions must add explicit migrations here.
        error("尚未提供数据库迁移：$oldVersion → $newVersion")
    }
    private fun validId(id: String): String {
        require(id.matches(Regex("[A-Za-z0-9._:-]{1,200}"))) { "消息或会话 ID 不合法" }
        return id
    }
    @Synchronized fun resolve(id: String): String {
        var current = id
        repeat(16) {
            val next = readableDatabase.rawQuery("SELECT target FROM aliases WHERE old=?", arrayOf(current)).use {
                if (it.moveToFirst()) it.getString(0) else null
            } ?: return current
            current = next
        }
        error("会话映射异常")
    }
    private fun ensure(db: SQLiteDatabase, id: String) {
        db.execSQL("INSERT OR IGNORE INTO conversations(id,updated) VALUES(?,?)", arrayOf(id, System.currentTimeMillis()))
    }
    private fun bump(db: SQLiteDatabase, id: String) {
        db.execSQL("UPDATE conversations SET revision=revision+1,updated=? WHERE id=?", arrayOf(System.currentTimeMillis(), id))
    }
    private fun Cursor.message() = ChatMessage(getString(0), getString(1), getString(2), getString(3),
        getString(4), getString(5), getInt(6) != 0, getLong(7))
    private fun message(db: SQLiteDatabase, cid: String, id: String): ChatMessage? = db.rawQuery(
        "SELECT id,parent,role,body,status,source,attachments,created FROM messages WHERE cid=? AND id=?", arrayOf(cid, id)
    ).use { if (it.moveToFirst()) it.message() else null }
    @Synchronized fun applyEvent(j: JSONObject): String {
        val db = writableDatabase
        db.beginTransaction()
        try {
            if (j.text("type") == "remap") {
                val from = validId(j.getString("from")); val target = resolve(validId(j.getString("to")))
                require(from.startsWith("local:") && !target.startsWith("local:")) { "无效会话映射" }
                val old = resolve(from)
                require(old == from || old == target) { "临时会话已关联其他会话，请重新加载核对" }
                if (old != target) {
                    ensure(db, target)
                    val leaf = db.rawQuery("SELECT leaf FROM conversations WHERE id=?", arrayOf(old)).use { if (it.moveToFirst()) it.getString(0) else "" }
                    db.execSQL("INSERT OR IGNORE INTO messages SELECT ?,id,parent,role,body,status,source,attachments,created FROM messages WHERE cid=?", arrayOf(target, old))
                    db.execSQL("UPDATE requests SET cid=? WHERE cid=?", arrayOf(target, old))
                    db.execSQL("UPDATE jobs SET cid=? WHERE cid=?", arrayOf(target, old))
                    db.execSQL("UPDATE conversations SET leaf=? WHERE id=? AND leaf=''", arrayOf(leaf, target))
                    db.delete("messages", "cid=?", arrayOf(old)); db.delete("conversations", "id=?", arrayOf(old))
                    db.execSQL("INSERT OR REPLACE INTO aliases(old,target) VALUES(?,?)", arrayOf(from, target))
                    bump(db, target)
                }
                db.setTransactionSuccessful(); return target
            }
            val cid = resolve(validId(j.getString("conversationId")))
            ensure(db, cid)
            when (j.text("type")) {
                "request" -> {
                    val token = validId(j.getString("requestId"))
                    if (j.text("state") == "start") db.execSQL("INSERT OR REPLACE INTO requests(id,cid,owner) VALUES(?,?,?)", arrayOf(token, cid, j.text("owner")))
                    else db.delete("requests", "id=?", arrayOf(token))
                }
                "active", "conversation" -> {
                    val values = ContentValues()
                    if (j.has("title") && j.text("title").isNotBlank()) values.put("title", j.text("title").take(200))
                    val url = j.text("url")
                    if (url.startsWith("https://chatgpt.com/")) values.put("url", url.take(1000))
                    val leaf = j.text("activeLeaf")
                    if (leaf.isNotEmpty() && message(db, cid, leaf) != null) values.put("leaf", leaf)
                    if (j.text("coverage") == "history") values.put("coverage", "history")
                    if (values.size() > 0) { db.update("conversations", values, "id=?", arrayOf(cid)); bump(db, cid) }
                }
                "message" -> {
                    val id = validId(j.getString("msgId")); val parent = j.text("parentId").also { if (it.isNotEmpty()) validId(it) }
                    val role = j.getString("role"); val body = j.getString("text"); var status = j.getString("status")
                    val source = j.text("source", "network")
                    require(role in setOf("user", "assistant") && status in setOf("pending", "streaming", "complete", "partial", "failed"))
                    require(source in setOf("network", "history", "dom") && body.length <= 200000 && body.isNotBlank())
                    val old = message(db, cid, id)
                    if (old != null && source == "dom" && old.source != "dom") {
                        db.setTransactionSuccessful(); return cid
                    }
                    if (old?.status == "complete" && old.source == "network" && source == "history" && status != "complete") {
                        db.setTransactionSuccessful(); return cid
                    }
                    if (old != null && old.status == "complete" && body == old.text && !j.optBoolean("uncertain")) status = "complete"
                    val created = if (old != null) old.createdAt else j.optLong("createdAt", System.currentTimeMillis()).coerceAtLeast(1)
                    val next = ChatMessage(id, parent.ifEmpty { old?.parentId ?: "" }, role, body, status, source,
                        j.optBoolean("attachments"), created)
                    if (next != old) {
                        val values = ContentValues().apply {
                            put("cid", cid); put("id", id); put("parent", next.parentId); put("role", role); put("body", body)
                            put("status", status); put("source", source); put("attachments", if (next.attachments) 1 else 0); put("created", created)
                        }
                        db.insertWithOnConflict("messages", null, values, SQLiteDatabase.CONFLICT_REPLACE).also { check(it != -1L) }
                        bump(db, cid)
                    }
                    if (j.optBoolean("select")) db.execSQL("UPDATE conversations SET leaf=? WHERE id=?", arrayOf(id, cid))
                }
            }
            db.setTransactionSuccessful(); return cid
        } finally { db.endTransaction() }
    }
    /** Called once per process, before new WebView events. Never on each Activity recreation. */
    @Synchronized fun recoverInterruptedCapture() {
        writableDatabase.execSQL("UPDATE messages SET status='partial' WHERE status IN ('pending','streaming')")
        writableDatabase.delete("requests", null, null)
    }
    @Synchronized fun closeCapture(owner: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val affected = db.rawQuery("SELECT DISTINCT cid FROM requests WHERE owner=?", arrayOf(owner)).use { c ->
                buildList { while (c.moveToNext()) add(c.getString(0)) }
            }
            db.delete("requests", "owner=?", arrayOf(owner))
            affected.forEach { cid ->
                db.execSQL("UPDATE messages SET status='partial' WHERE cid=? AND status IN ('pending','streaming') AND NOT EXISTS(SELECT 1 FROM requests WHERE cid=?)", arrayOf(cid, cid))
                bump(db, cid)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    @Synchronized fun conversations(): List<ConversationInfo> = readableDatabase.rawQuery(
        // Only conversations with captured messages are listed; login/navigation
        // shells (0 messages) were polluting the picker (user report 2026-09-18).
        "SELECT c.id,c.title,c.url,COUNT(m.id),c.updated FROM conversations c JOIN messages m ON m.cid=c.id AND m.status!='failed' AND m.role='user' GROUP BY c.id ORDER BY c.updated DESC", null
    ).use { c -> buildList { while (c.moveToNext()) add(ConversationInfo(c.getString(0), c.getString(1).ifBlank { "未命名会话" }, c.getString(2), c.getInt(3), c.getLong(4))) } }
    /** Drop placeholder-only messages left by the old thinking-frame bug. */
    @Synchronized fun prunePlaceholderMessages(): Int {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val deleted = db.delete("messages",
                "role='assistant' AND TRIM(REPLACE(body,'[附件或非文本内容未采集，请在原平台查看]',''))=''", null)
            db.setTransactionSuccessful()
            return deleted
        } finally { db.endTransaction() }
    }

    /** Drop empty shells (no messages, not referenced by aliases/jobs). */
    @Synchronized fun pruneEmptyConversations(): Int {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val deleted = db.delete("conversations",
                "id NOT IN (SELECT DISTINCT cid FROM messages) AND " +
                    "id NOT IN (SELECT target FROM aliases) AND " +
                    "id NOT IN (SELECT cid FROM jobs)", null)
            db.setTransactionSuccessful()
            return deleted
        } finally { db.endTransaction() }
    }
    @Synchronized fun count(cid: String): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM messages WHERE cid=?", arrayOf(resolve(cid)))
        .use { it.moveToFirst(); it.getInt(0) }
    /** Q&A rounds: each user message with a following assistant reply counts as one round. */
    @Synchronized fun rounds(cid: String): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM messages WHERE cid=? AND role='user' AND status!='failed'", arrayOf(resolve(cid))
    ).use { it.moveToFirst(); it.getInt(0) }
    @Synchronized fun allMessages(cid: String): List<ChatMessage> = readableDatabase.rawQuery(
        "SELECT id,parent,role,body,status,source,attachments,created FROM messages WHERE cid=? ORDER BY created,id", arrayOf(resolve(cid))
    ).use { c -> buildList { while (c.moveToNext()) add(c.message()) } }
    @Synchronized fun snapshot(conversationId: String): ConversationSnapshot {
        val cid = resolve(conversationId); val db = readableDatabase
        val busy = db.rawQuery("SELECT COUNT(*) FROM requests WHERE cid=?", arrayOf(cid)).use { it.moveToFirst(); it.getInt(0) }
        check(busy == 0) { "当前会话仍在生成回复，请完成或停止生成后再总结。原文正在自动保存。" }
        val meta = db.rawQuery("SELECT title,url,leaf,coverage,revision FROM conversations WHERE id=?", arrayOf(cid)).use {
            check(it.moveToFirst()) { "还没有保存的对话" }
            arrayOf(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getLong(4).toString())
        }
        val all = allMessages(cid); val map = all.associateBy { it.id }; val warnings = mutableListOf<String>()
        check(all.isNotEmpty()) { "还没有保存的对话" }
        var current = meta[2].ifBlank { all.last().id }; val chain = mutableListOf<ChatMessage>(); val visited = mutableSetOf<String>()
        while (current.isNotEmpty()) {
            check(visited.add(current)) { "消息父子关系形成循环，请重新加载原平台会话" }
            val m = map[current]
            if (m == null) { warnings.add("较早的上下文未采集，当前原文可能不完整"); break }
            chain.add(m); current = m.parentId
        }
        chain.reverse()
        if (meta[3] != "history") warnings.add("尚未通过历史会话核对全部上下文")
        if (chain.any { it.status != "complete" }) warnings.add("存在中断、未确认或发送失败的消息")
        if (chain.any { it.attachments }) warnings.add("附件或非文本内容未包含在原文中")
        if (chain.any { it.source == "dom" }) warnings.add("页面补采集只覆盖已加载的可见文字")
        val selected = chain.filter { it.status != "failed" }
        check(selected.any { it.role == "assistant" }) { "当前分支还没有可总结的助手回复，请补采集后重试" }
        check(selected.none { it.status in setOf("pending", "streaming") }) { "回复尚未完成，请稍后再总结" }
        // Incremental summaries: messages already archived by a SAVED job are
        // excluded, so the next note covers only what is new since then.
        val covered = summarizedMessageIds(cid)
        val incremental = selected.filter { it.id !in covered }
        if (covered.isNotEmpty() && incremental.none { it.role == "user" }) {
            error("上次归档后没有新的问答，无需重复总结")
        }
        val scope = if (covered.isEmpty()) selected else incremental
        return ConversationSnapshot(cid, meta[0].ifBlank { "未命名会话" }, meta[1], scope, warnings.distinct(), meta[4].toLong())
    }
    /** Message IDs already covered by a successfully saved summary job (from snapshots). */
    private fun summarizedMessageIds(cid: String): Set<String> {
        val ids = mutableSetOf<String>()
        val snapshots = mutableListOf<String>()
        readableDatabase.rawQuery("SELECT snapshot FROM jobs WHERE cid=? AND state='saved'", arrayOf(cid)).use { c ->
            while (c.moveToNext()) snapshots.add(c.getString(0))
        }
        for (raw in snapshots) {
            runCatching {
                val a = org.json.JSONObject(raw).optJSONArray("messages") ?: return@runCatching
                for (i in 0 until a.length()) ids.add(a.getJSONObject(i).getString("id"))
            }
        }
        return ids
    }
    @Synchronized fun createJob(snapshot: ConversationSnapshot, config: ApiConfig? = null, forceNew: Boolean = false): SummaryJob {
        val id = if (forceNew) sha256(snapshot.fingerprint + "\n" + System.currentTimeMillis())
        else if (config == null) snapshot.fingerprint else sha256(snapshot.fingerprint + "\n" + config.baseUrl + "\n" + config.model)
        val input = snapshot.json()
        if (config != null) input.put("api_base", config.baseUrl).put("api_model", config.model)
        val values = ContentValues().apply {
            put("id", id); put("cid", snapshot.id); put("snapshot", input.toString())
            put("state", "queued"); put("created", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("jobs", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        return requireNotNull(job(id))
    }
    @Synchronized fun job(id: String): SummaryJob? = readableDatabase.rawQuery(
        "SELECT id,cid,snapshot,state,draft,note_path,error FROM jobs WHERE id=?", arrayOf(id)
    ).use { if (it.moveToFirst()) SummaryJob(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5), it.getString(6)) else null }
    @Synchronized fun jobs(): List<SummaryJob> = readableDatabase.rawQuery("SELECT id FROM jobs ORDER BY created DESC", null)
        .use { c -> buildList { while (c.moveToNext()) job(c.getString(0))?.let { add(it) } } }
    @Synchronized fun updateJob(id: String, state: String, draft: String? = null, note: String? = null, error: String = "") {
        val values = ContentValues().apply {
            put("state", state); put("error", error.take(500))
            if (draft != null) put("draft", draft)
            if (note != null) put("note_path", note)
        }
        writableDatabase.update("jobs", values, "id=?", arrayOf(id))
    }
    @Synchronized fun part(job: String, key: String): String? = readableDatabase.rawQuery(
        "SELECT result FROM summary_parts WHERE job=? AND part=?", arrayOf(job, key)
    ).use { if (it.moveToFirst()) it.getString(0) else null }
    @Synchronized fun savePart(job: String, key: String, result: String) {
        writableDatabase.execSQL("INSERT OR REPLACE INTO summary_parts(job,part,result) VALUES(?,?,?)", arrayOf(job, key, result))
    }
    @Synchronized fun exportJson(): String = JSONObject().put("format", "chatnotes-1").put("exportedAt", System.currentTimeMillis())
        .put("conversations", JSONArray().also { a -> conversations().forEach { c -> a.put(JSONObject()
            .put("id", c.id).put("title", c.title).put("url", c.url)
            .put("messages", JSONArray().also { m -> allMessages(c.id).forEach { m.put(it.json()) } })) } })
        .put("jobs", JSONArray().also { a -> jobs().forEach { j -> a.put(JSONObject()
            .put("id", j.id).put("conversationId", j.conversationId).put("state", j.state)
            .put("snapshot", JSONObject(j.snapshot)).put("draft", j.draft).put("error", j.error)) } }).toString(2)
}
