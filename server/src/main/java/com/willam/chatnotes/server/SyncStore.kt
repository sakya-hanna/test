package com.willam.chatnotes.server

import com.willam.chatnotes.shared.sync.AppConfigDto
import java.sql.Connection
import java.sql.DriverManager

/**
 * 服务器存储：SQLite（嵌入，默认）或 PostgreSQL（外部实例）。
 * 表：
 *   docs      文档主表（note/summary_part/conv），content_hash 幂等，soft deleted_at
 *   changes   变更日志（单调递增 seq），Pull 光标即此 seq
 *   devices   设备注册与心跳
 *
 * @param dbUrl jdbc:sqlite:/path/x.db 或 jdbc:postgresql://host:port/db?user=&password=
 */
class SyncStore(dbUrl: String) : AutoCloseable {

    private val isPg = dbUrl.startsWith("jdbc:postgresql")

    init {
        // fat jar 合并 service 文件时 PG 驱动注册可能被覆盖，显式加载兜底
        if (isPg) Class.forName("org.postgresql.Driver")
    }

    private val conn: Connection = DriverManager.getConnection(dbUrl).apply {
        autoCommit = true
    }

    /** 方言差异：自增主键 DDL；PG 的 BIGSERED 已是 64 位 */
    private val autoIncPk = if (isPg) "BIGSERIAL PRIMARY KEY" else "INTEGER PRIMARY KEY AUTOINCREMENT"

    init {
        conn.createStatement().use { st ->
            if (!isPg) {
                st.execute("PRAGMA journal_mode=WAL")
                st.execute("PRAGMA synchronous=NORMAL")
            }
            st.execute(
                """CREATE TABLE IF NOT EXISTS docs(
                kind TEXT NOT NULL,
                doc_id TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT '',
                title TEXT NOT NULL DEFAULT '',
                content TEXT NOT NULL DEFAULT '',
                updated_at BIGINT NOT NULL,
                device_id TEXT NOT NULL,
                content_hash TEXT NOT NULL,
                deleted_at BIGINT NOT NULL DEFAULT 0,
                PRIMARY KEY(kind, doc_id)
            )"""
            )
            st.execute(
                """CREATE TABLE IF NOT EXISTS changes(
                seq $autoIncPk,
                kind TEXT NOT NULL,
                doc_id TEXT NOT NULL,
                deleted INTEGER NOT NULL DEFAULT 0
            )"""
            )
            st.execute(
                """CREATE TABLE IF NOT EXISTS devices(
                device_id TEXT PRIMARY KEY,
                first_seen BIGINT NOT NULL,
                last_seen BIGINT NOT NULL
            )"""
            )
        }
    }

    /** 返回 "new" | "same" | "older"（服务器已有同 ID 且更新/相同内容） */
    @Synchronized
    fun upsert(
        kind: String, docId: String, category: String, title: String,
        content: String, updatedAt: Long, deviceId: String, contentHash: String,
    ): String {
        val existing = queryDoc(kind, docId)
        if (existing != null) {
            // 完全相同才算幂等命中；同内容但标题/分类不同 = 改名/移动，是合法更新（P1 起支持）
            if (existing.deletedAt == 0L && existing.contentHash == contentHash && existing.title == title && existing.category == category) return "same"
            if (existing.updatedAt >= updatedAt) return "older"
        }
        // 方言分支：SQLite 用 INSERT OR REPLACE；PG 用 ON CONFLICT DO UPDATE
        if (isPg) {
            conn.prepareStatement(
                """INSERT INTO docs(kind,doc_id,category,title,content,updated_at,device_id,content_hash,deleted_at)
                   VALUES(?,?,?,?,?,?,?,?,0)
                   ON CONFLICT (kind, doc_id) DO UPDATE SET
                     category=EXCLUDED.category, title=EXCLUDED.title, content=EXCLUDED.content,
                     updated_at=EXCLUDED.updated_at, device_id=EXCLUDED.device_id,
                     content_hash=EXCLUDED.content_hash, deleted_at=0"""
            ).use { ps ->
                ps.setString(1, kind); ps.setString(2, docId); ps.setString(3, category)
                ps.setString(4, title); ps.setString(5, content); ps.setLong(6, updatedAt)
                ps.setString(7, deviceId); ps.setString(8, contentHash)
                ps.executeUpdate()
            }
        } else {
            conn.prepareStatement(
                """INSERT OR REPLACE INTO docs(kind,doc_id,category,title,content,updated_at,device_id,content_hash,deleted_at)
                   VALUES(?,?,?,?,?,?,?,?,0)"""
            ).use { ps ->
                ps.setString(1, kind); ps.setString(2, docId); ps.setString(3, category)
                ps.setString(4, title); ps.setString(5, content); ps.setLong(6, updatedAt)
                ps.setString(7, deviceId); ps.setString(8, contentHash)
                ps.executeUpdate()
            }
        }
        logChange(kind, docId, deleted = false)
        return "new"
    }

