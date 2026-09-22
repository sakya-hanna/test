package com.willam.chatnotes

import com.willam.chatnotes.shared.sync.AppConfigDto
import com.willam.chatnotes.shared.sync.AppConfigResponse
import com.willam.chatnotes.shared.sync.AppConfigSaveRequest
import com.willam.chatnotes.shared.sync.DeleteMark
import com.willam.chatnotes.shared.sync.DeleteRequest
import com.willam.chatnotes.shared.sync.DeleteResponse
import com.willam.chatnotes.shared.sync.HelloRequest
import com.willam.chatnotes.shared.sync.HelloResponse
import com.willam.chatnotes.shared.sync.PullRequest
import com.willam.chatnotes.shared.sync.PullResponse
import com.willam.chatnotes.shared.sync.PushRequest
import com.willam.chatnotes.shared.sync.PushResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/** 同步结果：区分「服务不可达」与「被拒绝」，对应设计 §12.5 的提示要求 */
sealed class SyncOutcome {
    data class Ok(val nPush: Int, val nPulled: Int, val nDeleted: Int) : SyncOutcome()
    data class Unreachable(val cause: String) : SyncOutcome()
    data class Rejected(val httpCode: Int, val detail: String) : SyncOutcome()
}

/**
 * 端上同步引擎：仅 HTTP + 显式 serializer（不依赖反射），决策全在 shared.SyncLogic。
 * HttpURLConnection 与 LlmClient 一致，不引入新网络依赖。
 */
class SyncEngine(
    private val baseUrl: String,
    private val token: String,
    val deviceId: String,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun <B, R> post(
        path: String, body: B, bodySer: KSerializer<B>, respSer: KSerializer<R>,
    ): Pair<Int, R?> {
        val conn = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 20000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.outputStream.use { it.write(json.encodeToString(bodySer, body).toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.readText()
            return code to (if (code in 200..299 && text != null) json.decodeFromString(respSer, text) else null)
        } finally {
            conn.disconnect()
        }
    }

    fun hello(appVersion: String): SyncOutcome = try {
        val (code, resp) = post("/v1/hello", HelloRequest(deviceId, appVersion), HelloRequest.serializer(), HelloResponse.serializer())
        when {
            code == 401 || code == 403 -> SyncOutcome.Rejected(code, "token 被服务器拒绝")
            resp == null -> SyncOutcome.Rejected(code, "服务器响应无法解析 (HTTP $code)")
            resp.protocolVersion != 1 -> SyncOutcome.Rejected(code, "服务器协议版本 ${resp.protocolVersion} 与端上不匹配")
            else -> SyncOutcome.Ok(0, 0, 0)
        }
    } catch (e: java.io.IOException) {
        SyncOutcome.Unreachable(e.message ?: "网络错误")
    }

    fun push(docs: List<com.willam.chatnotes.shared.sync.SyncDoc>): SyncOutcome {
        if (docs.isEmpty()) return SyncOutcome.Ok(0, 0, 0)
        return try {
            val (code, resp) = post("/v1/push", PushRequest(deviceId, docs), PushRequest.serializer(), PushResponse.serializer())
            when {
                resp == null -> SyncOutcome.Rejected(code, "推送被拒绝 (HTTP $code)")
                resp.results.any { it.status == "bad_hash" } -> SyncOutcome.Rejected(code, "有文档内容校验不符")
                else -> SyncOutcome.Ok(docs.size, 0, 0)
            }
        } catch (e: java.io.IOException) {
            SyncOutcome.Unreachable(e.message ?: "网络错误")
        }
    }

    fun pull(cursor: Long, limit: Int = 200): Pair<SyncOutcome, PullResponse?> = try {
        val (code, resp) = post("/v1/pull", PullRequest(deviceId, cursor, limit), PullRequest.serializer(), PullResponse.serializer())
        if (resp == null) SyncOutcome.Rejected(code, "拉取被拒绝 (HTTP $code)") to null
        else SyncOutcome.Ok(0, resp.docs.size, 0) to resp
    } catch (e: java.io.IOException) {
        SyncOutcome.Unreachable(e.message ?: "网络错误") to null
    }

    fun delete(marks: List<DeleteMark>): SyncOutcome {
        if (marks.isEmpty()) return SyncOutcome.Ok(0, 0, 0)
        return try {
            val (code, resp) = post("/v1/delete", DeleteRequest(deviceId, marks), DeleteRequest.serializer(), DeleteResponse.serializer())
            if (resp == null) SyncOutcome.Rejected(code, "删除广播被拒绝 (HTTP $code)") else SyncOutcome.Ok(0, 0, marks.size)
        } catch (e: java.io.IOException) {
            SyncOutcome.Unreachable(e.message ?: "网络错误")
        }
    }

    /** 读服务器配置备份（/v1/config） */
    fun fetchConfig(): Pair<SyncOutcome, AppConfigResponse?> = try {
        val (code, resp) = post("/v1/config", HelloRequest(deviceId, "config"), HelloRequest.serializer(), AppConfigResponse.serializer())
        if (resp == null) SyncOutcome.Rejected(code, "配置读取被拒绝 (HTTP $code)") to null
        else SyncOutcome.Ok(0, 0, 0) to resp
    } catch (e: java.io.IOException) {
        SyncOutcome.Unreachable(e.message ?: "网络错误") to null
    }

    /** 保存配置备份（/v1/config/save，服务器 LWW）；返回服务器的当前生效配置 */
    fun saveConfig(cfg: AppConfigDto): Pair<SyncOutcome, AppConfigResponse?> = try {
        val (code, resp) = post("/v1/config/save", AppConfigSaveRequest(deviceId, cfg), AppConfigSaveRequest.serializer(), AppConfigResponse.serializer())
        if (resp == null) SyncOutcome.Rejected(code, "配置保存被拒绝 (HTTP $code)") to null
        else SyncOutcome.Ok(0, 0, 0) to resp
    } catch (e: java.io.IOException) {
        SyncOutcome.Unreachable(e.message ?: "网络错误") to null
    }
}
