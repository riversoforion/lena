/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Converts raw string values to typed primitives and complex types. */
@Suppress("TooManyFunctions")
public interface ValueConverter {
    public fun toBoolean(value: String?): Boolean
    public fun toShort(value: String?): Short
    public fun toInt(value: String?): Int
    public fun toLong(value: String?): Long
    public fun toFloat(value: String?): Float
    public fun toDouble(value: String?): Double

    // Complex types
    public fun <T : Any> toEnum(value: String?, klass: KClass<T>): T
    public fun toDuration(value: String?): Duration
    public fun toIntList(value: String?): List<Int>
    public fun toStringList(value: String?): List<String>
    public fun toStringSet(value: String?): Set<String>
    public fun toStringMap(value: String?): Map<String, String>

    @OptIn(ExperimentalUuidApi::class)
    public fun toUuid(value: String?): Uuid

    public fun <T : Any> convert(value: String?, converter: (String) -> T): T

    // Pluggable custom converters: `converter.register<CustomType> { CustomType.parse(it) }`
    public fun <T : Any> register(klass: KClass<T>, fn: (String) -> T)
    public fun <T : Any> toRegistered(value: String?, klass: KClass<T>): T
}

/** Reified convenience for [ValueConverter.register] — avoids passing `T::class` explicitly. */
public inline fun <reified T : Any> ValueConverter.register(noinline fn: (String) -> T) {
    register(T::class, fn)
}

/** Reified convenience for [ValueConverter.toRegistered] — avoids passing `T::class` explicitly. */
public inline fun <reified T : Any> ValueConverter.toRegistered(value: String?): T =
    toRegistered(value, T::class)

