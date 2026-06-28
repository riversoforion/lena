/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/** Converts raw string values to typed primitives. */
public interface ValueConverter {
    public fun toBoolean(value: String?): Boolean
    public fun toShort(value: String?): Short
    public fun toInt(value: String?): Int
    public fun toLong(value: String?): Long
    public fun toFloat(value: String?): Float
    public fun toDouble(value: String?): Double
}
