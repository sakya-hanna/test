package com.willam.chatnotes.server

import com.willam.chatnotes.shared.sync.AppConfigResponse
import com.willam.chatnotes.shared.sync.AppConfigSaveRequest
import com.willam.chatnotes.shared.sync.DeleteRequest
import com.willam.chatnotes.shared.sync.DeleteResponse
import com.willam.chatnotes.shared.sync.DocKind
import com.willam.chatnotes.shared.sync.HelloRequest
import com.willam.chatnotes.shared.sync.HelloResponse
import com.willam.chatnotes.shared.sync.PullRequest
import com.willam.chatnotes.shared.sync.PullResponse
import com.willam.chatnotes.shared.sync.PushRequest
import com.willam.chatnotes.shared.sync.PushResponse
import com.willam.chatnotes.shared.sync.PushResultItem
import com.willam.chatnotes.shared.sync.SyncDoc
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.basic
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

const val PROTOCOL_VERSION = 2

fun sha256Hex16(content: String): String =
    MessageDigest.getInstance("SHA-256").digest(content.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }.take(16)

fun esc(s: String): String = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
fun urlEsc(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")

/** 鉴权通过后的 principal 占位；token 本身校验见 bearer validate */
object TokenPrincipal : Principal

fun Application.syncModule(store: SyncStore, token: String, adminEnabled: Boolean = false) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }
    install(Authentication) {
        bearer("chatnotes") {
            realm = "chatnotes"
            authenticate { cred -> if (cred.token == token) TokenPrincipal else null }
        }
        basic("admin") {
            realm = "ChatNotes admin"
            validate { cred -> if (cred.name == "admin" && cred.password == token) UserIdPrincipal(cred.name) else null }
        }
    }

    suspend fun ApplicationCall.badRequest(msg: String) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to msg))
    }

    routing {
        get("/health") {
            call.respond(mapOf("ok" to true))
        }

        // Optional read-only admin page, guarded by HTTP Basic and TLS.
        if (adminEnabled) {
            authenticate("admin") {
            get("/admin") {
            val q = call.request.queryParameters["q"].orEmpty().trim()
            val docs = store.listDocs(q, limit = 200)
            val rows = docs.joinToString("") { d ->
                val del = if (d.deletedAt > 0) " <span style='color:#b00'>[已删]</span>" else ""
                "<tr><td>${esc(d.kind)}</td><td><a href='/admin/doc?kind=${d.kind}&id=${urlEsc(d.docId)}'>${esc(d.title.ifBlank { d.docId.take(12) })}</a>$del</td>" +
                    "<td>${esc(d.category)}</td><td>${esc(d.deviceId)}</td><td>${d.updatedAt}</td></tr>"
            }
            val html = """<!DOCTYPE html><html lang="zh"><head><meta charset="utf-8">
<title>ChatNotes 后台</title><meta name="viewport" content="width=device-width,initial-scale=1">
<style>body{font-family:system-ui,sans-serif;margin:24px;max-width:960px}table{border-collapse:collapse;width:100%}
td,th{border:1px solid #ddd;padding:6px 10px;text-align:left;font-size:14px}th{background:#f5f5f5}
input{padding:6px;width:60%}button{padding:6px 14px}.m{color:#888;font-size:12px}</style></head><body>
<h2>ChatNotes 后台（只读）</h2>
<p class="m">共 ${store.docCount()} 篇有效文档</p>
<form method="get" action="/admin"><input name="q" value="${esc(q)}" placeholder="搜索标题/内容…"><button>搜索</button></form>
<table><tr><th>类型</th><th>标题</th><th>分类</th><th>设备</th><th>更新时间</th></tr>$rows</table>
</body></html>"""
            call.respondText(html, io.ktor.http.ContentType.Text.Html)
        }

        get("/admin/doc") {
            val kind = call.request.queryParameters["kind"].orEmpty()
            val id = call.request.queryParameters["id"].orEmpty()
            val d = store.getDoc(kind, id)
            if (d == null) call.respond(HttpStatusCode.NotFound, "未找到文档")
            else {
                val html = """<!DOCTYPE html><html lang="zh"><head><meta charset="utf-8"><title>${esc(d.title)}</title>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>body{font-family:system-ui,sans-serif;margin:24px;max-width:820px}pre{white-space:pre-wrap;word-break:break-word;background:#f8f8f8;padding:16px;border-radius:6px}
.m{color:#888;font-size:12px}</style></head><body>
<p><a href="/admin">← 返回列表</a></p>
<h2>${esc(d.title.ifBlank { "(无标题)" })}</h2>
<p class="m">${esc(d.kind)} · ${esc(d.category)} · 设备 ${esc(d.deviceId)} · updated=${d.updatedAt}${if (d.deletedAt > 0) " · <b style='color:#b00'>已删除</b>" else ""}</p>
<pre>${esc(d.content.ifBlank { "（内容为空——可能是墓碑记录）" })}</pre>
</body></html>"""
                call.respondText(html, io.ktor.http.ContentType.Text.Html)
            }
        }
            }
        }

        authenticate("chatnotes") {
            post("/v1/config") {
                val req = call.receive<HelloRequest>()
                store.touchDevice(req.deviceId, System.currentTimeMillis())
                call.respond(AppConfigResponse(config = store.getConfig()))
            }

            post("/v1/config/save") {
                val req = call.receive<AppConfigSaveRequest>()
                val (applied, current) = store.saveConfig(req.config)
                call.respond(AppConfigResponse(config = current, applied = applied))
            }

            post("/v1/hello") {
                val req = call.receive<HelloRequest>()
                store.touchDevice(req.deviceId, System.currentTimeMillis())
                call.respond(
                    HelloResponse(
                        serverVersion = "0.2.0",
                        protocolVersion = PROTOCOL_VERSION,
                        docCount = store.docCount(),
                        serverTime = System.currentTimeMillis(),
                    )
                )
            }

            post("/v1/push") {
                val req = call.receive<PushRequest>()
                if (req.deviceId.isBlank()) return@post call.badRequest("deviceId required")
                val results = req.docs.map { d ->
                    when {
                        // note ID 必须是 64hex（端上文件名强制），坏 ID 直接拒绝且不推进
                        d.kind == DocKind.note && !d.docId.matches(Regex("[a-f0-9]{64}")) ->
                            PushResultItem(d.docId, "bad_id")
                        d.contentHash != sha256Hex16(d.content) ->
                            PushResultItem(d.docId, "bad_hash")
                        else -> {
                            val status = store.upsert(
                                kind = d.kind.name, docId = d.docId, category = d.category,
                                title = d.title, content = d.content, updatedAt = d.updatedAt,
                                deviceId = d.deviceId, contentHash = d.contentHash,
                            )
                            PushResultItem(d.docId, status, serverUpdatedAt = d.updatedAt)
                        }
                    }
                }
                store.touchDevice(req.deviceId, System.currentTimeMillis())
                call.respond(PushResponse(results = results, serverCursor = store.currentSeq()))
            }

            post("/v1/pull") {
                val req = call.receive<PullRequest>()
                val limit = req.limit.coerceIn(1, 500)
                val (rows, newCursor) = store.pull(req.cursor, limit)
                call.respond(
                    PullResponse(
                        docs = rows.map { r ->
                            val deleted = r.deletedAt > 0
                            SyncDoc(
                                kind = DocKind.valueOf(r.kind), docId = r.docId,
                                category = if (deleted) "" else r.category,
                                title = if (deleted) "" else r.title,
                                content = if (deleted) "" else r.content,
                                updatedAt = r.updatedAt,
                                deviceId = r.deviceId, contentHash = r.contentHash,
                            )
                        },
                        cursor = newCursor,
                        hasMore = rows.size >= limit,
                    )
                )
            }

            post("/v1/delete") {
                val req = call.receive<DeleteRequest>()
                val rejected = req.deletes.filter { m -> !store.softDelete(m.kind.name, m.docId, m.deletedAt) }
                    .map { it.docId }
                store.touchDevice(req.deviceId, System.currentTimeMillis())
                call.respond(DeleteResponse(serverCursor = store.currentSeq(), rejectedIds = rejected))
            }
        }
    }
}

