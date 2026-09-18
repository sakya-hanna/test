import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLClassLoader;
public class RunJavaTests {
    public static void main(String[] args) throws Exception {
        var compiler=ToolProvider.getSystemJavaCompiler();
        if(compiler==null) throw new IllegalStateException("需要完整 JDK 17 或更新版本");
        Path out=Files.createTempDirectory("chatnotes-test-classes-");
        try {
            int result=compiler.run(null,System.out,System.err,"-encoding","UTF-8","-d",out.toString(),
                "app/src/main/java/com/willam/chatnotes/NoteFiles.java","tests/NoteFilesChecks.java");
            if(result!=0) throw new AssertionError("Java compilation failed: "+result);
            try(var loader=new URLClassLoader(new java.net.URL[]{out.toUri().toURL()})) {
                loader.loadClass("NoteFilesChecks").getMethod("main",String[].class).invoke(null,(Object)new String[0]);
            }
        } finally {
            try(var paths=Files.walk(out)) { for(Path p:paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(p); }
        }
    }
}
