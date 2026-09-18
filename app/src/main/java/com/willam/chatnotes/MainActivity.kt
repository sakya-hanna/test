package com.willam.chatnotes

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import io.noties.markwon.Markwon
import org.json.JSONObject
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var repo: NotesRepo
    private lateinit var llm: LlmClient
    private lateinit var markwon: Markwon
    private lateinit var webView: WebView

    // 对话缓冲:user_msg / assistant_msg 按到达顺序累积
    private val transcript = LinkedBlockingQueue<Pair<String, String>>()
    private var conversationTitle: String = ""
    private var summarized = false

    private lateinit var chatScreen: View
    private lateinit var notesScreen: View
    private lateinit var settingsBtn: View
    private lateinit var captureInfo: TextView
    private lateinit var pathBar: LinearLayout
    private lateinit var listBox: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var detailScroll: ScrollView
    private lateinit var detailBody: TextView

    private var stack = mutableListOf<NotesRepo.Node>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("config", MODE_PRIVATE)
        repo = NotesRepo(this)
        llm = LlmClient(this)
        markwon = Markwon.create(this)

        chatScreen = findViewById(R.id.chatScreen)
        notesScreen = findViewById(R.id.notesScreen)
        settingsBtn = findViewById(R.id.settingsBtn)
        captureInfo = findViewById(R.id.captureInfo)
        pathBar = findViewById(R.id.pathBar)
        listBox = findViewById(R.id.listBox)
        searchInput = findViewById(R.id.searchInput)
        scrollView = findViewById(R.id.browseScroll)
        detailScroll = findViewById(R.id.detailScroll)
        detailBody = findViewById(R.id.detailBody)

        setupWebView()
        setupTabs()
        setupNotes()
        render()
    }

    // ================= WebView + 拦截 =================

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView() {
        webView = findViewById(R.id.webView)
        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        // 伪装成 Chrome,降低 WebView 指纹差异带来的风控概率
        s.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest) = false
        }

        // JavascriptInterface:页面注入脚本回传消息
        webView.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun postMessage(msg: String) {
                runOnUiThread { handleIntercept(msg) }
            }
        }, "chatnotesProxy")

        // document-start 注入:页面脚本运行前包装 fetch
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(webView, InterceptorJs.SRC, setOf("https://chatgpt.com", "https://*.chatgpt.com"))
        } else {
            Toast.makeText(this, "WebView 版本过旧,拦截不可用,将退化为 DOM 抓取", Toast.LENGTH_LONG).show()
        }

        webView.loadUrl("https://chatgpt.com")
    }

    private fun handleIntercept(msg: String) {
        runCatching {
            val j = JSONObject(msg)
            when (j.optString("type")) {
                "user_msg" -> {
                    transcript.offer("user" to j.optString("text"))
                    updateCaptureInfo()
                }
                "assistant_msg" -> {
                    val t = j.optString("text").trim()
                    if (t.isNotEmpty()) {
                        transcript.offer("assistant" to t)
                        updateCaptureInfo()
                    }
                }
                "title" -> {
                    conversationTitle = j.optString("title")
                }
            }
        }
    }

    private fun updateCaptureInfo() {
        captureInfo.text = "旁听中 · 本次已捕获 ${transcript.size} 条消息"
    }

    /** 关闭页面按钮 → 总结流程 */
    fun onCloseChat(v: View) {
        if (transcript.isEmpty()) {
            Toast.makeText(this, "本次没有拦截到对话内容", Toast.LENGTH_SHORT).show()
            return
        }
        if (summarized) { switchTab(false); return }
        AlertDialog.Builder(this)
            .setTitle("总结本次对话?")
            .setMessage("已捕获 ${transcript.size} 条消息,将调用你配置的 LLM 总结并归档。")
            .setPositiveButton("总结") { _, _ -> runSummary() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun runSummary() {
        val msgs = transcript.toList()
        val dlg = AlertDialog.Builder(this)
            .setTitle("正在总结…")
            .setMessage("收集对话 → LLM 总结 → 归类 → 写入 Markdown")
            .setCancelable(false)
            .show()
        llm.summarize(msgs) { res ->
            runOnUiThread {
                dlg.dismiss()
                res.fold({
                    val f = repo.writeNote(it.path, it.markdown)
                    summarized = true
                    Toast.makeText(this, "已保存:${it.path.joinToString(" / ")}", Toast.LENGTH_LONG).show()
                    stack.clear(); stack.add(repo.tree())
                    switchTab(false); render()
                }, {
                    AlertDialog.Builder(this)
                        .setTitle("总结失败")
                        .setMessage(it.message ?: "未知错误")
                        .setPositiveButton("好", null)
                        .show()
                })
            }
        }
    }

    // ================= Tab / 设置 =================

    private fun setupTabs() {
        findViewById<View>(R.id.tabChat).setOnClickListener { switchTab(true) }
        findViewById<View>(R.id.tabNotes).setOnClickListener { switchTab(false) }
        settingsBtn.setOnClickListener { showSettings() }
    }

    private fun switchTab(chat: Boolean) {
        chatScreen.visibility = if (chat) View.VISIBLE else View.GONE
        notesScreen.visibility = if (chat) View.GONE else View.VISIBLE
        findViewById<View>(R.id.tabChat).alpha = if (chat) 1f else 0.45f
        findViewById<View>(R.id.tabNotes).alpha = if (chat) 0.45f else 1f
    }

    private fun showSettings() {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }
        fun field(label: String, key: String, hint: String, inputType: Int): EditText {
            val et = EditText(this)
            et.hint = hint
            et.inputType = inputType
            et.setText(prefs.getString(key, ""))
            wrap.addView(TextView(this).apply { text = label })
            wrap.addView(et)
            return et
        }
        val url = field("API Base URL", "base_url", "https://api.openai.com/v1", EditorInfo.TYPE_TEXT_VARIATION_URI)
        val key = field("API Key", "api_key", "sk-…", EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
        val model = field("模型", "model", "gpt-4o-mini / glm-4 / deepseek-chat …", EditorInfo.TYPE_CLASS_TEXT)

        AlertDialog.Builder(this)
            .setTitle("LLM 接口设置")
            .setView(wrap)
            .setPositiveButton("保存") { _, _ ->
                prefs.edit()
                    .putString("base_url", url.text.toString().trim())
                    .putString("api_key", key.text.toString().trim())
                    .putString("model", model.text.toString().trim())
                    .apply()
                Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // ================= 笔记浏览 =================

    private fun setupNotes() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { render() }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })
    }

    private fun render() {
        if (stack.isEmpty()) stack.add(repo.tree())
        renderBreadcrumb()
        listBox.removeAllViews()
        val q = searchInput.text.toString().trim()
        val cur = stack.last()
        if (q.isNotEmpty()) {
            repo.search(q).forEach { (n, path) ->
                addRow(n, path) { openDetail(n) }
            }
            return
        }
        val items = cur.children.sortedWith(compareByDescending<NotesRepo.Node> { it.isFolder }.thenByDescending { it.date })
        if (items.isEmpty()) {
            val tv = TextView(this)
            tv.text = "此分类下暂无笔记\n结束一次对话后会自动归档到这里"
            tv.gravity = android.view.Gravity.CENTER
            tv.setPadding(0, 160, 0, 160)
            listBox.addView(tv)
        }
        items.forEach { it ->
            if (it.isFolder) addRow(it, null) { stack.add(it); searchInput.setText(""); render() }
            else addRow(it, null) { openDetail(it) }
        }
    }

    private fun addRow(n: NotesRepo.Node, sub: String?, onClick: () -> Unit) {
        val row = layoutInflater.inflate(R.layout.row_note, listBox, false) as LinearLayout
        row.findViewById<TextView>(R.id.rowTitle).text = n.name
        row.findViewById<TextView>(R.id.rowSub).text = when {
            sub != null -> sub
            n.isFolder -> "${countNotes(n)} 篇笔记"
            else -> n.date
        }
        row.setOnClickListener { onClick() }
        listBox.addView(row)
    }

    private fun countNotes(n: NotesRepo.Node): Int =
        n.children.sumOf { if (it.isFolder) countNotes(it) else 1 }

    private fun renderBreadcrumb() {
        pathBar.removeAllViews()
        stack.forEachIndexed { i, node ->
            if (i > 0) pathBar.addView(TextView(this).apply { text = " / " })
            pathBar.addView(TextView(this).apply {
                text = if (i == 0) "知识库" else node.name
                textSize = 13f
                setPadding(8, 8, 8, 8)
                if (i < stack.size - 1) setOnClickListener {
                    stack = stack.subList(0, i + 1).toMutableList()
                    render()
                }
            })
        }
    }

    private fun openDetail(n: NotesRepo.Node) {
        val path = stack.drop(1).joinToString(" / ") { it.name }
        markwon.setMarkdown(detailBody, repo.readNote(n.file))
        scrollView.visibility = View.GONE
        detailScroll.visibility = View.VISIBLE
    }

    fun onBackFromDetail(v: View) {
        detailScroll.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            detailScroll.visibility == View.VISIBLE -> onBackFromDetail(detailScroll)
            stack.size > 1 -> { stack.removeAt(stack.size - 1); render() }
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }
}
