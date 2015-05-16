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
import io.not2excel.module.context.Module;
import io.not2excel.util.ClassEnumerator;
import io.not2excel.util.ClassEnumerator.LoadedClasses;

import java.util.Map;

public interface ModuleLoader<M extends Module> {

    default Map<String, LoadedClasses> getInternalModules() {
        return ClassEnumerator.loadClassesFromJar();
    }

    ModuleCoordinator<M> getRelativeCoordinator();
}
