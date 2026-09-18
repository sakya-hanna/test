package com.willam.chatnotes

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

internal fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 255) }

internal fun JSONObject.text(key: String, fallback: String = ""): String =
    if (isNull(key)) fallback else optString(key, fallback)

data class ChatMessage(
    val id: String, val parentId: String, val role: String, val text: String,
    val status: String, val source: String, val attachments: Boolean, val createdAt: Long
) {
    fun json() = JSONObject().put("id", id).put("parentId", parentId).put("role", role)
        .put("text", text).put("status", status).put("source", source)
        .put("attachments", attachments).put("createdAt", createdAt)
    companion object {
        fun from(j: JSONObject) = ChatMessage(j.getString("id"), j.text("parentId"), j.getString("role"),
            j.getString("text"), j.getString("status"), j.text("source", "network"),
            j.optBoolean("attachments"), j.optLong("createdAt"))
    }
}

data class ConversationInfo(val id: String, val title: String, val url: String, val count: Int, val updatedAt: Long)

data class ConversationSnapshot(
    val id: String, val title: String, val url: String, val messages: List<ChatMessage>,
    val warnings: List<String>, val revision: Long
) {
    fun json() = JSONObject().put("id", id).put("title", title).put("url", url)
        .put("messages", JSONArray().also { arr -> messages.forEach { arr.put(it.json()) } })
        .put("warnings", JSONArray(warnings)).put("revision", revision)
    // Deliberately exclude title/timestamps/revision: metadata changes must not create duplicate notes.
    val fingerprint: String get() = sha256(JSONObject().put("conversationId", id)
        .put("messages", JSONArray().also { a -> messages.forEach {
            a.put(JSONObject().put("id", it.id).put("parent", it.parentId).put("role", it.role)
                .put("text", it.text).put("status", it.status).put("attachments", it.attachments))
        } }).toString())
    companion object {
        fun from(j: JSONObject): ConversationSnapshot {
            val a = j.getJSONArray("messages"); val w = j.optJSONArray("warnings") ?: JSONArray()
            return ConversationSnapshot(j.getString("id"), j.text("title"), j.text("url"),
                (0 until a.length()).map { ChatMessage.from(a.getJSONObject(it)) },
                (0 until w.length()).map { w.getString(it) }, j.optLong("revision"))
        }
    }
}

data class SummaryJob(val id: String, val conversationId: String, val snapshot: String,
    val state: String, val draft: String, val notePath: String, val error: String)

/** One retrieval-selected category candidate handed to the summarizer. */
data class CategoryCandidate(
    val path: List<String>,
    val samples: List<String>,   // representative existing note titles under this path
    val score: Float,            // retrieval score of the best matching note (0 = keyword fallback)
    val semantic: Boolean        // true when ranked by embedding similarity
)
