/*
 * Copyright (C) 2011-Current Richmond Steele (Not2EXceL) (nasm) <not2excel@gmail.com>
 * 
 * This file is part of module-api.
 * 
 * module-api can not be copied and/or distributed without the express
 * permission of the aforementioned owner.
 */
package io.not2excel.module;

import io.not2excel.module.context.Module;

import java.util.List;
import java.util.Map;

public interface ModuleCoordinator<M extends Module> {

    void load(M module);

    void load(Class<M> moduleClass);

    void load(List<Class<M>> moduleClassList);

    void unload(M module);

    void unload(Class<M> moduleClass);

    void unload(List<Class<M>> moduleClassList);

    void unload(String id);

    void enable(String id);

    void enable(String... idList);

    void enable(M module);

    void enable(List<M> moduleList);

    void disable(String id);

    void disable(String... idList);

    void disable(M module);

    void disable(List<M> moduleList);

    M getModule(String id);

    M getModule(Class<M> module);

    Map<String, M> getModuleList();

    Class<M> getModuleClass();
}
//http://steamcommunity.com/profiles/76561198227314644/
