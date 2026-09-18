package com.willam.chatnotes

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Drives the REAL MainActivity search box on device (shell `input text`
 * is blocked by the Sogou IME on this Huawei unit). Seeds real note files,
 * types queries through the UI thread, and asserts the rendered result
 * cards — end-to-end validation of debounce -> background query -> cards.
 */
@RunWith(AndroidJUnit4::class)
class SearchUiTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private lateinit var notesRoot: File

    @After fun teardown() { if (::notesRoot.isInitialized) notesRoot.deleteRecursively() }

    private fun seedNotes() {
        // Replace the graph's notes dir for this test run with seeded content.
        val dir = File(context.filesDir, "notes")
        if (dir.exists()) { // keep a backup of real user notes
            val backup = File(context.filesDir, "notes-backup-${UUID.randomUUID()}")
            dir.copyRecursively(backup)
            notesRoot = backup
            dir.deleteRecursively()
        }
        val file = File(dir, "计算机/Android/WebView白屏修复--${"f1".repeat(32)}.md")
        file.parentFile!!.mkdirs()
        file.writeText("# WebView 白屏修复\n\n渲染进程退出 onRenderProcessGone；错误码 0x80004002；C++ 与 C# 对比。")
        val file2 = File(dir, "计算机/编程语言/Kotlin协程--${"e1".repeat(32)}.md")
        file2.parentFile!!.mkdirs()
        file2.writeText("# Kotlin 协程\n\nstructured concurrency 与 cancel 传播。")
    }

    /** Collect all text in a view subtree (result cards are LinearLayouts). */
    private fun View.allText(): String {
        val out = StringBuilder()
        fun collect(v: View) {
            if (v is TextView) out.append(v.text).append('\n')
            else if (v is android.view.ViewGroup) for (i in 0 until v.childCount) collect(v.getChildAt(i))
        }
        collect(this)
        return out.toString()
    }

    @Test fun searchBoxRendersResultCards() {
        seedNotes()
        // Sanity: the seeded content must be findable through the index pipeline
        // before asserting anything about the UI layer.
        val graph = AppGraph.get(context)
        val hits = runCatching {
            graph.search.ensureIndexed(graph.notes, graph.db)
            graph.search.query("0x80004002")
        }.getOrDefault(emptyList())
        assertTrue("seed not findable via SearchIndex (hits=${hits.size})", hits.isNotEmpty())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Switch to the notes tab via the real tab button.
            val latch = CountDownLatch(1)
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.tabNotes).performClick()
                latch.countDown()
            }
            assertTrue(latch.await(5, TimeUnit.SECONDS))
            // Type a query on the UI thread, then wait past the 300ms debounce.
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.searchInput).setText("0x80004002")
            }
            Thread.sleep(1500)
            // Assert rendered result cards in listBox.
            scenario.onActivity { activity ->
                val box = activity.findViewById<LinearLayout>(R.id.listBox)
                val joined = (0 until box.childCount).joinToString("\n") { box.getChildAt(it).allText() }
                assertTrue("expected result card for WebView note, got: $joined", joined.contains("WebView 白屏修复"))
                assertTrue("expected hit snippet highlight text", joined.contains("0x80004002") || joined.contains("渲染进程"))
            }
            // Clear query -> browse list restored.
            scenario.onActivity { activity -> activity.findViewById<EditText>(R.id.searchInput).setText("") }
            Thread.sleep(1200)
            scenario.onActivity { activity ->
                val box = activity.findViewById<LinearLayout>(R.id.listBox)
                val joined = (0 until box.childCount).joinToString("\n") { box.getChildAt(it).allText() }
                assertTrue("browse tree missing after clearing query", joined.contains("计算机"))
            }
        }
    }

    @Test fun emptyResultShowsFriendlyHintNotCrash() {
        seedNotes()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it -> it.findViewById<View>(R.id.tabNotes).performClick() }
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.searchInput).setText("zzz不存在的查询xyz")
            }
            Thread.sleep(1500)
            scenario.onActivity { activity ->
                val box = activity.findViewById<LinearLayout>(R.id.listBox)
                val joined = (0 until box.childCount).joinToString("\n") { box.getChildAt(it).allText() }
                assertTrue("missing empty-result hint: $joined", joined.contains("没有找到匹配"))
            }
        }
    }
}
