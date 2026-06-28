/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/**
 * Default [ValueConverter] implementation.
 *
 * Correctness notes (intentional behavior, verified from original Java implementation):
 * - `null` input to numeric converters returns `0` (not an exception). Invalid non-null
 *   strings throw [NumberFormatException].
 * - Boolean: `true/yes/y/on/1` (case-insensitive) → `true`; anything else → `false`.
 *   There is no explicit "false" list — this is intentional: unknown values default to off.
 */
public open class DefaultValueConverter : ValueConverter {

    private val trueValues = setOf("true", "yes", "y", "on", "1")

    override fun toBoolean(value: String?): Boolean =
        value?.lowercase() in trueValues

    override fun toShort(value: String?): Short =
        value?.toShort() ?: 0

    override fun toInt(value: String?): Int =
        value?.toInt() ?: 0

    override fun toLong(value: String?): Long =
        value?.toLong() ?: 0L

    override fun toFloat(value: String?): Float =
        value?.toFloat() ?: 0.0f

    override fun toDouble(value: String?): Double =
        value?.toDouble() ?: 0.0
}
