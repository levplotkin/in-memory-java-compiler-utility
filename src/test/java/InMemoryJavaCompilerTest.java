import com.tikal.compiler.ByteArrayClassLoader;
import com.tikal.compiler.JavaCompilerUtils;
import org.junit.Test;

import java.io.Serializable;
import java.security.Policy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;

public class InMemoryJavaCompilerTest extends Policy implements Serializable {

    @Test
    @SuppressWarnings("unchecked")
    public void testCompileAndRunFromSourceFile() throws Exception {

        String classPath = InMemoryJavaCompilerTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        final String source = "" +
                "import javax.annotation.Resource;\n" +
                "import java.io.Serializable;\n" +
                "import java.security.Policy;\n" +
                "@Resource\n" +
                "public class HelloWorld extends Policy implements Serializable {\n" +
                "    public static void main(String[] args) {\n" +
                "        /* Prints 'Hello, World' to the terminal window.*/\n" +
                "        System.out.println(\"Hello, World\");\n" +
                "    }\n" +
                "}\n";
        String classNameFromSource = JavaCompilerUtils.getClassNameFromSource(source);

        System.out.println("Compile " + classNameFromSource);
        System.out.println(source);

        ValidationUtils.isSourceValid(source);

        System.out.println("source is valid");

        /**
         * compilation
         */

        // compiler options
        final List<String> options = Arrays.asList("-cp", classPath);

        Map<String, byte[]> compile = JavaCompilerUtils.compile(source, options);
        System.out.println("compilation complete");
        /**
         * after compilation
         */

        final ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(compile);

        final Class<?> policyClass = byteArrayClassLoader.loadClass(classNameFromSource);

        System.out.println("class loaded");

        assertTrue(ValidationUtils.isClassValid((Class<Policy>) policyClass));

        System.out.println("class is valid");
        /**
         * validate instance
         */
        Policy policy = (Policy) policyClass.newInstance();

        System.out.println("make new instance of compiled class");
        // now we can use instance of compiled class
        policy.refresh();
    }
}
