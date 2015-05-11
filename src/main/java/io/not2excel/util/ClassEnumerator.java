package io.not2excel.util;
/*
 * Copyright (C) 2014-2015 Not2EXceL - Richmond Steele
 * **Totally didn't steal this from godshawk**
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class of static methods to enumerate, load, and possibly filter Class files
 *
 * @author not2excel
 * @version 0.0.1
 * @since 0.0.1
 */
public final class ClassEnumerator {

    private static final Logger logger = Logger.getLogger("ClassEnumerator");

    /**
     * Filters a list of classes by annotation
     *
     * @param input      class list found by ClassEnumerator
     * @param annotation annotation to filter by
     * @return filtered list
     * @since 0.0.1
     */
    public static List<Class<?>> filterByAnnotation(final List<Class<?>> input,
                                                    Class<? extends Annotation> annotation) {
        return input.stream().filter(c -> c.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    /**
     * Filters a ClassData object for classes that have the passed annotation
     *
     * @param input      classData object
     * @param annotation annotation to filter by
     * @return filtered list
     * @since 0.0.1
     */
    public static List<Class<?>> filterByAnnotation(final LoadedClasses input, Class<? extends Annotation> annotation) {
        return input.getClassMap().values().stream()
                .filter(c -> c.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    /**
     * Enumerates a directory for all class and jar files and loads appropriately
     * When loading .class files, this directory is expected to be top level directory for package
     *
     * @param directory directory to search
     * @param jarOnly   load only Jar Files
     * @return classData object
     */
    public static Map<String, LoadedClasses> getClassesFromDirectory(final File directory, final boolean jarOnly) {
        Map<String, LoadedClasses> dataMap = Collections.emptyMap();
        final ClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]{directory.toURI().toURL()},
                    ClassEnumerator.class.getClassLoader());
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create ClassLoader", e);
            return null;
        }
        dataMap.putAll(processFileTree(directory, classLoader, "", jarOnly));
        return dataMap;
    }

    /**
     * Retrieves all classes from a specified package
     * <p>
     * NOTE: Internal usage only, ClassEnumerator must exist in
     * the same {@link java.security.ProtectionDomain#getCodeSource},
     * else this will fail and return a classData object that is empty
     * <p>
     * Calls {@link io.not2excel.util.ClassEnumerator#getClassesFromPackage(java.lang.String, java.lang.String)}
     *
     * @param packageName internal package name
     * @return classData object
     * @since 0.0.1
     */
    public static Set<LoadedClasses> getClassesFromPackage(final String packageName) {
        String codeSource = ClassEnumerator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return getClassesFromPackage(packageName, codeSource);
    }

    /**
     * Retrieves all classes from a specified package that the given class resides in
     * Calls {@link io.not2excel.util.ClassEnumerator#getClassesFromPackage(java.lang.String, java.lang.String)}
     *
     * @param clazz class to pull code-source and package from
     * @return classData object
     * @since 0.0.1
     */
    public static Set<LoadedClasses> getClassesFromPackage(final Class<?> clazz) {
        String packageName = clazz.getPackage().getName().replace(".", "/");
        String codeSource = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        return getClassesFromPackage(packageName, codeSource);
    }

    /**
     * Internal method to retrieve classes from a specified package and code-source
     * Filtration in the case that the codeSource links to a .jar, will only return classes with passed packageName
     *
     * @param packageName package name, can be with either separator "." or "/"
     * @param codeSource  path of class origination
     * @return classData object
     * @since 0.0.1
     */
    private static Set<LoadedClasses> getClassesFromPackage(final String packageName, String codeSource) {
        boolean isJar = codeSource.endsWith(".jar");
        codeSource += isJar ? "" : packageName;
        codeSource = codeSource.replace(".", "/");
        Set<LoadedClasses> classDataSet = Collections.synchronizedSet(new HashSet<>());
        File file;
        try {
            file = new File(URLDecoder.decode(codeSource, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Failed to decode package path", e);
            return null;
        }
        final ClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()},
                    ClassEnumerator.class.getClassLoader());
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create ClassLoader", e);
            return null;
        }
        if (isJar) {
            classDataSet.add(getClassDataFromJar(file));
        } else {
            classDataSet.addAll(processFileTree(file, classLoader, packageName, false).values());
        }
        return classDataSet;
    }

