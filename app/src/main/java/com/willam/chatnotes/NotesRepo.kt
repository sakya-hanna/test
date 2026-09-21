package com.willam.chatnotes

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesRepo(context: Context, dirName: String = "notes") {
    private val files = NoteFiles(File(context.filesDir, dirName))
    val root: File get() = files.root()
    data class Node(val name: String, val isFolder: Boolean, val file: File) {
        var date: String = ""
        var children: MutableList<Node> = mutableListOf()
        /** 笔记节点的 jobId（文件名尾部 64hex），文件夹为 null。 */
        val id: String? get() = if (isFolder) null
            else Regex("--([a-f0-9]{64})$").find(file.nameWithoutExtension)?.groupValues?.get(1)
    }
    private fun node(f: File): Node = Node(
        if (f.isDirectory) f.name else f.nameWithoutExtension.replace(Regex("--[a-f0-9]{64}$"), ""), f.isDirectory, f
    ).apply { date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(f.lastModified())) }
    fun tree(): Node {
        val seen = mutableSetOf<String>()
        fun load(dir: File, depth: Int): Node {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            val n = node(dir)
            if (depth > 16 || !seen.add(dir.canonicalPath)) return n
            dir.listFiles()?.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name })?.forEach {
                val safe = runCatching { files.contained(it) }.getOrNull() ?: return@forEach
                // 回收站目录不在知识库树中渲染（经 设置→回收站 管理）
                if (it.name == NoteAdmin.TRASH_DIR) return@forEach
                if (safe.isDirectory) n.children.add(load(safe, depth + 1))
                else if (safe.extension == "md") n.children.add(node(safe))
            }
            return n
        }
        return load(root, 0)
    }
    fun allFiles(): List<File> {
        val result = mutableListOf<File>()
        fun walk(n: Node) { if (n.isFolder) n.children.forEach { walk(it) } else result.add(n.file) }
        walk(tree()); return result
    }
    /** All existing category paths (folder chains), for reuse by the summarizer. */
    fun categoryPaths(): List<List<String>> {
        val out = mutableListOf<List<String>>()
        fun walk(n: Node, chain: List<String>) {
            for (child in n.children) {
                if (!child.isFolder) continue
                val next = chain + child.name
                out.add(next)
                walk(child, next)
            }
        }
        walk(tree(), emptyList())
        return out
    }
    fun search(q: String): List<Pair<Node, String>> = allFiles().mapNotNull { f ->
        if (Thread.currentThread().isInterrupted) throw InterruptedException()
        val n = node(f)
        val text = runCatching { readNote(f) }.getOrDefault("")
        if (n.name.contains(q, ignoreCase = true) || text.contains(q, ignoreCase = true))
            n to f.parentFile!!.relativeTo(root).path else null
    }.sortedByDescending { it.first.file.lastModified() }
    fun writeNote(result: SummaryResult, id: String, markdown: String): File =
        files.write(result.path, result.title, id, markdown)
    fun readNote(file: File): String {
        val safe = files.contained(file)
        require(safe.extension == "md" && safe.length() <= 2 * 1024 * 1024) { "笔记格式不支持或文件过大" }
        return safe.readText(Charsets.UTF_8)
    }
}
