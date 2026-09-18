package com.willam.chatnotes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 领域树笔记仓库:notes/ 目录下 文件夹=领域, .md=笔记。
 * 与 demo 中的树结构一致:计算机科学与技术/Agent 开发/RAG 检索/什么是RAG.md
 */
class NotesRepo(context: Context) {
    private val root: File = File(context.filesDir, "notes").apply { mkdirs() }

    data class Node(val name: String, val isFolder: Boolean, val file: File) {
        var date: String = ""
        var children: MutableList<Node> = mutableListOf()
    }

    fun tree(): Node {
        fun load(dir: File): Node {
            val node = Node(dir.name, true, dir)
            dir.listFiles()?.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name })?.forEach { f ->
                if (f.isDirectory) node.children.add(load(f))
                else if (f.extension == "md") {
                    val n = Node(f.nameWithoutExtension, false, f)
                    n.date = f.lastModified().let { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(it)) }
                    node.children.add(n)
                }
            }
            return node
        }
        return load(root)
    }

    fun search(q: String): List<Pair<Node, String>> {
        val hits = mutableListOf<Pair<Node, String>>()
        fun walk(dir: File, path: String) {
            dir.listFiles()?.forEach { f ->
                if (f.isDirectory) walk(f, "$path/${f.name}")
                else if (f.extension == "md") {
                    val name = f.nameWithoutExtension
                    val body = runCatching { f.readText() }.getOrDefault("")
                    if (name.contains(q) || body.contains(q)) {
                        val n = Node(name, false, f)
                        n.date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(f.lastModified()))
                        hits.add(n to path.trimStart('/'))
                    }
                }
            }
        }
        walk(root, "")
        return hits
    }

    /** 写入笔记;path 形如 "计算机科学与技术/Agent 开发/RAG 检索",叶子为笔记名 */
    fun writeNote(path: List<String>, markdown: String): File {
        require(path.size >= 2) { "path 至少包含 领域/笔记名" }
        val dirPath = path.dropLast(1)
        val dir = dirPath.fold(root) { acc, seg -> File(acc, seg).apply { mkdirs() } }
        val fileName = sanitize(path.last()) + ".md"
        val f = File(dir, fileName)
        f.writeText(markdown)
        return f
    }

    fun readNote(f: File): String = f.readText()

    companion object {
        fun sanitize(s: String): String = s.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().take(80).ifEmpty { "未命名" }
    }
}
