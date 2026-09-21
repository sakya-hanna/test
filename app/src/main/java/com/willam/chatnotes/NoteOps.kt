package com.willam.chatnotes

import java.io.File

/**
 * 笔记管理操作（P0 管理闭环）：编辑、删除、重命名、移动分类。
 *
 * 设计（docs/gap-analysis.md P0）：
 * - NoteFiles 的"不可变"原则在此显式放宽：管理操作允许同 ID 覆盖（新版本），
 *   文件名保持 <标题>--<jobId64hex>.md，jobId 不变 → 同步协议把它当作同文档新版本。
 * - 每个操作只做文件系统变更；索引由调用方随后跑 ensureIndexed 增量更新（孤儿清理覆盖删除）。
 */
class NoteOps(private val root: File) {

    /** 笔记元信息（从文件名/路径解析） */
    data class Meta(val id: String, val title: String, val category: String, val file: File)

    private val files = NoteFiles(root)

    fun metaOf(file: File): Meta? {
        val safe = runCatching { files.contained(file) }.getOrNull() ?: return null
        if (!safe.isFile) return null
        val name = safe.nameWithoutExtension
        val jobId = name.substringAfterLast("--", "")
        if (jobId.length != 64 || jobId.any { it !in '0'..'9' && it !in 'a'..'f' }) return null
        return Meta(
            id = jobId,
            title = name.dropLast(66).ifEmpty { name },
            category = safe.parentFile?.relativeTo(root)?.path ?: "",
            file = safe,
        )
    }

    private fun catsOf(category: String): List<String> =
        category.split(File.separatorChar, '/', '\\').map { it.trim() }.filter { it.isNotBlank() }

    /** 编辑正文：同 ID 覆盖写（NoteFiles.overwrite 原子替换）。 */
    fun writeContent(meta: Meta, markdown: String): File {
        require(markdown.isNotBlank() && markdown.length <= 500_000) { "笔记为空或超长" }
        val cats = catsOf(meta.category)
        require(cats.size in 2..4) { "分类层级须为 2-4 级（当前：${cats.size}）" }
        return files.overwrite(cats, meta.title, meta.id, markdown)
    }

    /** 重命名标题：文件名替换，jobId 不变。 */
    fun rename(meta: Meta, newTitleRaw: String): File {
        val newTitle = NoteFiles.segment(newTitleRaw, 40)
        require(newTitle.isNotEmpty()) { "标题不能为空" }
        val target = targetFile(catsOf(meta.category), newTitle, meta.id)
        require(!target.exists()) { "同名笔记已存在" }
        if (!meta.file.renameTo(target)) throw java.io.IOException("重命名失败（文件被占用？）")
        return target
    }

    /** 移动到新分类（"编程/Kotlin" 形式，2-4 级）。 */
    fun move(meta: Meta, newCategoryRaw: String): File {
        val cats = newCategoryRaw.split('/', '\\', File.separatorChar).map { it.trim() }.filter { it.isNotBlank() }
        require(cats.size in 2..4) { "分类须为 2-4 级，用 / 分隔" }
        cats.forEach { require(NoteFiles.segment(it, 60) == it) { "分类含非法字符: $it" } }
        val target = targetFile(cats, meta.title, meta.id)
        require(!target.exists()) { "目标分类下已有同名笔记" }
        target.parentFile?.let { if (!it.isDirectory && !it.mkdirs()) throw java.io.IOException("无法创建目标分类目录") }
        if (!meta.file.renameTo(target)) throw java.io.IOException("移动失败")
        cleanupEmptyDirs(meta.file.parentFile)
        return target
    }

    /** 删除笔记文件；顺手清掉空分类目录。 */
    fun delete(meta: Meta) {
        if (!meta.file.delete()) throw java.io.IOException("删除失败（文件被占用？）")
        cleanupEmptyDirs(meta.file.parentFile)
    }

    private fun targetFile(cats: List<String>, title: String, id: String): File {
        var dir = root
        for (part in cats) dir = File(dir, NoteFiles.segment(part, 60))
        return File(dir, NoteFiles.segment(title, 40) + "--" + id + ".md")
    }

    /** 自底向上删除空目录（不越过 notes root）。 */
    private fun cleanupEmptyDirs(dir: File?) {
        var d = dir ?: return
        val rootPath = root.canonicalPath
        while (d.canonicalPath != rootPath && d.canonicalPath.startsWith(rootPath)) {
            if (!d.isDirectory || d.list()?.isNotEmpty() == true) break
            if (!d.delete()) break
            d = d.parentFile ?: break
        }
    }
}
