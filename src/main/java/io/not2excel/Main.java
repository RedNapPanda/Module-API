/*
 * Copyright (C) 2011-Current Richmond Steele (Not2EXceL) (nasm) <not2excel@gmail.com>
 * 
 * This file is part of module-api.
 * 
 * module-api can not be copied and/or distributed without the express
 * permission of the aforementioned owner.
 */
package io.not2excel;

import io.not2excel.module.loader.ModuleLoader;

public class Main {

    public static void main(String[] main) {
        ModuleLoader<?> loader = () -> null;
        loader.getInternalModules().forEach((s, l) -> {
            System.out.println("<====================>");
            System.out.println(s);
            System.out.println("----------------------");
            System.out.println(l.toString());
            System.out.println("<====================>");
        });
    }
}
