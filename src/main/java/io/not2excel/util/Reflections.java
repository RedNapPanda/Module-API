/*
 * Copyright (C) 2011-Current Richmond Steele (Not2EXceL) (nasm) <not2excel@gmail.com>
 * 
 * This file is part of module-api.
 * 
 * module-api can not be copied and/or distributed without the express
 * permission of the aforementioned owner.
 */
package io.not2excel.util;

import java.lang.annotation.Annotation;

public final class Reflections {

    /**
     * Retrieves the annotation object on a class
     *
     * @param clazz      target
     * @param annotation annotation class
     * @param <T>        annotation type
     * @return annotation object
     * @throws RuntimeException if annotation is not present
     */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotation) {
        if (!clazz.isAnnotationPresent(annotation)) {
            throw new RuntimeException(annotation.getSimpleName() + " is not present on target " + clazz.getSimpleName());
        }
        return clazz.getAnnotation(annotation);
    }
}
