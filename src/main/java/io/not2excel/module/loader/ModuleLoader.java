/*
 * Copyright (C) 2011-Current Richmond Steele (Not2EXceL) (nasm) <not2excel@gmail.com>
 * 
 * This file is part of module-api.
 * 
 * module-api can not be copied and/or distributed without the express
 * permission of the aforementioned owner.
 */
package io.not2excel.module.loader;

import io.not2excel.module.ModuleCoordinator;
import io.not2excel.module.annotation.ModuleInfo;
import io.not2excel.module.context.Module;
import io.not2excel.module.exception.ModuleLoadException;
import io.not2excel.util.ClassEnumerator;
import io.not2excel.util.ClassEnumerator.LoadedClasses;
import io.not2excel.util.Reflections;

import java.io.File;
import java.util.*;

public interface ModuleLoader<M extends Module> {
    
    default void loadModules(List<Class<M>> moduleList) {
        moduleList.forEach(c -> {
            ModuleCoordinator<M> moduleCoordinator = this.getRelativeCoordinator();
            try {
                moduleCoordinator.load(c);
            } catch (ModuleLoadException e) {
                e.printStackTrace();
            }
        });
    }

    default List<Class<M>> sortModulesByLoadOrder(List<Class<M>> moduleList) {
        List<Class<M>> sorted = new ArrayList<>();
        Map<Class<M>, String[]> mappedDependents = new HashMap<>();
        moduleList.parallelStream().forEach(c -> {
            ModuleInfo info = Reflections.getAnnotation(c, ModuleInfo.class);
            if(info.loadAfter().length == 0) {
                sorted.add(c);
            } else {
                mappedDependents.put(c, info.loadAfter());
            }
        });
        mappedDependents.forEach((c, s) -> {
            List<String> ids = Arrays.asList(s);
            int i = 0;
            int insert = i;
            for(Class<M> testClazz : sorted) {
                ModuleInfo info = Reflections.getAnnotation(testClazz, ModuleInfo.class);
                if(ids.contains(info.id())) {
                    ids.remove(info.id());
                    insert = i;
                }
                if(ids.isEmpty()) {
                    break;
                }
                i++;
            }
            sorted.add(insert, c);
        });
        return sorted;
    }

    default List<Class<?>> loadModulesFromDirectory(File directory) {
        Map<String, LoadedClasses> initialLoading = ClassEnumerator.loadClassesFromDirectory(directory, true);
        List<Class<?>> moduleClasses = ClassEnumerator.filterByAssignableFrom(initialLoading, Module.class);
        return ClassEnumerator.filterByAnnotation(moduleClasses, ModuleInfo.class);
    }

    default List<Class<?>> loadModulesFromDirectory(String relativePath) {
        File directory = new File(ClassEnumerator.getParentFolder(), relativePath);
        return this.loadModulesFromDirectory(directory);
    }

    default Map<String, LoadedClasses> getInternalModules() {
        return ClassEnumerator.isJar() ? ClassEnumerator.loadClassesFromJar() :
                ClassEnumerator.loadClassesFromPackage("");
    }

    ModuleCoordinator<M> getRelativeCoordinator();
}
