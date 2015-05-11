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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleModuleCoordinator<M extends Module> implements ModuleCoordinator<M> {

    private Class<M> moduleClass;
    private Map<String, M> moduleMap;

    public SimpleModuleCoordinator(Class<M> moduleClass) {
        moduleMap = new HashMap<>();
        this.moduleClass = moduleClass;
    }

    @Override
    public void load(M module) {

    }

    @Override
    public void load(Class<M> moduleClass) {

    }

    @Override
    public void load(List<Class<M>> moduleClassList) {

    }

    @Override
    public void unload(M module) {

    }

    @Override
    public void unload(Class<M> moduleClass) {

    }

    @Override
    public void unload(List<Class<M>> moduleClassList) {

    }

    @Override
    public void unload(String id) {

    }

    @Override
    public void enable(String id) {

    }

    @Override
    public void enable(String... idList) {

    }

    @Override
    public void enable(M module) {

    }

    @Override
    public void enable(List<M> moduleList) {

    }

    @Override
    public void disable(String id) {

    }

    @Override
    public void disable(String... idList) {

    }

    @Override
    public void disable(M module) {

    }

    @Override
    public void disable(List<M> moduleList) {

    }

    @Override
    public M getModule(String id) {
        return null;
    }

    @Override
    public M getModule(Class<M> module) {
        return null;
    }

    @Override
    public Map<String, M> getModuleList() {
        return this.moduleMap;
    }

    @Override
    public Class<M> getModuleClass() {
        return this.moduleClass;
    }
}
