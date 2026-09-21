package com.willam.chatnotes.server

import java.sql.Connection
import java.sql.DriverManager

/**
 * 服务器存储：SQLite 单文件，嵌入运行（无独立 DB 进程）。
 * 表：
 *   docs      文档主表（note/summary_part/conv），content_hash 幂等，soft deleted_at
 *   changes   变更日志（单调递增 seq），Pull 光标即此 seq
 *   devices   设备注册与心跳
 */
class SyncStore(dbPath: String) : AutoCloseable {

    private val conn: Connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    init {
        conn.createStatement().use { st ->
            st.execute("PRAGMA journal_mode=WAL")
            st.execute("PRAGMA synchronous=NORMAL")
            st.execute(
                """CREATE TABLE IF NOT EXISTS docs(
                kind TEXT NOT NULL,
                doc_id TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT '',
                title TEXT NOT NULL DEFAULT '',
                content TEXT NOT NULL DEFAULT '',
                updated_at INTEGER NOT NULL,
                device_id TEXT NOT NULL,
                content_hash TEXT NOT NULL,
                deleted_at INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(kind, doc_id)
            )"""
            )
            st.execute(
                """CREATE TABLE IF NOT EXISTS changes(
                seq INTEGER PRIMARY KEY AUTOINCREMENT,
                kind TEXT NOT NULL,
                doc_id TEXT NOT NULL,
                deleted INTEGER NOT NULL DEFAULT 0
            )"""
            )
            st.execute(
                """CREATE TABLE IF NOT EXISTS devices(
                device_id TEXT PRIMARY KEY,
                first_seen INTEGER NOT NULL,
                last_seen INTEGER NOT NULL
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
            if (existing.contentHash == contentHash) return "same"
            if (existing.updatedAt >= updatedAt) return "older"
        }
        conn.prepareStatement(
            """INSERT OR REPLACE INTO docs(kind,doc_id,category,title,content,updated_at,device_id,content_hash,deleted_at)
               VALUES(?,?,?,?,?,?,?,?,0)"""
        ).use { ps ->
            ps.setString(1, kind); ps.setString(2, docId); ps.setString(3, category)
            ps.setString(4, title); ps.setString(5, content); ps.setLong(6, updatedAt)
            ps.setString(7, deviceId); ps.setString(8, contentHash)
            ps.executeUpdate()
        }
        logChange(kind, docId, deleted = false)
        return "new"
    }

    @Synchronized
    fun softDelete(kind: String, docId: String, deletedAt: Long): Boolean {
        val updated = queryUpdatedAt(kind, docId) ?: return false
        if (updated >= deletedAt) return false
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
            "SELECT content_hash, updated_at, deleted_at FROM docs WHERE kind=? AND doc_id=?"
        ).use { ps ->
            ps.setString(1, kind); ps.setString(2, docId)
            ps.executeQuery().use { rs ->
                if (!rs.next()) return null
                return DocRow(
                    seq = 0, kind = kind, docId = docId, category = "", title = "", content = "",
                    updatedAt = rs.getLong(2), deviceId = "", contentHash = rs.getString(1),
                    deletedAt = rs.getLong(3),
                )
            }
        }
    }

    private fun queryUpdatedAt(kind: String, docId: String): Long? {
        conn.prepareStatement("SELECT updated_at FROM docs WHERE kind=? AND doc_id=?").use { ps ->
            ps.setString(1, kind); ps.setString(2, docId)
            ps.executeQuery().use { rs ->
                rs.next()
                return rs.getLong(1)
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
