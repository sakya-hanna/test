package com.willam.chatnotes

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.willam.chatnotes.shared.sync.AppConfigDto
import com.willam.chatnotes.shared.sync.SyncState
import java.net.URI
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.serialization.json.Json

data class ApiConfig(val baseUrl: String, val apiKey: String, val model: String)

class ConfigStore(context: Context) {
    val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
    private val alias = "chatnotes.api.v1"
    @Synchronized private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
        }.generateKey()
    }
    private fun encrypted(value: String): String {
        if (value.isEmpty()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return Base64.encodeToString(cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
    @Synchronized fun apiKey(): String {
        // Remove a legacy plaintext key only after the encrypted value was committed successfully.
        if (prefs.contains("api_key")) {
            val old = prefs.getString("api_key", "") ?: ""
            check(prefs.edit().putString("api_key_encrypted", encrypted(old)).remove("api_key").commit()) {
                "密钥迁移失败，请检查存储空间"
            }
        }
        val encoded = prefs.getString("api_key_encrypted", "") ?: ""
        if (encoded.isEmpty()) return ""
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            require(bytes.size > 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        } catch (e: Exception) { throw IllegalStateException("无法解密 API Key，请在设置中重新输入", e) }
    }
    fun read(): ApiConfig = ApiConfig(prefs.getString("base_url", "") ?: "", apiKey(), prefs.getString("model", "") ?: "")
        .also { validate(it.baseUrl, it.model) }

    // ---- 后台同步配置（形态 A）：URL 明文，token 加密；状态为 JSON 快照 ----

    fun syncConfig(): SyncConfig? {
        val base = prefs.getString("sync_base_url", "") ?: ""
        if (base.isBlank()) return null
        val token = syncToken()
        if (token.isBlank()) return null
        return SyncConfig(base, token)
    }

    @Synchronized fun syncToken(): String {
        if (prefs.contains("sync_token")) {
            val old = prefs.getString("sync_token", "") ?: ""
            check(prefs.edit().putString("sync_token_encrypted", encrypted(old)).remove("sync_token").commit()) {
                "同步令牌迁移失败"
            }
        }
        val encoded = prefs.getString("sync_token_encrypted", "") ?: ""
        if (encoded.isEmpty()) return ""
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            require(bytes.size > 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        } catch (e: Exception) { throw IllegalStateException("无法解密同步令牌，请在设置中重新输入", e) }
    }

    @Synchronized fun saveSync(baseUrl: String, token: String) {
        check(prefs.edit().putString("sync_base_url", baseUrl.trim()).putString("sync_token_encrypted", encrypted(token)).remove("sync_token").commit()) {
            "同步设置保存失败"
        }
    }

    fun syncCursor(): Long = java.lang.Long.parseLong(prefs.getString("sync_cursor", "0") ?: "0")

    fun syncState(): SyncState = try {
        val raw = prefs.getString("sync_state", "") ?: ""
        if (raw.isEmpty()) SyncState() else Json.decodeFromString(SyncState.serializer(), raw)
    } catch (e: Exception) { SyncState() }

    fun saveSyncState(state: SyncState) {
        prefs.edit()
            .putString("sync_state", Json.encodeToString(SyncState.serializer(), state))
            .putString("sync_cursor", state.cursor.toString())
            .apply()
    }

    // ---- embedding service config (stage 2) ----
    @Synchronized fun embedApiKey(): String {
        if (prefs.contains("embed_key")) {
            val old = prefs.getString("embed_key", "") ?: ""
            check(prefs.edit().putString("embed_key_encrypted", encrypted(old)).remove("embed_key").commit()) {
                "向量密钥迁移失败"
            }
        }
        val encoded = prefs.getString("embed_key_encrypted", "") ?: ""
        if (encoded.isEmpty()) return ""
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            require(bytes.size > 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        } catch (e: Exception) { throw IllegalStateException("无法解密向量密钥，请在设置中重新输入", e) }
    }
    @Synchronized fun saveEmbed(base: String, secret: String, model: String) {
        val url = base.trim().trimEnd('/'); val name = model.trim()
        if (url.isEmpty() && name.isEmpty() && secret.isEmpty()) {
            // Clearing the embedding config is allowed; hybrid search degrades to keyword.
            check(prefs.edit().remove("embed_base_url").remove("embed_model").remove("embed_key_encrypted")
                .putString("cfg_updated_at", System.currentTimeMillis().toString()).commit()) { "设置保存失败" }
            return
        }
        validate(url, name)
        require(secret.length <= 8192 && !secret.contains('\n') && !secret.contains('\r')) { "API Key 格式不正确" }
        check(prefs.edit().putString("embed_base_url", url).putString("embed_model", name)
            .putString("embed_key_encrypted", encrypted(secret.trim()))
            .putString("cfg_updated_at", System.currentTimeMillis().toString()).commit()) { "设置保存失败" }
    }
    fun embedConfigured(): Boolean =
        !(prefs.getString("embed_base_url", "") ?: "").isNullOrBlank() && !(prefs.getString("embed_model", "") ?: "").isNullOrBlank()
    @Synchronized fun save(base: String, secret: String, model: String) {
        val url = base.trim().trimEnd('/'); val name = model.trim()
        validate(url, name)
        require(secret.length <= 8192 && !secret.contains('\n') && !secret.contains('\r')) { "API Key 格式不正确" }
        check(prefs.edit().putString("base_url", url).putString("model", name)
            .putString("api_key_encrypted", encrypted(secret.trim())).remove("api_key")
            .putString("cfg_updated_at", System.currentTimeMillis().toString()).commit()) { "设置保存失败" }
    }

    // ---- 配置备份（服务器 app_config 单行，LWW 同步；见 SyncWorker.reconcileConfig）----

    /** 本地配置版本时间：每次在手机上保存设置时更新；0 = 本机从未编辑过（全新安装/刚重装） */
    fun configUpdatedAt(): Long = java.lang.Long.parseLong(prefs.getString("cfg_updated_at", "0") ?: "0")

    /** 导出当前配置为备份 DTO。解密失败的 key 记为空串，不阻断导出与同步。 */
    fun exportConfig(): AppConfigDto = AppConfigDto(
        llmBaseUrl = prefs.getString("base_url", "") ?: "",
        llmModel = prefs.getString("model", "") ?: "",
        llmApiKey = try { apiKey() } catch (e: Exception) { "" },
        embedBaseUrl = prefs.getString("embed_base_url", "") ?: "",
        embedModel = prefs.getString("embed_model", "") ?: "",
        embedApiKey = try { embedApiKey() } catch (e: Exception) { "" },
        updatedAt = configUpdatedAt(),
    )

    /**
     * 应用服务器配置（半段合并）：按字段写入非空值——服务器某字段为空就不动本地对应值，
     * 防止服务器上的残缺段把手机上正在使用的配置打没。应用后本地版本时间对齐服务器。
     */
    @Synchronized fun applyServerConfig(cfg: AppConfigDto) {
        val e = prefs.edit()
        if (cfg.llmBaseUrl.isNotBlank()) e.putString("base_url", cfg.llmBaseUrl.trim())
        if (cfg.llmModel.isNotBlank()) e.putString("model", cfg.llmModel.trim())
        if (cfg.llmApiKey.isNotBlank()) e.putString("api_key_encrypted", encrypted(cfg.llmApiKey.trim())).remove("api_key")
        if (cfg.embedBaseUrl.isNotBlank()) e.putString("embed_base_url", cfg.embedBaseUrl.trim().trimEnd('/'))
        if (cfg.embedModel.isNotBlank()) e.putString("embed_model", cfg.embedModel.trim())
        if (cfg.embedApiKey.isNotBlank()) e.putString("embed_key_encrypted", encrypted(cfg.embedApiKey.trim())).remove("embed_key")
        e.putString("cfg_updated_at", cfg.updatedAt.toString())
        check(e.commit()) { "服务器配置应用失败" }
    }
    companion object {
        fun validate(base: String, model: String) {
            val uri = runCatching { URI(base) }.getOrNull()
            require(uri != null && uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null &&
                uri.query == null && uri.fragment == null) { "API Base URL 必须是有效 HTTPS 地址，不能包含账号、查询参数或片段" }
            require(model.isNotBlank() && model.length <= 200) { "请填写有效模型名称" }
        }
    }
}
