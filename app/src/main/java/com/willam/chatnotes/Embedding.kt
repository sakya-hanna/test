package com.willam.chatnotes

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Pluggable so tests inject deterministic vectors without network. */
interface EmbedApi {
    val modelId: String
    /** Synchronous; throws on failure. Caller bounds batch size and total cost. */
    fun embed(texts: List<String>): List<FloatArray>
    fun cancel() {}
}

class EmbedFailure(message: String) : IOException(message)

/**
 * OpenAI-compatible /embeddings client (DashScope compatible-mode,
 * text-embedding-v4 and friends). Never follows redirects, bounded I/O.
 */
class EmbedClient(private val baseUrl: String, private val apiKey: String, override val modelId: String) : EmbedApi {
    @Volatile private var connection: HttpURLConnection? = null
    @Volatile private var cancelled = false
    override fun cancel() { cancelled = true; connection?.disconnect() }
    override fun embed(texts: List<String>): List<FloatArray> {
        require(texts.isNotEmpty() && texts.size <= 25) { "批量向量请求必须在 1-25 条之间" }
        val input = texts.map { it.take(6000) }
        val body = JSONObject().put("model", modelId).put("input", JSONArray(input))
        val conn = URL(baseUrl.trimEnd('/') + "/embeddings").openConnection() as HttpURLConnection
        connection = conn
        try {
            if (cancelled) throw EmbedFailure("任务已停止")
            conn.requestMethod = "POST"; conn.connectTimeout = 30000; conn.readTimeout = 60000
            conn.instanceFollowRedirects = false
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            if (apiKey.isNotEmpty()) conn.setRequestProperty("Authorization", "Bearer $apiKey")
            val bytes = body.toString().toByteArray(Charsets.UTF_8)
            conn.setFixedLengthStreamingMode(bytes.size)
            conn.outputStream.use { it.write(bytes) }
            val code = conn.responseCode
            if (code !in 200..299) {
                conn.errorStream?.close()
                throw EmbedFailure(when (code) {
                    401, 403 -> "向量接口认证失败（$code），请检查密钥"
                    429 -> "向量接口限流（429），稍后自动继续"
                    in 300..399 -> "向量接口返回重定向，已阻止发送密钥"
                    else -> "向量接口请求失败（HTTP $code）"
                })
            }
            val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val out = StringBuilder(); val buffer = CharArray(8192)
                while (true) {
                    if (cancelled) throw EmbedFailure("任务已停止")
                    val n = reader.read(buffer); if (n < 0) break
                    require(out.length + n <= 4 * 1024 * 1024) { "向量响应过大" }
                    out.append(buffer, 0, n)
                }
                out.toString()
            }
            val data = JSONObject(text).getJSONArray("data")
            require(data.length() == texts.size) { "向量接口返回数量与请求不一致" }
            val result = arrayOfNulls<FloatArray>(texts.size)
            var dim = -1
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val idx = item.optInt("index", i)
                require(idx in 0 until texts.size) { "向量接口返回非法索引" }
                val arr = item.getJSONArray("embedding")
                require(arr.length() in 2..4096) { "向量维度异常" }
                if (dim < 0) dim = arr.length() else require(arr.length() == dim) { "返回向量维度不一致" }
                val v = FloatArray(dim)
                for (j in 0 until dim) v[j] = arr.getDouble(j).toFloat()
                require(v.all { !it.isNaN() && !it.isInfinite() }) { "返回向量含非法值" }
                result[idx] = v
            }
            @Suppress("UNCHECKED_CAST")
            return result.filterNotNull() as List<FloatArray>
        } finally { conn.disconnect(); connection = null }
    }
}

object VectorCodec {
    fun encode(v: FloatArray): ByteArray {
        val out = ByteArray(v.size * 4)
        java.nio.ByteBuffer.wrap(out).order(java.nio.ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(v)
        return out
    }
    fun decode(b: ByteArray): FloatArray {
        require(b.size % 4 == 0 && b.size / 4 in 2..4096) { "向量存储损坏" }
        val v = FloatArray(b.size / 4)
        java.nio.ByteBuffer.wrap(b).order(java.nio.ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(v)
        return v
    }
    fun cosine(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "向量维度不匹配" }
        var dot = 0f; var na = 0f; var nb = 0f
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
        if (na <= 0f || nb <= 0f) return 0f
        return dot / (Math.sqrt(na.toDouble()).toFloat() * Math.sqrt(nb.toDouble()).toFloat())
    }
}

/**
 * Splits markdown into embedding chunks: paragraph accumulation to ~target
 * chars, code fences never split, long paragraphs hard-split, every chunk
 * carries the note title as context (per design doc).
 */
object Chunker {
    fun chunk(title: String, body: String, target: Int = 500, maxChunks: Int = 40, maxBody: Int = 30000): List<String> {
        val src = if (body.length > maxBody) body.substring(0, maxBody) else body
        val paragraphs = mutableListOf<String>()
        val fence = Regex("```")
        val lines = src.split('\n')
        val buf = StringBuilder(); var inFence = false
        for (line in lines) {
            if (fence.containsMatchIn(line)) inFence = !inFence
            buf.appendLine(line)
            if (!inFence && line.isBlank()) { paragraphs.add(buf.toString().trim()); buf.setLength(0) }
        }
        if (buf.isNotBlank()) paragraphs.add(buf.toString().trim())
        val cleanTitle = title.trim().trimStart('#').trim()
        val chunks = mutableListOf<String>()
        val acc = StringBuilder()
        fun flush() {
            val text = acc.toString().trim()
            if (text.isNotEmpty()) chunks.add(if (cleanTitle.isNotEmpty() && !text.startsWith("# $cleanTitle"))
                "## $cleanTitle\n$text" else text)
            acc.setLength(0)
        }
        for (p in paragraphs) {
            if (p.length > target * 2) {
                flush()
                var start = 0
                while (start < p.length && chunks.size < maxChunks) {
                    val end = (start + target).coerceAtMost(p.length)
                    chunks.add("## $cleanTitle\n" + p.substring(start, end))
                    start = end
                }
                continue
            }
            if (acc.length + p.length > target && acc.isNotEmpty()) flush()
            acc.append(p).append("\n\n")
            if (acc.length >= target) flush()
        }
        flush()
        return chunks.take(maxChunks)
    }
}

/**
 * Reciprocal Rank Fusion: merges ranked key lists WITHOUT mixing raw score
 * scales (bm25 vs cosine) — exactly what the stage-2 design requires.
 */
object Rrf {
    const val K = 60
    fun fuse(rankings: List<List<String>>): List<Pair<String, Double>> {
        val scores = HashMap<String, Double>()
        for (ranking in rankings) {
            ranking.forEachIndexed { rank, key ->
                scores.merge(key, 1.0 / (K + rank + 1)) { a, b -> a + b }
            }
        }
        return scores.entries.sortedByDescending { it.value }.map { it.key to it.value }
    }
}
