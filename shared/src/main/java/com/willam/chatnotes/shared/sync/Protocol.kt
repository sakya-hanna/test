package com.willam.chatnotes.shared.sync

import kotlinx.serialization.Serializable

/**
 * ChatNotes 同步协议 v1：形态 A（同步存储型）。
 * 端上为 source of truth 的写入方，服务器为同步中枢；笔记文件不可变（文件名含 jobId）。
 * 所有请求走 HTTPS + Bearer token；所有写操作以 (kind, docId) 为幂等键。
 */

/** 文档种类：note=整理后的 Markdown 笔记；summary_part=分段总结（便于未来重建）；conv=会话元数据 */
@Serializable
enum class DocKind { note, summary_part, conv }

/** 同步条目：端上推送或服务端下发的最小单元 */
@Serializable
data class SyncDoc(
    val kind: DocKind,
    /** 稳定 ID：note 用 jobId 的 64hex；conv 用会话 ID */
    val docId: String,
    /** 分类路径，如 "编程/Kotlin"，服务器用于目录归档与未来检索 */
    val category: String = "",
    /** 笔记标题（不含扩展名） */
    val title: String = "",
    /** Markdown 全文；summary_part 为分段文本 */
    val content: String = "",
    /** 端上生成时间 epoch ms，用于 last-writer-wins 与展示 */
    val updatedAt: Long,
    /** 生成该文档的设备 */
    val deviceId: String,
    /** 内容指纹（SHA-256 前 16 hex），幂等校验用 */
    val contentHash: String,
)

/** 端上 -> 服务器：推送一批文档（增量），服务器逐条幂等落盘 */
@Serializable
data class PushRequest(
    val deviceId: String,
    val docs: List<SyncDoc>,
)

@Serializable
data class PushResultItem(
    val docId: String,
    /** accepted=新写入或内容一致；conflict=同 ID 不同内容且服务器更新 */
    val status: String,
    val serverUpdatedAt: Long = 0,
)

@Serializable
data class PushResponse(
    val results: List<PushResultItem>,
    /** 服务器当前最大 updatedAt，用于下次对账 */
    val serverCursor: Long,
)

/** 端上 -> 服务器：拉取某光标之后的变更（打开 app 全量对账用） */
@Serializable
data class PullRequest(
    val deviceId: String,
    /** 上次同步到的服务器光标（服务器单调递增变更序号），首次传 0 */
    val cursor: Long,
    /** 单次最大条数，防止首拉过大 */
    val limit: Int = 200,
)

@Serializable
data class PullResponse(
    val docs: List<SyncDoc>,
    /** 本次拉取后应保存的新光标；可能还有剩余（docs 达到 limit 时需继续拉） */
    val cursor: Long,
    val hasMore: Boolean,
)

/** 删除标记：笔记在端上被删除时广播；服务器软删 */
@Serializable
data class DeleteMark(
    val kind: DocKind,
    val docId: String,
    val deletedAt: Long,
    val deviceId: String,
)

@Serializable
data class DeleteRequest(
    val deviceId: String,
    val deletes: List<DeleteMark>,
)

@Serializable
data class DeleteResponse(
    val serverCursor: Long,
)

/** 健康检查/握手：端上验证 token 与服务器可达性 */
@Serializable
data class HelloRequest(
    val deviceId: String,
    val appVersion: String,
)

@Serializable
data class HelloResponse(
    val serverVersion: String,
    val protocolVersion: Int,
    /** 服务器当前文档总数，端上用于判断是否需要全量对账 */
    val docCount: Long,
    val serverTime: Long,
)
