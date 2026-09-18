package com.willam.chatnotes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * LLM 客户端:任意 OpenAI 兼容接口(可配置 base-url / api-key / model)。
 * 一次调用完成两件事:
 *  1. 把对话总结成 Markdown 笔记
 *  2. 给出领域路径(领域 > 子领域 > …)
 * 用 JSON 输出以便稳定解析。
 */
class LlmClient(private val ctx: Context) {

    data class SummaryResult(val path: List<String>, val markdown: String)

    fun summarize(messages: List<Pair<String, String>>, onDone: (Result<SummaryResult>) -> Unit) {
        val prefs = ctx.getSharedPreferences("config", Context.MODE_PRIVATE)
        val baseUrl = prefs.getString("base_url", "")!!.trimEnd('/')
        val apiKey = prefs.getString("api_key", "")!!
        val model = prefs.getString("model", "")!!
        if (baseUrl.isEmpty() || model.isEmpty()) {
            onDone(Result.failure(IllegalStateException("请先在设置中配置 API")));
            return
        }

        Thread {
            onDone(runCatching {
                val transcript = StringBuilder()
                messages.forEach { (role, text) ->
                    transcript.append(if (role == "user") "用户:" else "助手:")
                        .append(text).append("\n\n")
                }

                val sys = """你是一个学习笔记整理助手。把下面的对话总结成一篇结构化中文学习笔记,并归类到领域树。
输出严格 JSON:{"path": ["领域","子领域",...], "title": "笔记标题", "markdown": "笔记正文"}
要求:
- path 是领域层级,2-4 级,如 ["计算机科学与技术","Agent 开发","RAG 检索"];使用已普及的领域名
- markdown 用 ## / ### 划分小节,要点用列表,可留 "## 待深入" 待办
- 只输出 JSON,不要额外文字"""

                val body = JSONObject().apply {
                    put("model", model)
                    put("temperature", 0.3)
                    put("messages", JSONArray().apply {
                        put(JSONObject().put("role", "system").put("content", sys))
                        put(JSONObject().put("role", "user").put("content", transcript.toString()))
                    })
                }

                val conn = URL("$baseUrl/chat/completions").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 30000
                conn.readTimeout = 120000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                if (apiKey.isNotEmpty()) conn.setRequestProperty("Authorization", "Bearer $apiKey")
                conn.outputStream.use { it.write(body.toString().toByteArray()) }

                val code = conn.responseCode
                val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.readText() ?: ""
                if (code !in 200..299) throw RuntimeException("API $code: ${text.take(300)}")

                val content = JSONObject(text)
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content")
                val json = stripFence(content)
                val parsed = JSONObject(json)
                val pathArr = mutableListOf<String>()
                val arr = parsed.getJSONArray("path")
                for (i in 0 until arr.length()) pathArr.add(arr.getString(i))
                val title = parsed.optString("title", pathArr.lastOrNull() ?: "未命名")
                SummaryResult(pathArr + title, parsed.getString("markdown"))
            })
        }.start()
    }

    private fun stripFence(s: String): String {
        var t = s.trim()
        if (t.startsWith("```")) {
            t = t.removePrefix("```json").removePrefix("```").trim()
            if (t.endsWith("```")) t = t.removeSuffix("```").trim()
        }
        return t
    }
}
