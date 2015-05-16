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
     * Returns true if this class is run from a jar file
     *
     * @return is run from a jar file or not
     */
    public static boolean isJar() {
        return ClassEnumerator.class.getProtectionDomain().getCodeSource().getLocation().getFile().endsWith(".jar");
    }

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
     * Filters a LoadedClasses object for classes that have the passed annotation
     *
     * @param input      loadedClasses object
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
     * @return loadedClasses object
     */
    public static Map<String, LoadedClasses> loadClassesFromDirectory(final File directory, final boolean jarOnly) {
        Map<String, LoadedClasses> loadedMap = new HashMap<>();
        final ClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]{directory.toURI().toURL()},
                    ClassEnumerator.class.getClassLoader());
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create ClassLoader", e);
            return null;
        }
        loadedMap.putAll(processFileTree(directory, classLoader, "", jarOnly));
        return loadedMap;
    }

    /**
     * Retrieves all classes from a specific package, will only return the LoadedClasses object with the same key as the passed packageName
     * This ignores the rest of the loaded classes from recursive enumeration
     *
     * @param packageName package name to return same classes from
     * @return relative LoadedClasses object
     */
    public static LoadedClasses loadClassesFromSinglePackage(String packageName) {
        return loadClassesFromPackage(packageName).get(packageName);
    }

    /**
     * Retrieves all classes from a specific package, will only return the LoadedClasses object relative to the passed Class
     * This ignores the rest of the loaded classes from recursive enumeration
     *
     * @param clazz Class in Package to retrieve Classes from
     * @return relative LoadedClasses object
     */
    public static LoadedClasses loadClassesFromSinglePackage(Class<?> clazz) {
        return loadClassesFromPackage(clazz).get(clazz.getPackage().getName());
    }

    /**
     * Retrieves all classes from a specified package
     * <p/>
     * NOTE: Internal usage only, ClassEnumerator must exist in
     * the same {@link java.security.ProtectionDomain#getCodeSource},
     * else this will fail and return a loadedClasses object that is empty
     * <p/>
     * Calls {@link io.not2excel.util.ClassEnumerator#loadClassesFromPackage(java.lang.String, java.lang.String)}
     *
     * @param packageName internal package name
     * @return loadedClasses object
     * @since 0.0.1
     */
    public static Map<String, LoadedClasses> loadClassesFromPackage(String packageName) {
        if (packageName.contains(".")) {
            packageName = packageName.replace(".", "/");
        }
        String codeSource = ClassEnumerator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return loadClassesFromPackage(packageName, codeSource);
    }

    /**
     * Retrieves all classes from a specified package that the given class resides in
     * Calls {@link io.not2excel.util.ClassEnumerator#loadClassesFromPackage(java.lang.String, java.lang.String)}
     *
     * @param clazz class to pull code-source and package from
     * @return loadedClasses object
     * @since 0.0.1
     */
    public static Map<String, LoadedClasses> loadClassesFromPackage(final Class<?> clazz) {
        String packageName = clazz.getPackage().getName().replace(".", "/");
        String codeSource = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        return loadClassesFromPackage(packageName, codeSource);
    }

    /**
     * Internal method to retrieve classes from a specified package and code-source
     * Filtration in the case that the codeSource links to a .jar, will only return classes with passed packageName
     *
     * @param packageName package name, can be with either separator "." or "/"
     * @param codeSource  path of class origination
     * @return loadedClasses object
     * @since 0.0.1
     */
    private static Map<String, LoadedClasses> loadClassesFromPackage(final String packageName, String codeSource) {
        boolean isJar = codeSource.endsWith(".jar");
        codeSource += isJar ? "" : packageName;
        if (!isJar) {
            codeSource = codeSource.replace(".", "/");
        }
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
            LoadedClasses jarClasses = loadClassesFromJarUnformatted(file);
            return formatClasses(jarClasses);
        } else {
            return processFileTree(file, classLoader, packageName, false);
        }
    }

    /**
     * Loads the classes from the current Jar that ClassEnumerator resides in
     * Passes the loaded classes to {@link io.not2excel.util.ClassEnumerator#formatClasses(LoadedClasses)} to return
     * Returns the value from the resulting pass
     *
     * @return mapped classes
     * @since 0.0.1
     */
    public static Map<String, LoadedClasses> loadClassesFromJar() {
        File jarFile = new File(ClassEnumerator.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        if (!isJar()) {
            return Collections.emptyMap();
        }
        LoadedClasses jarClasses = loadClassesFromJarUnformatted(jarFile);
        return formatClasses(jarClasses);
    }

    /**
     * Passes the relative {@link io.not2excel.util.ClassEnumerator.LoadedClasses} object created from processing a jar to {@link io.not2excel.util.ClassEnumerator#formatClasses(LoadedClasses)}
     * Returns the value from the resulting pass
     *
     * @param file file passed that *should* be a .jar file
     * @return mapped classes
     * @since 0.0.1
     */
    public static Map<String, LoadedClasses> loadClassesFromJar(final File file) {
        LoadedClasses jarClasses = loadClassesFromJarUnformatted(file);
        return formatClasses(jarClasses);
    }

    /**
     * Passes the relative {@link io.not2excel.util.ClassEnumerator.LoadedClasses} object created from processing a jar to {@link io.not2excel.util.ClassEnumerator#formatClasses(LoadedClasses)}
     * Returns the value from the resulting pass
     *
     * @param file file passed that *should* be a .jar file
     * @return mapped classes
     * @since 0.0.1
     */
    public static Map<String, LoadedClasses> loadClassesFromJar(final File file, final ClassLoader classLoader) {
        LoadedClasses jarClasses = loadClassesFromJarUnformatted(file, classLoader);
        return formatClasses(jarClasses);
    }

    /**
     * Formats the LoadedClasses object into a pretty mapping
     * Mapping follows the same as processFileTree pkgName -> relative LoadedClasses object
     *
     * @param classes LoadedClasses object
     * @return mapped classes
     */
    private static Map<String, LoadedClasses> formatClasses(LoadedClasses classes) {
        return formatClasses(classes, null);
    }

    /**
     * Formats the LoadedClasses object into a pretty mapping
     * Mapping follows the same as processFileTree pkgName -> relative LoadedClasses object
     *
     * @param classes     LoadedClasses object
     * @param curClassMap current mapping to push the formatted object to, avoids recreation of LoadedClasses objects, and prevents overwriting existing objects
     * @return mapped classes
     */
    private static Map<String, LoadedClasses> formatClasses(LoadedClasses classes, Map<String, LoadedClasses> curClassMap) {
        Map<String, LoadedClasses> classMap;
        if (curClassMap == null) {
            classMap = new HashMap<>();
        } else {
            classMap = new HashMap<>(curClassMap);
        }
        if (classes != null) {
            classes.forEach(e -> {
                String pkgName = e.getValue().getPackage().getName();
                if (classMap.containsKey(pkgName)) {
                    LoadedClasses curLoaded = classMap.get(pkgName);
                    curLoaded.addClass(e.getValue());
                    classMap.put(pkgName, curLoaded);
                } else {
                    LoadedClasses newLoaded = new LoadedClasses(classes.classLoader);
                    newLoaded.addClass(e.getValue());
                    classMap.put(pkgName, newLoaded);
                }
            });
        }
        return classMap;
    }

    /**
     * Returns the relative {@link io.not2excel.util.ClassEnumerator.LoadedClasses} object created from processing a jar
     * Calls {@link io.not2excel.util.ClassEnumerator#loadClassesFromJarUnformatted(java.io.File, java.lang.ClassLoader)}
     * This returns an unformatted singular loadedClasses object containing all classes
     *
     * @param file file passed that *should* be a .jar file
     * @return loadedClasses object
     * @since 0.0.1
     */
    public static LoadedClasses loadClassesFromJarUnformatted(final File file) {
        final ClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()},
                    ClassEnumerator.class.getClassLoader());
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Failed to create ClassLoader", e);
            return null;
        }
        return loadClassesFromJarUnformatted(file, classLoader);
    }

    /**
     * Returns the relative {@link io.not2excel.util.ClassEnumerator.LoadedClasses} object created from processing a jar
     * This returns an unformatted singular loadedClasses object containing all classes
     *
     * @param file file passed that *should* be a .jar file
     * @return loadedClasses object
     * @since 0.0.1
     */
    public static LoadedClasses loadClassesFromJarUnformatted(final File file, final ClassLoader classLoader) {
        final LoadedClasses loadedClasses = new LoadedClasses(classLoader);
        try {
            final JarFile jarFile = new JarFile(file);
            jarFile.stream().parallel().forEach(entry -> {
                if (entry.isDirectory() || !entry.getName().toLowerCase().trim().endsWith(".class")) {
                    return;
                }
                Optional<Class<?>> clazz = Optional.ofNullable(loadClass(entry.getName(), classLoader));
                if (clazz.isPresent()) {
                    loadedClasses.addClass(clazz.get());
                }
            });
            jarFile.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create JarFile", e);
        }
        return loadedClasses;
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
        final Map<String, LoadedClasses> classMap = new HashMap<>();
        final Optional<String[]> files = Optional.ofNullable(directory.list());
        if (!files.isPresent()) {
            return classMap;
        }
        LoadedClasses loaded = new LoadedClasses(classLoader);
        Arrays.stream(files.get()).parallel().forEach(fileName -> {
            Optional<String> className = Optional.empty();
            if (!jarOnly) {
                if (fileName.endsWith(".class")) {
                    className = Optional.of(String.format("%s.%s", prepend, fileName));
                }
                if (className.isPresent()) {
                    Optional<Class<?>> clazz = Optional.ofNullable(loadClass(className.get(), classLoader));
                    if (clazz.isPresent()) {
                        loaded.addClass(clazz.get());
                    }
                    return;
                }
            }
            final File subDir = new File(directory, fileName);
            if (subDir.isDirectory()) {
                classMap.putAll(processFileTree(subDir, classLoader,
                        String.format("%s.%s", prepend, fileName), jarOnly));
            } else if (subDir.getName().toLowerCase().trim().endsWith(".jar")) {
                LoadedClasses jarClasses = loadClassesFromJarUnformatted(subDir);
                Map<String, LoadedClasses> formatted = formatClasses(jarClasses, classMap);
                classMap.clear();
                classMap.putAll(formatted);
            }
        });
        if (!loaded.isEmpty()) {
            classMap.put(prepend.replace("/", "."), loaded);
        }
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
            if (this.classMap.containsKey(clazz.getCanonicalName())) {
                logger.log(Level.WARNING, clazz.getName() + " => Already loaded. Skipping loading.");
                return;
            }
            this.classMap.put(clazz.getCanonicalName(), clazz);
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

        /**
         * Wrapper for checking if this object is empty
         *
         * @return HashMap::isEmpty
         */
        public boolean isEmpty() {
            return this.classMap.isEmpty();
        }

        /**
         * Wrapper for checking the size of this object
         *
         * @return HashMap::size
         */
        public int size() {
            return this.classMap.size();
        }

        @Override
        public Iterator<Entry<String, Class<?>>> iterator() {
            return this.classMap.entrySet().iterator();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            this.classMap.forEach((s, c) -> {
                builder.append(c.getSimpleName()).append(" => ").append(s);
                builder.append("\n");
            });
            return builder.toString();
        }
    }
}