    /**
     * Returns the relative {@link io.not2excel.util.ClassEnumerator.LoadedClasses} object created from processing a jar
     * Calls {@link io.not2excel.util.ClassEnumerator#getClassDataFromJar(java.io.File, java.lang.ClassLoader)}
     *
     * @param file file passed that *should* be a .jar file
     * @return classData object
     * @since 0.0.1
     */
    public static LoadedClasses getClassDataFromJar(final File file) {
        final ClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()},
                    ClassEnumerator.class.getClassLoader());
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create ClassLoader", e);
            return null;
        }
        return getClassDataFromJar(file, classLoader);
    }

    /**
     * Returns the relative {@link io.not2excel.util.ClassEnumerator.LoadedClasses} object created from processing a jar
     *
     * @param file file passed that *should* be a .jar file
     * @return classData object
     * @since 0.0.1
     */
    public static LoadedClasses getClassDataFromJar(final File file, final ClassLoader classLoader) {
        final LoadedClasses classData = new LoadedClasses(classLoader);
        try {
            final JarFile jarFile = new JarFile(file);
            jarFile.stream().parallel().forEach(entry -> {
                if (entry.isDirectory() || !entry.getName().toLowerCase().trim().endsWith(".class")) {
                    return;
                }
                Optional<Class<?>> clazz = Optional.ofNullable(loadClass(entry.getName(), classLoader));
                if (clazz.isPresent()) {
                    classData.addClass(clazz.get());
                }
            });
            jarFile.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create JarFile", e);
        }
        return classData;
    }

    /**
     * Internally processes a directory to retrieve the set of classes
     * to later add to a {@link io.not2excel.util.ClassEnumerator.LoadedClasses} object
     *
     * @param classLoader relative classLoader
     * @param directory   start directory of file tree
     * @param prepend     prepended string to create fully qualified name
     * @param jarOnly     load only Jar Files
     * @return set of classes
     * @since 0.0.1
     */
    private static Map<String, LoadedClasses> processFileTree(final File directory, final ClassLoader classLoader,
                                                              final String prepend, final boolean jarOnly) {
        final Map<String, LoadedClasses> classMap = Collections.emptyMap();
        final Optional<String[]> files = Optional.ofNullable(directory.list());
        if (!files.isPresent()) {
            return classMap;
        }
        Arrays.stream(files.get()).parallel().forEach(fileName -> {
            Optional<String> className = Optional.empty();
            if (!jarOnly) {
                if (fileName.endsWith(".class")) {
                    className = Optional.of(String.format("%s.%s", prepend, fileName));
                }
                if (className.isPresent()) {
                    Optional<Class<?>> clazz = Optional.ofNullable(loadClass(className.get(), classLoader));
                    if (clazz.isPresent()) {
                        LoadedClasses data = new LoadedClasses(classLoader);
                        data.addClass(clazz.get());
                        classMap.put(fileName, data);
                    }
                    return;
                }
            }
            final File subDir = new File(directory, fileName);
            if (subDir.isDirectory()) {
                classMap.putAll(processFileTree(subDir, classLoader,
                        String.format("%s.%s", prepend, fileName), jarOnly));
            } else if (subDir.getName().toLowerCase().trim().endsWith(".jar")) {
                classMap.put(subDir.getName().replace(".jar", ""), getClassDataFromJar(subDir));
            }
        });
        return classMap;
    }

    /**
     * Loads a class via {@see Class#forName(String, boolean, ClassLoader) Class#forName}
     * If the passed className contains {@link java.io.File#separator} with "."
     * If the passed className ends with ".class", it'll be removed
     * If the passed className starts with ".", it'll be sub-stringed out
     * Wraps the exception within this method
     *
     * @param name        fully qualified name
     * @param classLoader relative classLoader
     * @return class if loaded else null
     * @since 0.0.1
     */
    public static Class<?> loadClass(String name, final ClassLoader classLoader) {
        Class<?> retVal = null;
        if (name.endsWith(".class")) {
            name = name.replace(".class", "");
        }
        if (name.contains("/")) {
            name = name.replace("/", ".");
        }
        if (name.startsWith(".")) {
            name = name.substring(1);
        }
        try {
            retVal = classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Failed to load class " + name, e);
        }
        return retVal;
    }

    /**
     * Contains the set of loaded classes and the relative classLoader
     *
     * @author not2excel
     * @version 0.0.1
     * @since 0.0.1
     */
    public static final class LoadedClasses implements Iterable<Entry<String, Class<?>>> {

        @Getter
        private final ClassLoader classLoader;
        @Getter
        private final Map<String, Class<?>> classMap = new HashMap<>();

        /**
         * Initializes with a specific ClassLoader
         *
         * @param classLoader ClassLoader instance
         * @since 0.0.1
         */
        public LoadedClasses(final ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        /**
         * Retrieves the value found by the give key className
         *
         * @param className key for classMap
         * @return class if found, else null
         * @since 0.0.1
         */
        public synchronized Class<?> get(String className) {
            return this.classMap.get(className);
        }

        /**
         * Adds a class to the classMap
         * class is loaded via relative classLoader
         *
         * @param clazz Class to add to classMap
         * @since 0.0.1
         */
        public synchronized void addClass(final Class<?> clazz) {
            this.classMap.put(clazz.getName(), clazz);
        }

        /**
         * Adds a varargs of class to the classMap via {@link #addClass(Class) addClass}
         *
         * @param classes classes to add to classMap
         * @since 0.0.1
         */
        public void addClasses(final Class<?>... classes) {
            for (Class<?> clazz : classes) {
                this.addClass(clazz);
            }
        }

        /**
         * Adds a set of class to the classMap via {@link #addClass(Class) addClass}
         *
         * @param classes classes to add to classMap
         * @since 0.0.1
         */
        public void addClasses(final Set<Class<?>> classes) {
            for (Class<?> clazz : classes) {
                this.addClass(clazz);
            }
        }

        @Override
        public Iterator<Entry<String, Class<?>>> iterator() {
            return this.classMap.entrySet().iterator();
        }
    }
}
