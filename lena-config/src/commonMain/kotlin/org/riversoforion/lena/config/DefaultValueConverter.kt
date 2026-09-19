/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Default [ValueConverter] implementation.
 *
 * Correctness notes (intentional behavior, verified from original Java implementation):
 * - `null` input to numeric converters returns `0` (not an exception). Invalid non-null
 *   strings throw [NumberFormatException].
 * - Boolean: `true/yes/y/on/1` (case-insensitive) → `true`; anything else → `false`.
 *   There is no explicit "false" list — this is intentional: unknown values default to off.
 */
@Suppress("TooManyFunctions")
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

    override fun <T : Any> toEnum(value: String?, klass: KClass<T>): T =
        if (value == null) throw IllegalArgumentException("null enum value")
        else platformEnumLookup(value, klass)

    public fun toDuration(value: String?): Duration =
        if (value == null) throw IllegalArgumentException("null duration")
        else parseDuration(value)

    public fun toIntList(value: String?): List<Int> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(",").map { it.trim().toInt() }

    public fun toStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(",").map { it.trim() }

    public fun toStringSet(value: String?): Set<String> =
        toStringList(value).toSet()

    public fun toStringMap(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        return value.split(",").associate { pair ->
            val parts = pair.split("=", limit = 2)
            require(parts.size == 2) { "Invalid map format: '$pair'. Expected 'key=value'" }
            parts[0].trim() to parts[1].trim()
        }
    }

    override fun <T : Any> convert(value: String?, converter: (String) -> T): T =
        if (value == null) throw IllegalArgumentException("null value")
        else converter(value)

    private fun parseDuration(value: String): Duration {
        val trimmed = value.trim()
        // Try ISO-8601 first
        val isoParseResult = runCatching { Duration.parse(trimmed) }
        return if (isoParseResult.isSuccess) {
            isoParseResult.getOrThrow()
        } else {
            // Try shorthand: "5s", "2m", "1h", etc.
            parseShorthandDuration(trimmed)
        }
    }

    private fun parseShorthandDuration(value: String): Duration {
        val regex = Regex("""^(\d+(?:\.\d+)?)\s*([a-zA-Z]+)$""")
        val match = regex.matchEntire(value) ?: throw IllegalArgumentException(
            "Invalid duration format: '$value'. Expected ISO-8601 or shorthand (e.g., '5s', '2m', '1h')"
        )
        val amount = match.groupValues[1].toDouble()
        val unit = when (match.groupValues[2].lowercase()) {
            "ns", "nano" -> DurationUnit.NANOSECONDS
            "us", "micro" -> DurationUnit.MICROSECONDS
            "ms", "milli" -> DurationUnit.MILLISECONDS
            "s", "sec", "second" -> DurationUnit.SECONDS
            "m", "min", "minute" -> DurationUnit.MINUTES
            "h", "hour" -> DurationUnit.HOURS
            "d", "day" -> DurationUnit.DAYS
            else -> throw IllegalArgumentException("Unknown duration unit: '${match.groupValues[2]}'")
        }
        return amount.toDuration(unit)
    }
}

internal expect fun <T : Any> platformEnumLookup(value: String, klass: KClass<T>): T
