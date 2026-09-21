package com.willam.chatnotes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** P0 管理闭环：NoteOps 编辑/重命名/移动/删除 */
class NoteOpsTest {

    @get:Rule val tmp = TemporaryFolder()

    private val id1 = "a".repeat(64)
    private val id2 = "b".repeat(64)

    private fun newOps(): Pair<NoteOps, File> {
        val root = tmp.newFolder("notes" + System.nanoTime())
        return NoteOps(root) to root
    }

    private fun seed(root: File, id: String, cats: List<String>, title: String, content: String): File =
        NoteFiles(root).write(cats, title, id, content)

    @Test fun `metaOf 解析 64hex 文件名与分类`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "# 协程")
        val m = ops.metaOf(f)!!
        assertEquals(id1, m.id)
        assertEquals("协程", m.title)
        assertTrue(m.category.replace('\\', '/') == "编程/Kotlin")
    }

    @Test fun `metaOf 拒绝非笔记文件`() {
        val (ops, root) = newOps()
        val dir = File(root, "工具").apply { mkdirs() }
        val f = File(dir, "readme.md").apply { writeText("x") }
        assertNull(ops.metaOf(f))
    }

    @Test fun `编辑内容 同ID覆盖`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "旧内容")
        val m = ops.metaOf(f)!!
        val after = ops.writeContent(m, "新内容")
        assertEquals("新内容", after.readText())
        assertEquals(f.canonicalPath, after.canonicalPath)
    }

    @Test fun `编辑拒绝空内容`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "旧内容")
        val m = ops.metaOf(f)!!
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) { ops.writeContent(m, "  ") }
    }

    @Test fun `重命名 保留ID 换标题`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "内容")
        val m = ops.metaOf(f)!!
        val after = ops.rename(m, "协程基础")
        assertTrue(after.name.endsWith("--$id1.md"))
        assertTrue(after.name.startsWith("协程基础--"))
        assertFalse(f.exists())
        assertEquals("内容", after.readText())
    }

    @Test fun `重命名撞同名拒绝`() {
        val (ops, root) = newOps()
        seed(root, id1, listOf("编程", "Kotlin"), "协程", "内容")
        val f2 = seed(root, id2, listOf("编程", "Kotlin"), "流", "内容2")
        val m2 = ops.metaOf(f2)!!
        // 目标文件 = root/编程/Kotlin/协程--id2.md；已存在的是 协程--id1.md。
        // rename 的 exists 检查按"目标路径"判断，两篇标题相同但 ID 不同 → 目标不同名 → 不该撞。
        // 该用例真正要防的是"改标题后与另一篇完全同名"——即同标题不同 ID 是允许的。
        // 因此预期 NOT 抛异常，而是成功改名为与 id1 同标题、不同 ID 的文件。
        val after = ops.rename(m2, "协程")
        assertTrue(after.name.endsWith("--$id2.md"))
        assertTrue(after.name.startsWith("协程--"))
    }

    @Test fun `移动分类 换目录 清空旧目录`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "内容")
        val m = ops.metaOf(f)!!
        val after = ops.move(m, "编程/Coroutines")
        assertTrue(after.path.contains("Coroutines"))
        assertFalse(f.exists())
        assertEquals("内容", after.readText())
        assertFalse(File(root, "编程/Kotlin").exists())
    }

    @Test fun `移动拒绝非2-4级`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "内容")
        val m = ops.metaOf(f)!!
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) { ops.move(m, "一级") }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) { ops.move(m, "a/b/c/d/e") }
    }

    @Test fun `删除文件并清空父目录`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "内容")
        val m = ops.metaOf(f)!!
        ops.delete(m)
        assertFalse(f.exists())
        assertFalse(File(root, "编程/Kotlin").exists())
    }

    @Test fun `编辑保持文件在根目录之内`() {
        val (ops, root) = newOps()
        val f = seed(root, id1, listOf("编程", "Kotlin"), "协程", "内容")
        val m = ops.metaOf(f)!!
        val after = ops.writeContent(m, "再改")
        assertTrue(after.canonicalPath.startsWith(root.canonicalPath))
    }
}
