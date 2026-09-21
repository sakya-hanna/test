package com.willam.chatnotes

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.work.WorkManager
import io.noties.markwon.Markwon
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.Date
import java.util.concurrent.RejectedExecutionException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var graph: AppGraph
    private lateinit var markwon: Markwon
    private lateinit var webView: WebView
    private lateinit var chatScreen: View
    private lateinit var notesScreen: View
    private lateinit var captureInfo: TextView
    private lateinit var pathBar: LinearLayout
    private lateinit var listBox: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var detailScroll: ScrollView
    private lateinit var detailBody: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var selectedId = ""
    private var captureState = "正在初始化"
    private var captureWarning = false
    private var bridgeReady = false
    private var documentStart = false
    private var script = ""
    private var chatVisible = true
    private var folders = mutableListOf<String>()
    private var renderFuture: java.util.concurrent.Future<*>? = null
    private var renderGeneration = 0
    private var renderLimit = 200
    private var searchTask: Runnable? = null
    private var countTask: Runnable? = null
    private var lastIndexStatus: SearchIndex.Status? = null
    private var rateWindow = 0L
    private var rateCount = 0
    private var observedJobs = false
    private val savedJobs = mutableSetOf<String>()
    private val captureOwner = java.util.UUID.randomUUID().toString()
    private var acceptingCapture = true
    private var webViewDestroyed = false
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        fileCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data))
        fileCallback = null
    }
    private val exportPicker = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) exportTo(uri)
    }
    private fun ui(action: () -> Unit) = runOnUiThread { if (!isFinishing && !isDestroyed) action() }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    private fun error(message: String) {
        if (!isFinishing && !isDestroyed) AlertDialog.Builder(this).setTitle("操作未完成")
            .setMessage(message).setPositiveButton("好", null).show()
    }
    private fun <T> disk(work: () -> T, done: (T) -> Unit) {
        try {
            graph.io.execute {
                val result = runCatching(work)
                ui { result.fold(done) { error(it.message ?: "文件或数据库操作失败，已保存的数据仍保留") } }
            }
        } catch (_: RejectedExecutionException) { error("保存队列繁忙，请稍后重试；可重新加载会话补采集") }
    }
    private fun <T> query(work: () -> T, done: (T) -> Unit) {
        renderFuture?.cancel(true)
        renderFuture = graph.queries.submit {
            val result = runCatching(work)
            if (!Thread.currentThread().isInterrupted) ui {
                result.fold(done) { if (it !is InterruptedException) error(it.message ?: "读取笔记失败") }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        graph = AppGraph.get(this); markwon = Markwon.create(this)
        chatScreen = findViewById(R.id.chatScreen); notesScreen = findViewById(R.id.notesScreen)
        captureInfo = findViewById(R.id.captureInfo); pathBar = findViewById(R.id.pathBar)
        listBox = findViewById(R.id.listBox); searchInput = findViewById(R.id.searchInput)
        scrollView = findViewById(R.id.browseScroll); detailScroll = findViewById(R.id.detailScroll)
        detailBody = findViewById(R.id.detailBody)
        detailBody.setTextIsSelectable(true)
        selectedId = graph.config.prefs.getString("last_conversation", "") ?: ""
        setupWebView(savedInstanceState)
        findViewById<View>(R.id.tabChat).setOnClickListener { switchTab(true) }
        findViewById<View>(R.id.tabNotes).setOnClickListener { switchTab(false); render() }
        findViewById<View>(R.id.settingsBtn).setOnClickListener { showMenu() }
        captureInfo.setOnClickListener { if (webViewDestroyed) showRendererRecovery() }
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                renderGeneration++; renderLimit = 200
                searchTask?.let { handler.removeCallbacks(it) }
                searchTask = Runnable { render() }.also { handler.postDelayed(it, 300) }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        // Migration and unfinished job recovery run after the once-per-process capture recovery.
        disk({
            graph.db.jobs().filter { it.state in setOf("queued", "running", "writing") }.forEach { SummaryWorker.enqueue(graph.app, it.id) }
            if (graph.config.syncConfig() != null) SyncWorker.enqueueOnAppOpen(applicationContext) // 打开 app 自动对账（设计 §12.6）
        }) { }
        WorkManager.getInstance(this).getWorkInfosByTagLiveData("chatnotes-summary").observe(this) {
            disk({ graph.db.jobs() }) { jobs ->
                val completed = jobs.filter { it.state == "saved" }.map { it.id }.toSet()
                if (observedJobs && (completed - savedJobs).isNotEmpty()) { toast("笔记已归档，可在知识库中查看"); render() }
                savedJobs.addAll(completed); observedJobs = true
            }
        }
        render()
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(state: Bundle?) {
        webView = findViewById(R.id.webView)
        webView.settings.apply {
            javaScriptEnabled = true; domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = true // Only user-selected content URIs are granted by the system picker.
            javaScriptCanOpenWindowsAutomatically = false
            // Keep the real WebView UA; a fabricated browser version breaks capability detection.
        }
        bridgeReady = WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)
        documentStart = WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
        script = InterceptorJs.source(this)
        if (bridgeReady) {
            WebViewCompat.addWebMessageListener(webView, "chatnotesProxy", setOf("https://chatgpt.com")) { _, message, origin, mainFrame, _ ->
                if (mainFrame && origin.scheme == "https" && origin.host == "chatgpt.com" && origin.port in setOf(-1, 443)) {
                    val data = runCatching { message.data }.getOrNull()
                    if (data != null) intercept(data)
                }
            }
            if (documentStart) WebViewCompat.addDocumentStartJavaScript(webView, script, setOf("https://chatgpt.com"))
        } else {
            captureState = "当前 WebView 不支持安全采集，请更新 Android System WebView"
            captureWarning = true; refreshCount()
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isForMainFrame) return false
                val uri = request.url
                if (allowedNavigation(uri)) return false
                if (uri.scheme in setOf("http", "https")) runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    .onFailure { toast("无法打开外部链接") }
                else toast("不支持此链接类型")
                return true
            }
            override fun onPageFinished(view: WebView, url: String) {
                val uri = Uri.parse(url)
                if (bridgeReady && uri.scheme == "https" && uri.host == "chatgpt.com" && !documentStart) {
                    view.evaluateJavascript(script, null)
                    view.evaluateJavascript("window.__chatnotes && window.__chatnotes.captureDom()", null)
                }
            }
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                acceptingCapture = false
                closeCapture()
                (view.parent as? ViewGroup)?.removeView(view)
                view.destroy()
                webViewDestroyed = true
                captureState = "网页已退出，点此恢复；已保存的原文仍保留"
                captureWarning = true; refreshCount()
                showRendererRecovery()
                return true
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                fileCallback?.onReceiveValue(null); fileCallback = callback
                return try {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.mode == FileChooserParams.MODE_OPEN_MULTIPLE)
                        val types = params.acceptTypes.filter { it.contains('/') && !it.contains(';') }.toTypedArray()
                        if (types.isNotEmpty()) putExtra(Intent.EXTRA_MIME_TYPES, types)
                    }
                    filePicker.launch(intent); true
                } catch (_: Exception) { fileCallback?.onReceiveValue(null); fileCallback = null; false }
            }
        }
        if (state == null || webView.restoreState(state) == null) {
            loadLastOrHome()
        } else {
            // restoreState 只恢复导航历史不恢复页面内容：进程被杀重启后 WebView 停在
            // about:blank 白屏（联调实测）。检测到无效页则重新加载。
            webView.evaluateJavascript("(function(){return location.href})()") { href ->
                if (href == null || href.contains("about:blank") || href == "\"\"" || href == "null") {
                    runOnUiThread { loadLastOrHome() }
                }
            }
        }
    }
    private fun loadLastOrHome() {
        val last = graph.config.prefs.getString("last_url", "https://chatgpt.com") ?: "https://chatgpt.com"
        webView.loadUrl(if (allowedNavigation(Uri.parse(last))) last else "https://chatgpt.com")
    }
    private fun allowedNavigation(uri: Uri): Boolean = uri.scheme == "https" && uri.port in setOf(-1, 443) &&
        uri.host in setOf("chatgpt.com", "chat.openai.com", "auth.openai.com", "auth0.openai.com") && uri.userInfo == null
    private fun intercept(raw: String) {
        if (!acceptingCapture) return
        val now = SystemClock.elapsedRealtime()
        if (now - rateWindow >= 1000) { rateWindow = now; rateCount = 0 }
        rateCount++
        if (raw.length > 256 * 1024 || rateCount > 200) {
            captureState = "采集输入超过上限，部分内容未保存，请重新加载会话补采集"
            captureWarning = true; refreshCount(); return
        }
        val j = runCatching { JSONObject(raw) }.getOrElse {
            captureState = "收到无法解析的采集数据"; captureWarning = true; refreshCount(); return
        }
        when (j.text("type")) {
            "ready" -> { captureState = if (documentStart) "采集中" else "页面补采集模式"; captureWarning = !documentStart; refreshCount() }
            "notice" -> { captureState = j.text("message").take(160); captureWarning = true; refreshCount() }
            "active", "conversation", "message", "remap", "request" -> try {
                graph.io.execute {
                    val result = runCatching { graph.db.applyEvent(j.put("owner", captureOwner)) }
                    ui {
                        result.fold({ cid ->
                            if (j.text("type") == "active" || j.text("type") == "remap" && selectedId == j.text("from")) {
                                selectedId = cid
                                graph.config.prefs.edit().putString("last_conversation", cid).apply()
                            }
                            val url = j.text("url")
                            if (j.text("type") == "active" && url.isNotEmpty() && allowedNavigation(Uri.parse(url)))
                                graph.config.prefs.edit().putString("last_url", url).apply()
                            refreshCount()
                        }, {
                            captureState = "原文保存失败，请检查空间并重新加载会话补采集"
                            captureWarning = true; refreshCount()
                        })
                    }
                }
            } catch (_: RejectedExecutionException) {
                captureState = "保存队列已满，部分内容未保存，请稍后补采集"; captureWarning = true; refreshCount()
            }
        }
    }
    private fun refreshCount() {
        if (countTask != null) return
        countTask = Runnable {
            countTask = null
            val id = selectedId
            disk({ if (id.isEmpty()) 0 else graph.db.rounds(id) }) { count ->
                if (id == selectedId) {
                    captureInfo.text = if (count == 0) captureState else "$captureState · 本次已捕获 $count 轮问答"
                    captureInfo.setTextColor(if (captureWarning) Color.rgb(170, 80, 0) else Color.rgb(0, 130, 85))
                }
            }
        }.also { handler.postDelayed(it, 300) }
    }
    fun onCloseChat(view: View) {
        if (selectedId.isEmpty()) { toast("还没有已保存的会话"); return }
        if (webViewDestroyed) { prepareSummary(selectedId); return }
        webView.evaluateJavascript("window.__chatnotes && window.__chatnotes.flush()") { prepareSummary(selectedId) }
    }
    private fun showRendererRecovery() {
        if (!isFinishing && !isDestroyed) AlertDialog.Builder(this)
            .setTitle("网页需要重新加载")
            .setMessage("已落盘的原文和笔记会保留。恢复后请核对未完成的回复；不会自动重发提问。")
            .setPositiveButton("恢复网页") { _, _ -> recreate() }
            .setNegativeButton("稍后", null).show()
    }
    private fun prepareSummary(cid: String) {
        disk({ graph.config.read() to graph.db.snapshot(cid) }) { (config, snapshot) ->
            val text = "将把此分支的 ${snapshot.messages.size} 条消息发送到 ${Uri.parse(config.baseUrl).host} 整理。\n" +
                "原始对话会保留，后续新消息可再次归档。长对话会分段调用 API。\n\n" +
                snapshot.warnings.joinToString("\n") { "注意：$it" }
            AlertDialog.Builder(this).setTitle("总结“${snapshot.title}”？").setMessage(text)
                .setPositiveButton("总结并归档") { _, _ ->
                    disk({
                        val job = graph.db.createJob(snapshot, config)
                        if (job.state != "saved") {
                            // Reuse the immutable snapshot and completed checkpoints on manual retry.
                            if (job.state == "failed") graph.db.updateJob(job.id, "queued")
                            SummaryWorker.enqueue(graph.app, job.id)
                        }
                        job
                    }) { job ->
                        toast(if (job.state == "saved") "这份原文快照已归档，无需重复生成" else "已加入整理任务，离开此页面后仍可继续")
                        switchTab(false); render()
                    }
                }.setNeutralButton("查看原文") { _, _ -> showConversation(cid) }.setNegativeButton("取消", null).show()
        }
    }
    private fun showMenu() {
        val actions = arrayOf("LLM 接口设置", "向量检索设置", "后台同步设置", "已保存对话", "整理任务与重试", "补采集当前网页", "重建搜索索引", "重建语义索引", "导出原文与笔记 ZIP")
        AlertDialog.Builder(this).setTitle("ChatNotes").setItems(actions) { _, index ->
            when (index) {
                0 -> showSettings()
                1 -> showEmbedSettings()
                2 -> showSyncSettings()
                3 -> showConversations()
                4 -> showJobs()
                5 -> {
                    switchTab(true)
                    if (webViewDestroyed) showRendererRecovery()
                    else if (bridgeReady) webView.evaluateJavascript("window.__chatnotes && window.__chatnotes.captureDom()", null)
                    else toast("请先更新 Android System WebView")
                }
                6 -> {
                    toast("正在后台重建索引")
                    disk({ graph.search.rebuild(graph.notes, graph.db) }) { status ->
                        toast("索引完成：${status.indexedNotes} 篇笔记、${status.indexedConversations} 个对话${if (status.fts) "" else "（本机不支持 FTS5，已降级为子串匹配）"}")
                        render()
                    }
                }
                7 -> rebuildSemanticIndex()
                8 -> exportPicker.launch("ChatNotes-backup-${System.currentTimeMillis()}.zip")
            }
        }.show()
    }

    private fun rebuildSemanticIndex() {
        val api = graph.embedApi()
        if (api == null) { toast("请先在“向量检索设置”中配置 embedding 服务"); showEmbedSettings(); return }
        toast("正在后台重建语义索引（分批调用向量接口）")
        disk({
            graph.search.clearEmbeddings(api.modelId)
            graph.search.ensureEmbedded(graph.notes, graph.db, api)
        }) { status ->
            toast("语义索引完成：${status.chunks} 个片段（维度 ${status.dim}）")
            render()
        }
    }

    private fun showEmbedSettings() {
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 24, 40, 16) }
        fun field(label: String, value: String, hint: String, type: Int): EditText {
            wrap.addView(TextView(this).apply { text = label })
            return EditText(this).apply { inputType = type; setText(value); this.hint = hint; wrap.addView(this) }
        }
        val prefs = graph.config.prefs
        val url = field("向量接口 Base URL（HTTPS，可留空关闭）", prefs.getString("embed_base_url", "") ?: "", "https://dashscope.aliyuncs.com/compatible-mode/v1", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        val key = field("向量接口 API Key（可复用整理服务的 Key）", runCatching { graph.config.embedApiKey() }.getOrDefault(""), "由服务商提供", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val model = field("向量模型（如 text-embedding-v4）", prefs.getString("embed_model", "") ?: "", "text-embedding-v4", InputType.TYPE_CLASS_TEXT)
        AlertDialog.Builder(this).setTitle("向量检索设置").setView(ScrollView(this).apply { addView(wrap) })
            .setPositiveButton("保存", null).setNegativeButton("取消", null)
            .setNeutralButton("清空配置") { _, _ ->
                disk({ graph.config.saveEmbed("", "", "") }) { toast("已关闭语义检索，使用关键词搜索") }
            }
            .create().apply {
                setOnShowListener { getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    disk({ graph.config.saveEmbed(url.text.toString(), key.text.toString(), model.text.toString()) }) {
                        dismiss(); toast(if (graph.config.embedConfigured()) "向量设置已保存，可在菜单重建语义索引" else "已清空，使用关键词搜索")
                    }
                } }
                show()
            }
    }

    private fun showSyncSettings() {
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 24, 40, 16) }
        fun field(label: String, value: String, hint: String, type: Int): EditText {
            wrap.addView(TextView(this).apply { text = label })
            return EditText(this).apply { inputType = type; setText(value); this.hint = hint; wrap.addView(this) }
        }
        val prefs = graph.config.prefs
        val url = field("后台同步地址（可留空关闭同步）", prefs.getString("sync_base_url", "") ?: "", "https://1.2.3.4:8443", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        val token = field("同步令牌", runCatching { graph.config.syncToken() }.getOrDefault(""), "服务器 token.txt 的内容", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        AlertDialog.Builder(this).setTitle("后台同步设置").setView(ScrollView(this).apply { addView(wrap) })
            .setPositiveButton("保存并立即同步", null).setNegativeButton("取消", null)
            .setNeutralButton("清空配置") { _, _ ->
                disk({ graph.config.saveSync("", "") }) { toast("已关闭后台同步，笔记仅存本机") }
            }
            .create().apply {
                setOnShowListener { getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    disk({
                        graph.config.saveSync(url.text.toString(), token.text.toString())
                        SyncWorker.enqueueOnAppOpen(applicationContext)
                    }) {
                        dismiss()
                        toast(if (graph.config.syncConfig() != null) "同步设置已保存，正在后台同步" else "已清空，笔记仅存本机")
                    }
                } }
                show()
            }
    }

    // ---- P0 笔记管理：编辑 / 重命名 / 移动 / 删除 ----

    /** 笔记长按菜单。所有操作完成后：刷新索引 + 触发同步（编辑/移动产生新版本，删除产生墓碑）。 */
    private fun showNoteActions(file: File) {
        val ops = NoteOps(graph.notes.root)
        val meta = diskOnce { ops.metaOf(file) } ?: run { toast("不是可管理的笔记文件"); return }
        val actions = arrayOf("编辑内容", "重命名", "移动分类", "删除")
        AlertDialog.Builder(this).setTitle(meta.title).setItems(actions) { _, i ->
            when (i) {
                0 -> editNoteDialog(ops, meta)
                1 -> renameNoteDialog(ops, meta)
                2 -> moveNoteDialog(ops, meta)
                3 -> deleteNoteConfirm(ops, meta)
            }
        }.show()
    }

    private fun editNoteDialog(ops: NoteOps, meta: NoteOps.Meta) {
        diskOnce({ graph.notes.readNote(meta.file) }) { markdown ->
            val input = EditText(this).apply {
                setText(markdown)
                minLines = 12; gravity = android.view.Gravity.TOP
                setSingleLine(false)
                setTextIsSelectable(false)
            }
            val wrap = ScrollView(this).apply { addView(input) }
            AlertDialog.Builder(this).setTitle("编辑：${meta.title}")
                .setView(wrap)
                .setPositiveButton("保存") { _, _ ->
                    val next = input.text.toString()
                    disk({
                        ops.writeContent(meta, next)
                        graph.search.ensureIndexed(graph.notes, graph.db)
                    }, {
                        toast("已保存")
                        SyncWorker.enqueueAfterNoteChange(applicationContext)
                        render()
                    })
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun renameNoteDialog(ops: NoteOps, meta: NoteOps.Meta) {
        val input = EditText(this).apply { setText(meta.title) }
        AlertDialog.Builder(this).setTitle("重命名").setView(input)
            .setPositiveButton("保存") { _, _ ->
                val next = input.text.toString().trim()
                if (next.isEmpty() || next == meta.title) return@setPositiveButton
                disk({
                    ops.rename(meta, next)
                    graph.search.ensureIndexed(graph.notes, graph.db)
                }, {
                    toast("已重命名")
                    SyncWorker.enqueueAfterNoteChange(applicationContext)
                    render()
                })
            }
            .setNegativeButton("取消", null).show()
    }

    private fun moveNoteDialog(ops: NoteOps, meta: NoteOps.Meta) {
        val input = EditText(this).apply { setText(meta.category); hint = "如：编程/Kotlin（2-4 级，/ 分隔）" }
        AlertDialog.Builder(this).setTitle("移动分类").setView(input)
            .setPositiveButton("移动") { _, _ ->
                val next = input.text.toString().trim()
                if (next.isEmpty() || next == meta.category) return@setPositiveButton
                disk({
                    ops.move(meta, next)
                    graph.search.ensureIndexed(graph.notes, graph.db)
                }, {
                    toast("已移动到 $next")
                    SyncWorker.enqueueAfterNoteChange(applicationContext)
                    folders.clear(); render()
                })
            }
            .setNegativeButton("取消", null).show()
    }

    private fun deleteNoteConfirm(ops: NoteOps, meta: NoteOps.Meta) {
        AlertDialog.Builder(this).setTitle("删除笔记")
            .setMessage("「${meta.title}」将被删除。已同步到后台的副本会一并标记删除；此操作不可撤销。")
            .setPositiveButton("删除") { _, _ ->
                disk({
                    ops.delete(meta)
                    graph.search.ensureIndexed(graph.notes, graph.db) // 孤儿清理：索引/向量同步移除
                }, {
                    toast("已删除")
                    SyncWorker.enqueueAfterNoteChange(applicationContext) // planDeletes 广播墓碑
                    render()
                })
            }
            .setNegativeButton("取消", null).show()
    }

    /** 同步一次性后台任务（不走渲染队列，避免打断列表）。带回调版。 */
    private fun <T> diskOnce(work: () -> T, done: (T) -> Unit) {
        try {
            graph.io.execute { val r = runCatching(work); ui { r.fold(done) { error(it.message ?: "操作失败") } } }
        } catch (_: java.util.concurrent.RejectedExecutionException) { error("后台忙，请稍后重试") }
    }

    /** 同步一次性后台任务（无回调）。 */
    private fun <T> diskOnce(work: () -> T): T? {
        var result: T? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        try {
            graph.io.execute { result = runCatching(work).getOrNull(); latch.countDown() }
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        } catch (_: Exception) { }
        return result
    }

    private fun showConversations() {
        disk({ graph.db.conversations() }) { conversations ->
            if (conversations.isEmpty()) { toast("尚无已保存的原文"); return@disk }
            AlertDialog.Builder(this).setTitle("已保存对话（含原文）")
                .setItems(conversations.map { "${it.title} · ${it.count} 轮问答" }.toTypedArray()) { _, i -> showConversation(conversations[i].id) }
                .setNegativeButton("关闭", null).show()
        }
    }
    private fun showConversation(cid: String) {
        disk({
            val conversation = graph.db.conversations().firstOrNull { it.id == graph.db.resolve(cid) }
            val messages = graph.db.allMessages(cid)
            val content = buildString {
                append("保留了已捕获的各个分支；总结使用最后浏览或生成的分支。\n\n")
                for (m in messages) {
                    if (length > 100000) { append("\n显示已截短，完整原文可从菜单导出。\n"); break }
                    append(if (m.role == "user") "【用户" else "【助手")
                    append(" · ${m.status}】\n${m.text}\n\n")
                }
            }
            conversation to content
        }) { (conversation, content) ->
            val body = TextView(this).apply { text = content; textSize = 14f; setPadding(24, 24, 24, 24); setTextIsSelectable(true) }
            AlertDialog.Builder(this).setTitle(conversation?.title ?: "原始对话")
                .setView(ScrollView(this).apply { addView(body) })
                .setPositiveButton("总结当前分支") { _, _ -> prepareSummary(cid) }
                .setNeutralButton("打开原会话") { _, _ ->
                    val url = conversation?.url.orEmpty()
                    if (webViewDestroyed) showRendererRecovery()
                    else if (url.isNotEmpty() && allowedNavigation(Uri.parse(url))) { switchTab(true); webView.loadUrl(url) }
                    else toast("该会话尚未取得原平台地址，原文可导出")
                }.setNegativeButton("关闭", null).show()
        }
    }
    private fun showJobs() {
        disk({ graph.db.jobs() }) { jobs ->
            if (jobs.isEmpty()) { toast("暂无整理任务"); return@disk }
            val labels = mapOf("queued" to "等待继续", "running" to "整理中", "writing" to "保存中", "failed" to "失败，可重试", "saved" to "已归档")
            AlertDialog.Builder(this).setTitle("整理任务")
                .setItems(jobs.map { "${labels[it.state] ?: it.state} · ${runCatching { JSONObject(it.snapshot).text("title") }.getOrDefault("会话")}" }.toTypedArray()) { _, index ->
                    val job = jobs[index]
                    if (job.state == "saved") { switchTab(false); render() }
                    else AlertDialog.Builder(this).setTitle(labels[job.state]).setMessage(job.error.ifBlank { "已保存原文与处理进度，可离开页面。" })
                        .setPositiveButton("继续 / 重试") { _, _ ->
                            disk({ graph.config.read(); graph.db.updateJob(job.id, "queued"); SummaryWorker.enqueue(graph.app, job.id) }) { toast("任务已提交") }
                        }.setNegativeButton("关闭", null).show()
                }.setNegativeButton("关闭", null).show()
        }
    }
    private fun showSettings() {
        disk({ runCatching { graph.config.apiKey() } }) { keyResult ->
            if (keyResult.isFailure) toast("旧密钥无法读取，请重新输入后保存")
            val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 24, 40, 16) }
            fun field(label: String, value: String, hint: String, type: Int): EditText {
                wrap.addView(TextView(this).apply { text = label })
                return EditText(this).apply { inputType = type; setText(value); this.hint = hint; wrap.addView(this) }
            }
            val prefs = graph.config.prefs
            val url = field("API Base URL（HTTPS）", prefs.getString("base_url", "") ?: "", "https://api.openai.com/v1", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
            val key = field("API Key", keyResult.getOrDefault(""), "由你选择的服务商提供", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            val model = field("模型名称", prefs.getString("model", "") ?: "", "填写服务商支持的模型 ID", InputType.TYPE_CLASS_TEXT)
            val dialog = AlertDialog.Builder(this).setTitle("整理模型设置").setView(ScrollView(this).apply { addView(wrap) })
                .setPositiveButton("保存", null).setNegativeButton("取消", null).create()
            dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val base = url.text.toString(); val secret = key.text.toString(); val name = model.text.toString()
                disk({ graph.config.save(base, secret, name) }) { dialog.dismiss(); toast("设置已保存") }
            } }
            dialog.show()
        }
    }
    private fun exportTo(uri: Uri) {
        val resolver = applicationContext.contentResolver
        disk({
            val output = requireNotNull(resolver.openOutputStream(uri, "w")) { "无法创建导出文件" }
            ZipOutputStream(output.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry("conversations.json")); zip.write(graph.db.exportJson().toByteArray(Charsets.UTF_8)); zip.closeEntry()
                graph.notes.allFiles().forEach { file ->
                    zip.putNextEntry(ZipEntry("notes/" + file.relativeTo(graph.notes.root).invariantSeparatorsPath))
                    file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
                }
            }
        }) { toast("原文与笔记已导出，未导出接口配置与登录状态") }
    }
    private fun switchTab(chat: Boolean) {
        chatVisible = chat
        chatScreen.visibility = if (chat) View.VISIBLE else View.GONE
        notesScreen.visibility = if (chat) View.GONE else View.VISIBLE
        findViewById<View>(R.id.tabChat).alpha = if (chat) 1f else 0.45f
        findViewById<View>(R.id.tabNotes).alpha = if (chat) 0.45f else 1f
    }
    private fun render() {
        val generation = ++renderGeneration
        val query = searchInput.text.toString().trim(); val desired = folders.toList(); val limit = renderLimit
        query({
            val root = graph.notes.tree(); var current = root; val actual = mutableListOf<String>()
            for (name in desired) {
                val child = current.children.firstOrNull { it.isFolder && it.name == name } ?: break
                actual.add(name); current = child
            }
            val rows: List<Pair<Any, String>> = if (query.isNotEmpty()) {
                // Hybrid search: keyword + semantic (RRF), degrades to keyword offline.
                graph.search.ensureIndexed(graph.notes, graph.db)
                graph.search.hybridQuery(query, graph.embedApi()).map { it to "" }
            } else current.children
                .sortedWith(compareByDescending<NotesRepo.Node> { it.isFolder }.thenByDescending { it.file.lastModified() }).map { it to "" }
            actual to rows
        }) { (actual, rows) ->
            if (generation != renderGeneration) return@query
            folders = actual; pathBar.removeAllViews(); listBox.removeAllViews()
            (listOf("知识库") + folders).forEachIndexed { i, name ->
                pathBar.addView(TextView(this).apply {
                    text = if (i == 0) name else " / $name"; textSize = 13f; setPadding(8, 12, 8, 12)
                    setOnClickListener { folders = folders.take(i).toMutableList(); renderLimit = 200; searchInput.setText(""); render() }
                })
            }
            if (rows.isEmpty()) listBox.addView(TextView(this).apply {
                val statusExtra = if (query.isEmpty()) ""
                else {
                    val s = lastIndexStatus
                    when {
                        s == null -> ""
                        !s.fts -> "\n（本机不支持 FTS5，当前为子串匹配模式）"
                        s.dirty -> "\n（索引待更新，结果可能不全）"
                        else -> ""
                    }
                }
                text = if (query.isEmpty()) "暂无笔记。可在菜单中查看已保存对话和整理任务。"
                else "没有找到匹配的笔记或对话$statusExtra"
                setPadding(24, 80, 24, 24)
            })
            rows.take(limit).forEach { (item, _) ->
                when (item) {
                    is SearchHit -> listBox.addView(searchCard(item))
                    is NotesRepo.Node -> {
                        val n = item
                        val row = layoutInflater.inflate(R.layout.row_note, listBox, false)
                        row.findViewById<TextView>(R.id.rowTitle).text = n.name
                        row.findViewById<TextView>(R.id.rowSub).text = if (n.isFolder) "分类" else n.date
                        row.setOnClickListener {
                            if (n.isFolder) { folders.add(n.name); renderLimit = 200; render() }
                            else disk({ graph.notes.readNote(n.file) }) { markdown ->
                                markwon.setMarkdown(detailBody, markdown); scrollView.visibility = View.GONE; detailScroll.visibility = View.VISIBLE
                            }
                        }
                        if (!n.isFolder) row.setOnLongClickListener { showNoteActions(n.file); true }
                        listBox.addView(row)
                    }
                }
            }
            if (rows.size > limit) listBox.addView(Button(this).apply {
                text = "显示更多（已显示 $limit / ${rows.size}）"; setOnClickListener { renderLimit += 200; render() }
            })
        }
    }

    /** Result card for one search hit, with keyword highlight and source jump. */
    private fun searchCard(hit: SearchHit): View {
        val card = layoutInflater.inflate(R.layout.row_search_hit, listBox, false)
        val title = card.findViewById<TextView>(R.id.hitTitle)
        val sub = card.findViewById<TextView>(R.id.hitSnippet)
        val meta = card.findViewById<TextView>(R.id.hitMeta)
        val more = card.findViewById<TextView>(R.id.hitMore)
        title.text = hit.title
        meta.text = buildString {
            append(if (hit.kind == "note") "笔记" else "对话")
            append(" · ")
            append(java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(hit.updatedAt)))
            if (hit.category.isNotBlank() && hit.kind == "note") { append(" · "); append(hit.category) }
        }
        fun span(text: String, start: Int, end: Int) = android.text.SpannableString(text).apply {
            if (end > start && end <= length)
                setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    start, end, android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        sub.text = span(hit.snippet, hit.hitStart, hit.hitEnd)
        if (hit.moreHits > 0) {
            more.visibility = View.VISIBLE
            more.text = "展开另外 ${hit.moreHits} 处命中"
            var expanded = false
            more.setOnClickListener {
                expanded = !expanded
                if (expanded) {
                    disk({ runCatching { graph.notes.readNote(hit.file) } }) { result ->
                        result.fold({ markdown ->
                            val windows = SearchLogic.snippets(markdown, searchInput.text.toString().trim())
                            if (windows.size > 1) {
                                more.text = "收起"
                                (more.parent as? android.view.ViewGroup)?.let { parent ->
                                    val start = parent.indexOfChild(more)
                                    windows.drop(1).forEachIndexed { i, w ->
                                        val extra = TextView(this)
                                        extra.tag = "extra-snippet"
                                        extra.text = span(w.first, w.second, w.third)
                                        extra.textSize = 13f
                                        extra.setPadding(12, 4, 12, 4)
                                        extra.setTextColor(android.graphics.Color.rgb(0x4B, 0x55, 0x63))
                                        parent.addView(extra, start + 1 + i)
                                    }
                                }
                            } else more.text = "其他命中在笔记其他位置"
                        }, { toast("笔记文件已被移动或删除，可重建索引") })
                    }
                } else {
                    more.text = "展开另外 ${hit.moreHits} 处命中"
                    (card.parent as? android.view.ViewGroup)?.let { parent ->
                        // Remove extra snippet views added after this card until the next card.
                        val start = parent.indexOfChild(card)
                        var i = start + 1
                        while (i < parent.childCount) {
                            val child = parent.getChildAt(i)
                            if (child.tag == "extra-snippet") { parent.removeViewAt(i); continue }
                            break
                        }
                    }
                }
            }
        }
        card.setOnClickListener {
            when (hit.kind) {
                "note" -> disk({ runCatching { graph.notes.readNote(hit.file) } }) { result ->
                    result.fold({ markdown ->
                        markwon.setMarkdown(detailBody, markdown); scrollView.visibility = View.GONE; detailScroll.visibility = View.VISIBLE
                    }, { toast("笔记文件已被移动或删除，可重建索引") })
                }
                "conv" -> showConversation(hit.conversationId)
            }
        }
        if (hit.kind == "note") card.setOnLongClickListener { showNoteActions(hit.file); true }
        return card
    }
    fun onBackFromDetail(view: View) { detailScroll.visibility = View.GONE; scrollView.visibility = View.VISIBLE }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            !chatVisible && detailScroll.visibility == View.VISIBLE -> onBackFromDetail(detailScroll)
            !chatVisible && folders.isNotEmpty() -> { folders.removeAt(folders.lastIndex); render() }
            chatVisible && !webViewDestroyed && webView.canGoBack() -> webView.goBack()
            !chatVisible -> switchTab(true)
            else -> super.onBackPressed()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        if (!webViewDestroyed) webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }
    private fun closeCapture() {
        val cleanup = Runnable { runCatching { graph.db.closeCapture(captureOwner) } }
        try { graph.io.execute(cleanup) }
        catch (_: RejectedExecutionException) { Thread { graph.io.queue.put(cleanup) }.start() }
    }
    override fun onDestroy() {
        acceptingCapture = false
        closeCapture()
        renderFuture?.cancel(true)
        handler.removeCallbacksAndMessages(null)
        fileCallback?.onReceiveValue(null); fileCallback = null
        if (!webViewDestroyed) {
            webView.stopLoading()
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        super.onDestroy()
    }
}
