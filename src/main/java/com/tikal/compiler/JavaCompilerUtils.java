package com.tikal.compiler;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JavaCompilerUtils {

    private static final Logger log = Logger.getLogger(JavaCompilerUtils.class.getName());

    private final static LocalDiagnosticListener LISTENER = new LocalDiagnosticListener(log);

    private final static JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

    private static final Map<String, byte[]> COMPILED_CLASSES_MAP = new ConcurrentHashMap<String, byte[]>();
    private static final Pattern CLASS_PATTERN = Pattern.compile("public\\s?class\\s?([$_a-zA-Z][$_a-zA-Z0-9]*)");

    public static String getClassNameFromSource(final String source) {

        Matcher matcher = CLASS_PATTERN.matcher(source);
        String className = null;

        if (matcher.find()) {
            className = matcher.group(1);
        }

        return className;
    }

    /**
     * @param source  java source code as string
     * @param options compiler options such as CLASSPATH
     * @return Map className->byteCode
     */
    public static Map<String, byte[]> compile(final String source, final List<String> options) throws Exception {

        String className = getClassNameFromSource(source);

        if (COMPILER == null)
            throw new Exception("Compiler not found. Make shure that tools.jar is in classpath");

        JavaCompiler.CompilationTask task;
        try {
            task = COMPILER.getTask(null, new MemoryFileManager(COMPILER.getStandardFileManager(LISTENER, null, null),
                    COMPILED_CLASSES_MAP), LISTENER, options, null, Collections.singletonList(new JavaSourceFileObject(className,
                    source)));
        } catch (URISyntaxException e) {
            throw new Exception(e.getMessage());
        }
        // compile and check result
        if (task.call().equals(Boolean.FALSE))
            throw new Exception("failed to compile " + className);

        return COMPILED_CLASSES_MAP;
    }
}

/**
 * A file object represents java class and stores it map during compilation
 */
class JavaClassFileObject extends SimpleJavaFileObject {
    private final String className;
    private final Map<String, byte[]> compiledClassMap;

    JavaClassFileObject(final String className, final Map<String, byte[]> compiledClassMap) throws URISyntaxException {
        super(new URI("mfm:///" + className.replace('.', '/') + JavaFileObject.Kind.CLASS.extension), Kind.CLASS);
        this.className = className;
        this.compiledClassMap = compiledClassMap;
    }

    @Override
    public OutputStream openOutputStream() {
        return new FilterOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                out.close();

                compiledClassMap.put(className, ((ByteArrayOutputStream) out).toByteArray());
            }
        };
    }
}

/**
 * A file object represents source (from a string)
 */
class JavaSourceFileObject extends SimpleJavaFileObject {

    final String sourceCode;

    JavaSourceFileObject(final String name, final String code) throws URISyntaxException {
        super(new URI("mfm:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = code;
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
        return sourceCode;
    }
}

/**
 * the listener will print compiler output
 */
class LocalDiagnosticListener implements DiagnosticListener<JavaFileObject> {
    private static final Locale LOCALE = Locale.ENGLISH;
    private Logger log;

    public LocalDiagnosticListener(Logger log) {

        this.log = log;
    }

    public void report(final Diagnostic<? extends JavaFileObject> diagnostic) {

        if (log.isLoggable(Level.INFO))
            log.log(Level.INFO, "line #" + diagnostic.getLineNumber() + "code: " + diagnostic.getCode() + " message: "
                    + diagnostic.getMessage(LOCALE), "source: " + diagnostic.getSource());
    }
}

/**
 * FileManager used for in memory compilation
 */
class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, byte[]> compiledClassMap;

    MemoryFileManager(final JavaFileManager fileManager, final Map<String, byte[]> compiledClassMap) {
        super(fileManager);
        this.compiledClassMap = compiledClassMap;
    }


    public JavaFileObject getJavaFileForOutput(final Location location, final String name, final JavaFileObject.Kind kind,
                                               final FileObject originatingSource) {
        if (originatingSource instanceof JavaSourceFileObject) {
            try {
                return new JavaClassFileObject(name, compiledClassMap);
            } catch (final URISyntaxException e) {
                throw new UnsupportedOperationException(e.getMessage());
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
}