fun main() {
    val port = System.getenv("CHATNOTES_PORT")?.toIntOrNull() ?: 8443
    val dbUrl = System.getenv("CHATNOTES_DB_URL")
        ?: "jdbc:sqlite:" + (System.getenv("CHATNOTES_DB") ?: "./chatnotes-server.db")
    val token = System.getenv("CHATNOTES_TOKEN")
        ?: File("token.txt").takeIf { it.exists() }?.readText()?.trim().takeUnless { it.isNullOrEmpty() }
        ?: error("CHATNOTES_TOKEN env or token.txt required")
    val ksPath = System.getenv("CHATNOTES_KEYSTORE")
    val ksPass = System.getenv("CHATNOTES_KEYSTORE_PASSWORD") ?: "chatnotes"
    // /admin defaults off; when enabled it requires Basic admin / sync token.
    val adminEnabled = System.getenv("CHATNOTES_ADMIN") == "1"

    val server = embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = org.slf4j.LoggerFactory.getLogger("chatnotes")
        module { syncModule(SyncStore(dbUrl), token, adminEnabled) }
        if (ksPath != null) {
            // 生产：PKCS12 证书（gen-certs.sh 产出），Ktor 直接终止 TLS
            val ks = java.security.KeyStore.getInstance("PKCS12").apply {
                java.io.FileInputStream(ksPath).use { load(it, ksPass.toCharArray()) }
            }
            sslConnector(ks, "chatnotes", { ksPass.toCharArray() }, { ksPass.toCharArray() }) {
                this.host = "0.0.0.0"; this.port = port
            }
        } else {
            // No certificate: plain HTTP is restricted to this machine only.
            connector { this.host = "127.0.0.1"; this.port = port }
        }
    })
    server.start(wait = true)
}
