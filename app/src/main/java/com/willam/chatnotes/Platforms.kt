package com.willam.chatnotes

import android.net.Uri

/**
 * 支持的对话平台注册表。
 *
 * 每个平台定义：
 * - hosts: WebView 内允许导航的域名（https only）
 * - jsOrigins: document-start 注入 interceptor.js 的 origin 集合（精确 host）
 * - homeUrl: 进入平台时的首页
 * - canJump(url): 会话 URL 是否可跳回原平台
 *
 * 拦截脚本 interceptor.js 内部按 window.location.host 自适配路由/SSE/DOM 差异，
 * 页面侧不感知平台细节，只根据 origin 白名单决定注入与消息接受。
 */
data class Platform(
    val id: String,
    val label: String,
    val hosts: Set<String>,
    val homeUrl: String,
) {
    fun owns(uri: Uri): Boolean =
        uri.scheme == "https" && uri.port in setOf(-1, 443) && uri.host in hosts && uri.userInfo == null

    fun ownsHost(host: String?): Boolean = host != null && host in hosts

    companion object {
        val CHATGPT = Platform(
            id = "chatgpt", label = "ChatGPT",
            hosts = setOf("chatgpt.com", "chat.openai.com", "auth.openai.com", "auth0.openai.com"),
            homeUrl = "https://chatgpt.com",
        )
        val DEEPSEEK = Platform(
            id = "deepseek", label = "DeepSeek",
            hosts = setOf("chat.deepseek.com"),
            homeUrl = "https://chat.deepseek.com",
        )

        val ALL = listOf(CHATGPT, DEEPSEEK)

        /** 按当前 URL 找所属平台；找不到返回 null（不允许导航）。 */
        fun match(uri: Uri): Platform? = ALL.firstOrNull { it.owns(uri) }

        /** document-start 注入需要的全量 origin 集合（所有平台的会话页 origin）。 */
        val jsOrigins: Set<String> = ALL.flatMap { p ->
            p.hosts.filter { !it.startsWith("auth") }.map { "https://$it" }
        }.toSet()
    }
}
