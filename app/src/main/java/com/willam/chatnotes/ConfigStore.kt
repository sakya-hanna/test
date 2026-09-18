package com.willam.chatnotes

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.net.URI
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
    @Synchronized fun save(base: String, secret: String, model: String) {
        val url = base.trim().trimEnd('/'); val name = model.trim()
        validate(url, name)
        require(secret.length <= 8192 && !secret.contains('\n') && !secret.contains('\r')) { "API Key 格式不正确" }
        check(prefs.edit().putString("base_url", url).putString("model", name)
            .putString("api_key_encrypted", encrypted(secret.trim())).remove("api_key").commit()) { "设置保存失败" }
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
