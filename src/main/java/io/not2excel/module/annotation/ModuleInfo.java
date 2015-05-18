/*
 * Copyright (C) 2011-Current Richmond Steele (Not2EXceL) (nasm) <not2excel@gmail.com>
 * 
 * This file is part of module-api.
 * 
 * module-api can not be copied and/or distributed without the express
 * permission of the aforementioned owner.
 */
package io.not2excel.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {

    String id(); //id's are non case sensitive
    String name();
    String description() default "";
    String[] loadAfter() default {}; //equivalent of hard dependencies, circular dependencies will cause issues
}
