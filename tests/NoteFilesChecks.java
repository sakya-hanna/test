import com.willam.chatnotes.NoteFiles;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class NoteFilesChecks {
    interface Attempt { void run() throws Exception; }
    static int checks;
    static void check(boolean ok, String name) { if (!ok) throw new AssertionError(name); checks++; System.out.println("PASS " + name); }
    static void rejects(Attempt test, String name) throws Exception {
        try { test.run(); } catch (IllegalArgumentException | java.io.IOException expected) { check(true, name); return; }
        throw new AssertionError(name + " did not reject");
    }
    public static void main(String[] args) throws Exception {
        Path workspace = Files.createTempDirectory("chatnotes-storage-");
        try {
            Path root = workspace.resolve("notes"); NoteFiles store = new NoteFiles(root.toFile());
            String id1="a".repeat(64), id2="b".repeat(64);
            File first=store.write(List.of("计算机", "编程"), "相同标题", id1, "第一份内容");
            File second=store.write(List.of("计算机", "编程"), "相同标题", id2, "第二份内容");
            check(!first.equals(second) && Files.readString(first.toPath()).equals("第一份内容"), "same titles retain both notes");
            check(first.equals(store.write(List.of("计算机","编程"),"相同标题",id1,"第一份内容")), "same job resumes idempotently");
            rejects(()->store.write(List.of("计算机","编程"),"相同标题",id1,"不应覆盖"), "existing note cannot be overwritten");
            check(Files.readString(first.toPath()).equals("第一份内容"), "failed overwrite leaves bytes unchanged");
            rejects(()->store.write(List.of("..","分类"),"测试",id1,"内容"), "parent traversal rejected");
            rejects(()->store.write(List.of("/tmp","分类"),"测试",id1,"内容"), "absolute directory rejected");
            rejects(()->store.write(List.of("分类"),"测试",id1,"内容"), "invalid depth rejected");
            rejects(()->store.write(List.of("分类","子类"),"测试","../bad","内容"), "untrusted job id rejected");
            File safe=store.write(List.of("C/C++","I/O"),"路径/分隔符", "c".repeat(64),"正文");
            check(safe.getCanonicalFile().toPath().startsWith(root), "labels containing slashes stay inside root");
            Files.createDirectories(workspace.resolve("outside"));
            Files.createSymbolicLink(root.resolve("escape"), workspace.resolve("outside"));
            rejects(()->store.write(List.of("escape","子类"),"测试",id1,"内容"), "symlink escape rejected");
            Files.createDirectories(root.resolve("冲突"));Files.writeString(root.resolve("冲突/file.md"),"保留");
            rejects(()->store.write(List.of("冲突","file.md"),"测试",id1,"内容"), "file used as directory reports IO failure");
            File unicode=store.write(List.of("🙂".repeat(90),"中文".repeat(90)),"🙂".repeat(90),"d".repeat(64),"内容");
            check(unicode.getName().getBytes(StandardCharsets.UTF_8).length<=255, "unicode filename fits filesystem byte limit");
            try(var stream=Files.walk(root)) { check(stream.noneMatch(p->p.getFileName().toString().startsWith(".pending-")), "no leftover temporary note files"); }
            System.out.println("Storage checks passed: " + checks);
        } finally {
            try(var paths=Files.walk(workspace)) { for(Path p:paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(p); }
        }
    }
}