    /** 配置备份表（单行）：LLM/embed 两段配置整存整取 */
    init {
        conn.createStatement().use { st ->
            st.execute(
                """CREATE TABLE IF NOT EXISTS app_config(
                id INTEGER PRIMARY KEY CHECK (id = 1),
                llm_base_url TEXT NOT NULL DEFAULT '',
                llm_model TEXT NOT NULL DEFAULT '',
                llm_api_key TEXT NOT NULL DEFAULT '',
                embed_base_url TEXT NOT NULL DEFAULT '',
                embed_model TEXT NOT NULL DEFAULT '',
                embed_api_key TEXT NOT NULL DEFAULT '',
                updated_at BIGINT NOT NULL DEFAULT 0
            )"""
            )
        }
    }

    /** 读配置备份；从未保存过时返回全空 + updatedAt=0 */
    @Synchronized
    fun getConfig(): AppConfigDto = conn.createStatement().use { st ->
        st.executeQuery("SELECT llm_base_url, llm_model, llm_api_key, embed_base_url, embed_model, embed_api_key, updated_at FROM app_config WHERE id = 1")
            .use { rs ->
                if (rs.next()) AppConfigDto(
                    llmBaseUrl = rs.getString(1) ?: "", llmModel = rs.getString(2) ?: "", llmApiKey = rs.getString(3) ?: "",
                    embedBaseUrl = rs.getString(4) ?: "", embedModel = rs.getString(5) ?: "", embedApiKey = rs.getString(6) ?: "",
                    updatedAt = rs.getLong(7),
                ) else AppConfigDto()
            }
    }

    /** 保存配置备份（LWW：入参 updatedAt 更新才落库）。返回 applied 与当前生效配置。 */
    @Synchronized
    fun saveConfig(c: AppConfigDto): Pair<Boolean, AppConfigDto> {
        val current = getConfig()
        if (c.updatedAt <= current.updatedAt) return false to current
        if (isPg) {
            conn.prepareStatement(
                """INSERT INTO app_config(id,llm_base_url,llm_model,llm_api_key,embed_base_url,embed_model,embed_api_key,updated_at)
                   VALUES (1,?,?,?,?,?,?,?)
                   ON CONFLICT (id) DO UPDATE SET llm_base_url=EXCLUDED.llm_base_url, llm_model=EXCLUDED.llm_model,
                     llm_api_key=EXCLUDED.llm_api_key, embed_base_url=EXCLUDED.embed_base_url,
                     embed_model=EXCLUDED.embed_model, embed_api_key=EXCLUDED.embed_api_key, updated_at=EXCLUDED.updated_at"""
            ).use { ps ->
                ps.setString(1, c.llmBaseUrl); ps.setString(2, c.llmModel); ps.setString(3, c.llmApiKey)
                ps.setString(4, c.embedBaseUrl); ps.setString(5, c.embedModel); ps.setString(6, c.embedApiKey)
                ps.setLong(7, c.updatedAt); ps.executeUpdate()
            }
        } else {
            conn.prepareStatement(
                """INSERT OR REPLACE INTO app_config(id,llm_base_url,llm_model,llm_api_key,embed_base_url,embed_model,embed_api_key,updated_at)
                   VALUES (1,?,?,?,?,?,?,?)"""
            ).use { ps ->
                ps.setString(1, c.llmBaseUrl); ps.setString(2, c.llmModel); ps.setString(3, c.llmApiKey)
                ps.setString(4, c.embedBaseUrl); ps.setString(5, c.embedModel); ps.setString(6, c.embedApiKey)
                ps.setLong(7, c.updatedAt); ps.executeUpdate()
            }
        }
        return true to c
    }

    @Synchronized
    fun softDelete(kind: String, docId: String, deletedAt: Long): Boolean {
        val existing = queryDoc(kind, docId) ?: return false
        if (existing.deletedAt > 0) return true // already tombstoned: idempotent acknowledgement
        if (existing.updatedAt >= deletedAt) return false
        conn.prepareStatement("UPDATE docs SET deleted_at=?, updated_at=? WHERE kind=? AND doc_id=?").use { ps ->
            ps.setLong(1, deletedAt); ps.setLong(2, deletedAt)
            ps.setString(3, kind); ps.setString(4, docId)
            ps.executeUpdate()
        }
        logChange(kind, docId, deleted = true)
        return true
    }

