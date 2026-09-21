package com.willam.chatnotes

import java.io.File

/**
 * P2 管理增强：
 * 1. 回收站：删除先移入 notes 根下 ".trash/<原分类路径>/文件名"，不直接物理删除；
 *    同步语义 = 删除墓碑（服务器和其他设备视为已删），本机保留物理文件可恢复。
 * 2. 收藏/置顶：文件名不动，把记录写进 prefs（noteId -> pinned），列表渲染时置顶。
 */
class NoteAdmin(private val root: File, private val config: ConfigStore) {

    private val trashRoot: File get() = File(root, TRASH_DIR)

    // ---------- 回收站 ----------

    /** 删除→移入回收站。返回墓碑 docId（调用方继续走删除广播）。 */
    fun trash(meta: NoteOps.Meta): String {
        if (meta.file.canonicalPath.startsWith(trashRoot.canonicalPath)) {
            // 已在回收站的文件再次"删除"= 彻底删除
            meta.file.delete()
            return meta.id
        }
        val rel = meta.file.relativeToOrSelf(root)
        val dest = File(trashRoot, rel.path)
        dest.parentFile?.let { if (!it.isDirectory && !it.mkdirs()) throw java.io.IOException("无法创建回收站目录") }
        if (!meta.file.renameTo(dest)) throw java.io.IOException("移入回收站失败（文件被占用？）")
        unpin(meta.id)
        NoteOps(root).cleanupEmptyDirsPublic(meta.file.parentFile)
        return meta.id
    }

    /** 回收站条目：文件 + 原分类（从路径推断）。 */
    fun trashItems(): List<Pair<NoteOps.Meta, String>> {
        if (!trashRoot.isDirectory) return emptyList()
        val out = mutableListOf<Pair<NoteOps.Meta, String>>()
        val ops = NoteOps(root)
        trashRoot.walkTopDown().filter { it.isFile && it.extension == "md" }.forEach { f ->
            val meta = ops.metaOf(f) ?: return@forEach
            val origin = f.relativeToOrSelf(trashRoot).parent?.replace('\\', '/') ?: ""
            out.add(meta to origin)
        }
        return out.sortedByDescending { it.first.file.lastModified() }
    }

    /** 从回收站恢复：放回原分类（目录不存在则重建）。 */
    fun restore(meta: NoteOps.Meta): File {
        val cur = meta.file
        require(cur.canonicalPath.startsWith(trashRoot.canonicalPath)) { "不在回收站内" }
        val originRel = cur.relativeToOrSelf(trashRoot).path
        val dest = File(root, originRel)
        dest.parentFile?.let { if (!it.isDirectory && !it.mkdirs()) throw java.io.IOException("无法恢复原目录") }
        require(!dest.exists()) { "原位置已有同名文件" }
        if (!cur.renameTo(dest)) throw java.io.IOException("恢复失败")
        NoteOps(root).cleanupEmptyDirsPublic(cur.parentFile)
        return dest
    }

    fun emptyTrash(): Int {
        var n = 0
        if (trashRoot.isDirectory) trashRoot.walkTopDown().filter { it.isFile }.forEach { if (it.delete()) n++ }
        trashRoot.delete()
        return n
    }

    // ---------- 收藏/置顶 ----------

    private val KEY = "pinned_notes"
    private fun pinnedSet(): Set<String> =
        config.prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    fun isPinned(id: String): Boolean = pinnedSet().contains(id)

    fun pin(id: String) { config.prefs.edit().putStringSet(KEY, pinnedSet() + id).apply() }
    fun unpin(id: String) {
        val s = pinnedSet() - id
        config.prefs.edit().putStringSet(KEY, s).apply()
    }

    companion object {
        /** 回收站目录名：以 . 开头避免被当成普通分类渲染。 */
        const val TRASH_DIR = ".trash"
    }
}
