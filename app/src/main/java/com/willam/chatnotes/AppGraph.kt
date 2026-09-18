package com.willam.chatnotes

import android.content.Context
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AppGraph private constructor(context: Context) {
    val app: Context = context.applicationContext
    val db = ChatStore(app)
    val notes by lazy { NotesRepo(app) }
    val config = ConfigStore(app)
    val search by lazy { SearchIndex(app) }
    // Bounded queue: a web page cannot create unlimited pending disk operations.
    val queries = java.util.concurrent.Executors.newSingleThreadExecutor()
    val io = ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, ArrayBlockingQueue<Runnable>(512))
    init { io.execute {
        db.recoverInterruptedCapture()
        // The previous debug build logged fragments of every captured network message.
        java.io.File(app.filesDir, "intercept_log.txt").delete()
        runCatching { config.apiKey() } // Migrate the legacy key before any API request.
    } }
    companion object {
        @Volatile private var instance: AppGraph? = null
        fun get(context: Context): AppGraph = instance ?: synchronized(this) {
            instance ?: AppGraph(context).also { instance = it }
        }
    }
}
