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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleModuleCoordinator<M extends Module> implements ModuleCoordinator<M> {

    private Class<M> baseModuleClass;
    private Map<String, M> moduleMap;

    public SimpleModuleCoordinator(Class<M> baseModuleClass) {
        moduleMap = new HashMap<>();
        this.baseModuleClass = baseModuleClass;
    }

    @Override
    public M instantiate(Class<M> moduleClass) {
        try {
            return moduleClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void load(M module) throws ModuleLoadException {
        if (!this.hasModule(module.getClass())) {
            ModuleInfo info = this.getModuleInfo(module);
            this.moduleMap.put(info.id(), module);
            module.onLoad();
        }
        else {
            throw new ModuleLoadException("Module " + module.getClass().getSimpleName() + " already loaded.");
        }
    }

    @Override
    public void load(Class<M> moduleClass) {
        M module;
        try {
            module = instantiate(moduleClass);
            this.load(module);
        } catch (ModuleLoadException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(List<Class<M>> moduleClassList) {
        moduleClassList.forEach(this::load);
    }

    @Override
    public void unload(M module) throws ModuleUnLoadException {
        if (this.hasModule(module.getClass())) {
            ModuleInfo info = this.getModuleInfo(module);
            this.unload(info.id());
        }
        else {
            throw new ModuleUnLoadException("Module " + module.getClass().getSimpleName() + " not loaded.");
        }
    }

    @Override
    public void unload(Class<? extends Module> moduleClass) {
        M module = this.getModule(moduleClass);
        if (module != null) {
            try {
                this.unload(module);
            } catch (ModuleUnLoadException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unload(List<Class<? extends Module>> moduleClassList) {
        moduleClassList.forEach(this::unload);
    }

    @Override
    public void unload(String id) throws ModuleUnLoadException {
        if(this.moduleMap.containsKey(id)) {
            M module = this.moduleMap.get(id);
            module.onUnload();
            this.moduleMap.remove(id);
        }
        else {
            throw new ModuleUnLoadException("Module " + id + " not loaded.");
        }
    }

    @Override
    public void enable(String id) throws ModuleEnableException {
        if(this.moduleMap.containsKey(id)) {
            M module = this.moduleMap.get(id);
            module.onEnable();
        } else {
            throw new ModuleEnableException("Module " + id + " not loaded.");
        }
    }

    @Override
    public void enable(String... idList) {
        for(String id : idList) {
            try {
                this.enable(id);
            } catch (ModuleEnableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void enable(M module) throws ModuleEnableException {
        if(this.hasModule(module.getClass())) {
            module.onEnable();
        } else {
            throw new ModuleEnableException("Module " + module.getClass().getSimpleName() + " not loaded.");
        }
    }

    @Override
    public void enable(List<M> moduleList) {
        moduleList.forEach((module) -> {
            try {
                this.enable(module);
            } catch (ModuleEnableException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void disable(String id) throws ModuleDisableException {
        if(this.moduleMap.containsKey(id)) {
            M module = this.moduleMap.get(id);
            module.onDisable();
        } else {
            throw new ModuleDisableException("Module " + id+ " not loaded.");
        }
    }

    @Override
    public void disable(String... idList) {
        for(String id : idList) {
            try {
                this.disable(id);
            } catch (ModuleDisableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disable(M module) throws ModuleDisableException {
        if(this.hasModule(module.getClass())) {
            module.onEnable();
        } else {
            throw new ModuleDisableException("Module " + module.getClass().getSimpleName() + " not loaded.");
        }
    }

    @Override
    public void disable(List<M> moduleList) {
        moduleList.forEach((module) -> {
            try {
                this.disable(module);
            } catch (ModuleDisableException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public M getModule(String id) {
        return this.moduleMap.get(id);
    }

    @Override
    public M getModule(Class<? extends Module> moduleClass) {
        M module = null;
        for (M m : this.moduleMap.values()) {
            if (m.getClass().equals(moduleClass)) {
                module = m;
                break;
            }
        }
        return module;
    }


    @Override
    public boolean hasModule(String id) {
        return this.moduleMap.containsKey(id);
    }

    @Override
    public boolean hasModule(Class<? extends Module> moduleClass) {
        boolean hasModule = false;
        for (M m : this.moduleMap.values()) {
            if (m.getClass().equals(moduleClass)) {
                hasModule = true;
                break;
            }
        }
        return hasModule;
    }

    @Override
    public Map<String, M> getModuleList() {
        return this.moduleMap;
    }

    @Override
    public ModuleInfo getModuleInfo(M module) {
        return module.getClass().getAnnotation(ModuleInfo.class);
    }

    @Override
    public ModuleInfo getModuleInfo(Class<? extends Module> moduleClass) {
        return moduleClass.getAnnotation(ModuleInfo.class);
    }

    @Override
    public Class<M> getBaseModuleClass() {
        return this.baseModuleClass;
    }
}
