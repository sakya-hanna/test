package com.willam.chatnotes.server

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
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

const val PROTOCOL_VERSION = 1

fun sha256Hex16(content: String): String =
    MessageDigest.getInstance("SHA-256").digest(content.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }.take(16)

/** 鉴权通过后的 principal 占位；token 本身校验见 bearer validate */
object TokenPrincipal : Principal

fun Application.syncModule(store: SyncStore, token: String) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }
    install(Authentication) {
        bearer("chatnotes") {
            realm = "chatnotes"
            authenticate { cred -> if (cred.token == token) TokenPrincipal else null }
        }
    }

    suspend fun ApplicationCall.badRequest(msg: String) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to msg))
    }

    routing {
        get("/health") {
            call.respond(mapOf("ok" to true))
        }

        authenticate("chatnotes") {
            post("/v1/hello") {
                val req = call.receive<HelloRequest>()
                store.touchDevice(req.deviceId, System.currentTimeMillis())
                call.respond(
                    HelloResponse(
                        serverVersion = "0.1.0",
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
                    if (d.contentHash != sha256Hex16(d.content)) {
                        PushResultItem(d.docId, "bad_hash")
                    } else {
                        val status = store.upsert(
                            kind = d.kind.name, docId = d.docId, category = d.category,
                            title = d.title, content = d.content, updatedAt = d.updatedAt,
                            deviceId = d.deviceId, contentHash = d.contentHash,
                        )
                        PushResultItem(d.docId, status, serverUpdatedAt = d.updatedAt)
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
                req.deletes.forEach { m ->
                    store.softDelete(m.kind.name, m.docId, m.deletedAt)
                }
                store.touchDevice(req.deviceId, System.currentTimeMillis())
                call.respond(DeleteResponse(serverCursor = store.currentSeq()))
            }
        }
    }
}

fun main() {
    val port = System.getenv("CHATNOTES_PORT")?.toIntOrNull() ?: 8443
    val dbPath = System.getenv("CHATNOTES_DB") ?: "./chatnotes-server.db"
    val token = System.getenv("CHATNOTES_TOKEN")
        ?: File("token.txt").takeIf { it.exists() }?.readText()?.trim().takeUnless { it.isNullOrEmpty() }
        ?: error("CHATNOTES_TOKEN env or token.txt required")
    val ksPath = System.getenv("CHATNOTES_KEYSTORE")
    val ksPass = System.getenv("CHATNOTES_KEYSTORE_PASSWORD") ?: "chatnotes"

    val server = embeddedServer(Netty, environment = applicationEngineEnvironment {
        log = org.slf4j.LoggerFactory.getLogger("chatnotes")
        module { syncModule(SyncStore(dbPath), token) }
        if (ksPath != null) {
            // 生产：PKCS12 证书（gen-certs.sh 产出），Ktor 直接终止 TLS
            val ks = java.security.KeyStore.getInstance("PKCS12").apply {
                java.io.FileInputStream(ksPath).use { load(it, ksPass.toCharArray()) }
            }
            sslConnector(ks, "chatnotes", { ksPass.toCharArray() }, { ksPass.toCharArray() }) {
                this.host = "0.0.0.0"; this.port = port
            }
        } else {
            // 开发/本机调试：无证书时退回明文（app 端 usesCleartextTraffic=false 会拒绝，仅限 curl/联调）
            connector { this.host = "0.0.0.0"; this.port = port }
        }
    })
    server.start(wait = true)
}