    /** 拉取 seq > cursor 的变更对应的文档（每文档只取最新一条变更；已删内容置空） */
    @Synchronized
    fun pull(cursor: Long, limit: Int): Pair<List<DocRow>, Long> {
        val rows = mutableListOf<DocRow>()
        var newCursor = cursor
        conn.prepareStatement(
            """SELECT c.maxseq, d.kind, d.doc_id, d.category, d.title, d.content,
                      d.updated_at, d.device_id, d.content_hash, d.deleted_at
               FROM (SELECT kind, doc_id, MAX(seq) AS maxseq FROM changes WHERE seq > ? GROUP BY kind, doc_id) c
               JOIN docs d ON d.kind = c.kind AND d.doc_id = c.doc_id
               ORDER BY c.maxseq ASC LIMIT ?"""
        ).use { ps ->
            ps.setLong(1, cursor); ps.setInt(2, limit)
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    rows.add(
                        DocRow(
                            seq = rs.getLong(1), kind = rs.getString(2), docId = rs.getString(3),
                            category = rs.getString(4), title = rs.getString(5), content = rs.getString(6),
                            updatedAt = rs.getLong(7), deviceId = rs.getString(8), contentHash = rs.getString(9),
                            deletedAt = rs.getLong(10),
                        )
                    )
                }
            }
        }
        if (rows.isNotEmpty()) newCursor = rows.last().seq
        return rows to newCursor
    }

    @Synchronized
    fun docCount(): Long =
        conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM docs WHERE deleted_at=0").use { rs ->
                rs.next(); rs.getLong(1)
            }
        }

    /** 管理页：列出文档（可选关键词过滤标题/内容），新→旧。 */
    @Synchronized
    fun listDocs(query: String, limit: Int = 200): List<DocRow> {
        val rows = mutableListOf<DocRow>()
        val hasQ = query.isNotBlank()
        val sql = buildString {
            append("SELECT 0, kind, doc_id, category, title, content, updated_at, device_id, content_hash, deleted_at FROM docs")
            if (hasQ) append(" WHERE title LIKE ? OR content LIKE ?")
            append(" ORDER BY updated_at DESC LIMIT ?")
        }
        conn.prepareStatement(sql).use { ps ->
            var i = 1
            if (hasQ) { ps.setString(i, "%$query%"); ps.setString(i + 1, "%$query%"); i += 2 }
            ps.setInt(i, limit)
            ps.executeQuery().use { rs ->
                while (rs.next()) rows.add(rsToRow(rs))
            }
        }
        return rows
    }

    @Synchronized
    fun getDoc(kind: String, docId: String): DocRow? {
        conn.prepareStatement(
            "SELECT 0, kind, doc_id, category, title, content, updated_at, device_id, content_hash, deleted_at FROM docs WHERE kind=? AND doc_id=?"
        ).use { ps ->
            ps.setString(1, kind); ps.setString(2, docId)
            ps.executeQuery().use { rs -> if (rs.next()) return rsToRow(rs) }
        }
        return null
    }

    private fun rsToRow(rs: java.sql.ResultSet): DocRow = DocRow(
        seq = rs.getLong(1), kind = rs.getString(2), docId = rs.getString(3),
        category = rs.getString(4), title = rs.getString(5), content = rs.getString(6),
        updatedAt = rs.getLong(7), deviceId = rs.getString(8), contentHash = rs.getString(9),
        deletedAt = rs.getLong(10),
    )

    @Synchronized
    fun touchDevice(deviceId: String, now: Long) {
        conn.prepareStatement(
            """INSERT INTO devices(device_id,first_seen,last_seen) VALUES(?,?,?)
               ON CONFLICT(device_id) DO UPDATE SET last_seen=excluded.last_seen"""
        ).use { ps ->
            ps.setString(1, deviceId); ps.setLong(2, now); ps.setLong(3, now)
            ps.executeUpdate()
        }
    }

    fun currentSeq(): Long =
        conn.createStatement().use { st ->
            st.executeQuery("SELECT COALESCE(MAX(seq),0) FROM changes").use { rs ->
                rs.next(); rs.getLong(1)
            }
        }

    private fun logChange(kind: String, docId: String, deleted: Boolean) {
        conn.prepareStatement("INSERT INTO changes(kind,doc_id,deleted) VALUES(?,?,?)").use { ps ->
            ps.setString(1, kind); ps.setString(2, docId)
            ps.setInt(3, if (deleted) 1 else 0)
            ps.executeUpdate()
        }
    }

    private fun queryDoc(kind: String, docId: String): DocRow? {
        conn.prepareStatement(
            "SELECT content_hash, updated_at, deleted_at, title, category FROM docs WHERE kind=? AND doc_id=?"
        ).use { ps ->
            ps.setString(1, kind); ps.setString(2, docId)
            ps.executeQuery().use { rs ->
                if (!rs.next()) return null
                return DocRow(
                    seq = 0, kind = kind, docId = docId, category = rs.getString(5) ?: "",
                    title = rs.getString(4) ?: "", content = "",
                    updatedAt = rs.getLong(2), deviceId = "", contentHash = rs.getString(1),
                    deletedAt = rs.getLong(3),
                )
            }
        }
    }

    data class DocRow(
        val seq: Long, val kind: String, val docId: String, val category: String,
        val title: String, val content: String, val updatedAt: Long, val deviceId: String,
        val contentHash: String, val deletedAt: Long,
    )

    override fun close() {
        conn.close()
    }
}
