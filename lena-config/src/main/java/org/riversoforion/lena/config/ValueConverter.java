/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

public interface ValueConverter {

    boolean toBoolean(String value);

    short toShort(String value);

    int toInt(String value);

    long toLong(String value);
}
