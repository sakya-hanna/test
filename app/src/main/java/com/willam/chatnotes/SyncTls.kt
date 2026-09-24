package com.willam.chatnotes

import android.content.Context
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/** The private sync CA is trusted only by SyncEngine, never by the WebView or LLM clients.
 * HttpsURLConnection still performs its normal hostname verification against the URL.
 */
internal object SyncTls {
    @Volatile private var cached: SSLSocketFactory? = null

    fun socketFactory(context: Context): SSLSocketFactory = cached ?: synchronized(this) {
        cached ?: build(context.applicationContext).also { cached = it }
    }

    private fun build(context: Context): SSLSocketFactory {
        val cert = context.resources.openRawResource(R.raw.chatnotes_ca).use {
            CertificateFactory.getInstance("X.509").generateCertificate(it)
        }
        val store = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("chatnotes-ca", cert)
        }
        fun trust(store: KeyStore?): X509TrustManager =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply { init(store) }
                .trustManagers.filterIsInstance<X509TrustManager>().first()
        val system = trust(null)
        val privateCa = trust(store)
        val combined = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
                system.checkClientTrusted(chain, authType)

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                try { system.checkServerTrusted(chain, authType) }
                catch (_: CertificateException) { privateCa.checkServerTrusted(chain, authType) }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> =
                system.acceptedIssuers + privateCa.acceptedIssuers
        }
        return SSLContext.getInstance("TLS").apply { init(null, arrayOf(combined), null) }.socketFactory
    }
}
