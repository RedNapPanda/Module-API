/*
 * Copyright (C) 2011-Current Richmond Steele (Not2EXceL) (nasm) <not2excel@gmail.com>
 * 
 * This file is part of module-api.
 * 
 * module-api can not be copied and/or distributed without the express
 * permission of the aforementioned owner.
 */
package io.not2excel.module;

import io.not2excel.module.annotation.ModuleInfo;
import io.not2excel.module.context.Module;
import io.not2excel.module.exception.ModuleDisableException;
import io.not2excel.module.exception.ModuleEnableException;
import io.not2excel.module.exception.ModuleLoadException;
import io.not2excel.module.exception.ModuleUnLoadException;

import java.util.List;
import java.util.Map;

public interface ModuleCoordinator<M extends Module> {

    M instantiate(Class<M> moduleClass);

    void load(M module) throws ModuleLoadException;

    void load(Class<M> moduleClass) throws ModuleLoadException;

    void load(List<Class<M>> moduleClassList);

    void unload(M module) throws ModuleUnLoadException;

    void unload(Class<? extends Module> moduleClass);

    void unload(List<Class<? extends Module>> moduleClassList);

    void unload(String id) throws ModuleUnLoadException;

    void enable(String id) throws ModuleEnableException;

    void enable(String... idList);

    void enable(M module) throws ModuleEnableException;

    void enable(List<M> moduleList);

    void disable(String id) throws ModuleDisableException;

    void disable(String... idList);

    void disable(M module) throws ModuleDisableException;

    void disable(List<M> moduleList);

    M getModule(String id);

    M getModule(Class<? extends Module> moduleClass);

    boolean hasModule(String id);

    boolean hasModule(Class<? extends Module> moduleClass);

    Map<String, M> getModuleList();

    ModuleInfo getModuleInfo(M module);

    ModuleInfo getModuleInfo(Class<? extends Module> moduleClass);

    Class<M> getBaseModuleClass();
}
