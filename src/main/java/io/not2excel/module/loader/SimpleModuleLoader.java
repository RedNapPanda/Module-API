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

public class SimpleModuleLoader<M extends Module> implements ModuleLoader<M> {

    private final ModuleCoordinator<M> moduleCoordinator;

    public SimpleModuleLoader(ModuleCoordinator<M> moduleCoordinator) {
        this.moduleCoordinator = moduleCoordinator;
    }

    @Override
    public ModuleCoordinator<M> getRelativeCoordinator() {
        return moduleCoordinator;
    }
}
