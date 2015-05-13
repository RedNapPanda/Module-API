/*
 * Copyright (C) 2011-Current Richmond Steele (Not2EXceL) (nasm) <not2excel@gmail.com>
 * 
 * This file is part of module-api.
 * 
 * module-api can not be copied and/or distributed without the express
 * permission of the aforementioned owner.
 */
package io.not2excel.module.exception;

public class ModuleDisableException extends Exception {

    public ModuleDisableException(String msg) {
        super("Module " + msg + " not loaded.");
    }
}
