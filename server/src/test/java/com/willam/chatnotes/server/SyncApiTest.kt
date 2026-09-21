package com.willam.chatnotes.server

import com.willam.chatnotes.shared.sync.DeleteMark
import com.willam.chatnotes.shared.sync.DeleteRequest
import com.willam.chatnotes.shared.sync.DocKind
import com.willam.chatnotes.shared.sync.HelloRequest
import com.willam.chatnotes.shared.sync.PullRequest
import com.willam.chatnotes.shared.sync.PushRequest
import com.willam.chatnotes.shared.sync.SyncDoc
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOKEN = "test-token-123"

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

private fun doc(
    id: String, content: String, updatedAt: Long = 1000L,
    kind: DocKind = DocKind.note, device: String = "phone-1",
) = SyncDoc(
    kind = kind, docId = id, category = "编程/Kotlin", title = "笔记 $id",
    content = content, updatedAt = updatedAt, deviceId = device,
    contentHash = sha256Hex16(content),
)

private fun withApp(block: suspend ApplicationTestBuilder.(String, HttpClient) -> Unit) {
    val db = Files.createTempFile("chatnotes-test", ".db").toFile().apply { deleteOnExit() }
    testApplication {
        environment {
            config = ApplicationConfig("empty")
        }
        application {
            syncModule(SyncStore(db.absolutePath), TOKEN)
        }
        val client = createClient { }
        block(TOKEN, client)
    }
}

/** 手动序列化 JSON POST：不依赖客户端 ContentNegotiation 基建 */
private suspend fun HttpClient.postJsonRaw(path: String, token: String, bodyJson: String): HttpResponse =
    post(path) {
        bearerAuth(token)
        contentType(ContentType.Application.Json)
        setBody(bodyJson)
    }

class SyncApiTest {

    @Test
    fun `health 探活无需鉴权`() = withApp { _, client ->
        val resp = client.get("/health")
        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.bodyAsText().contains("ok"))
    }

    @Test
    fun `hello 错误 token 返回 401`() = withApp { _, client ->
        val body = json.encodeToString(HelloRequest.serializer(), HelloRequest(deviceId = "phone-1", appVersion = "test"))
        val resp = client.postJsonRaw("/v1/hello", "wrong-token", body)
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
    }

    @Test
    fun `hello 正确 token 返回服务器状态`() = withApp { token, client ->
        val body = json.encodeToString(HelloRequest.serializer(), HelloRequest(deviceId = "phone-1", appVersion = "test"))
        val resp = client.postJsonRaw("/v1/hello", token, body)
        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.bodyAsText().contains("\"protocolVersion\":1"))
    }

    @Test
    fun `push 后 pull 能取回同一文档`() = withApp { token, client ->
        val d = doc("abc123", "# 笔记 abc123\n\n内容", updatedAt = 5000L)
        val pushResp = client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(d))))
        assertEquals(HttpStatusCode.OK, pushResp.status)
        assertTrue(pushResp.bodyAsText().contains("\"status\":\"new\""))

        val pullResp = client.postJsonRaw("/v1/pull", token, json.encodeToString(PullRequest.serializer(), PullRequest(deviceId = "phone-1", cursor = 0)))
        assertEquals(HttpStatusCode.OK, pullResp.status)
        val body = pullResp.bodyAsText()
        assertTrue(body.contains("abc123"))
        assertTrue(body.contains("# 笔记 abc123"))
    }

    @Test
    fun `push 幂等：同内容重复推送不产生新变更`() = withApp { token, client ->
        val d = doc("abc123", "内容")
        val first = client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(d))))
        assertTrue(first.bodyAsText().contains("\"status\":\"new\""))
        val second = client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(d))))
        assertTrue(second.bodyAsText().contains("\"status\":\"same\""))
    }

    @Test
    fun `push 内容 hash 不符被拒绝`() = withApp { token, client ->
        val d = doc("abc123", "内容").copy(contentHash = "deadbeefdeadbeef")
        val resp = client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(d))))
        assertTrue(resp.bodyAsText().contains("bad_hash"))
    }

    @Test
    fun `删除后 pull 保留墓碑但内容不可得`() = withApp { token, client ->
        val d = doc("abc123", "内容")
        client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(d))))
        val before = client.postJsonRaw("/v1/pull", token, json.encodeToString(PullRequest.serializer(), PullRequest(deviceId = "phone-1", cursor = 0)))
        assertTrue(before.bodyAsText().contains("abc123"))

        client.postJsonRaw(
            "/v1/delete", token,
            json.encodeToString(
                DeleteRequest.serializer(),
                DeleteRequest(
                    deviceId = "phone-1",
                    deletes = listOf(DeleteMark(kind = DocKind.note, docId = "abc123", deletedAt = 9000L, deviceId = "phone-1")),
                ),
            ),
        )
        val after = client.postJsonRaw("/v1/pull", token, json.encodeToString(PullRequest.serializer(), PullRequest(deviceId = "phone-1", cursor = 0)))
        assertTrue(after.bodyAsText().contains("abc123"))       // 变更日志保留墓碑
        assertTrue(!after.bodyAsText().contains("内容"))         // 内容已软删不可得
    }

    @Test
    fun `push 旧版本被服务器较新版本拒绝`() = withApp { token, client ->
        val newer = doc("abc123", "新内容", updatedAt = 8000L)
        val older = doc("abc123", "旧内容", updatedAt = 5000L)
        client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(newer))))
        val resp = client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(older))))
        assertTrue(resp.bodyAsText().contains("\"status\":\"older\""))
    }

    @Test
    fun `pull 同一文档多次变更只返回最新一条`() = withApp { token, client ->
        val d1 = doc("abc123", "第一版", updatedAt = 1000L)
        val d2 = doc("abc123", "第二版", updatedAt = 2000L)
        client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(d1))))
        client.postJsonRaw("/v1/push", token, json.encodeToString(PushRequest.serializer(), PushRequest(deviceId = "phone-1", docs = listOf(d2))))
        client.postJsonRaw(
            "/v1/delete", token,
            json.encodeToString(
                DeleteRequest.serializer(),
                DeleteRequest(
                    deviceId = "phone-1",
                    deletes = listOf(DeleteMark(kind = DocKind.note, docId = "abc123", deletedAt = 3000L, deviceId = "phone-1")),
                ),
            ),
        )
        val resp = client.postJsonRaw("/v1/pull", token, json.encodeToString(PullRequest.serializer(), PullRequest(deviceId = "phone-1", cursor = 0)))
        val body = resp.bodyAsText()
        assertEquals(1, Regex("\"docId\":\"abc123\"").findAll(body).count(), "同一文档应只出现一次：$body")
        assertTrue(body.contains("\"content\":\"\""), "最新变更是删除，内容应为空：$body")
    }
}
