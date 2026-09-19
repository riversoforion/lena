/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.reflect.KClass

internal actual fun <T : Any> platformEnumLookup(value: String, klass: KClass<T>): T {
    val enumConstants = klass.java.enumConstants
        ?: throw IllegalArgumentException("Not an enum: $klass")
    @Suppress("UNCHECKED_CAST")
    val result = enumConstants.firstOrNull { (it as Enum<*>).name.equals(value, ignoreCase = true) }
        as T?
    return result ?: throw IllegalArgumentException("No enum constant '$value' in ${klass.simpleName}")
}
