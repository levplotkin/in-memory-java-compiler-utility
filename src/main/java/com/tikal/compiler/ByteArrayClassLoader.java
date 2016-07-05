package com.tikal.compiler;

import java.util.Map;

/**
 * @author levp
 */
public class ByteArrayClassLoader extends ClassLoader {
    private Map<String, byte[]> compiledClasses;

    /**
     * @param compiledClasses : map with compiled classes
     */
    public ByteArrayClassLoader(final Map<String, byte[]> compiledClasses) {
        this.compiledClasses = compiledClasses;

    }

    /**
     * try to load class from classesMap in case the parent class loader failed to find it by name
     *
     * @see ClassLoader#loadClass(String)
     */
    @Override
    public Class<?> loadClass(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            byte[] classData = compiledClasses.get(name);
            return (classData == null) ? null : defineClass(name, classData, 0, classData.length);
        }
    }
}
