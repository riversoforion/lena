/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ComplexTypeConversionTest {

    private val converter = DefaultValueConverter()

    @Test
    fun toDuration_ParsesISO8601() {
        val result = converter.toDuration("PT5S")
        assertEquals(5.toDuration(DurationUnit.SECONDS), result)
    }

    @Test
    fun toDuration_ParsesShorthandSeconds() {
        val result = converter.toDuration("5s")
        assertEquals(5, result.inWholeSeconds)
    }

    @Test
    fun toDuration_ParsesShorthandMinutes() {
        val result = converter.toDuration("2m")
        assertEquals(120, result.inWholeSeconds)
    }

    @Test
    fun toDuration_ParsesShorthandHours() {
        val result = converter.toDuration("1h")
        assertEquals(3600, result.inWholeSeconds)
    }

    @Test
    fun toDuration_ParsesShorthandDays() {
        val result = converter.toDuration("1d")
        assertEquals(86400, result.inWholeSeconds)
    }

    @Test
    fun toDuration_ParsesWithWhitespace() {
        val result = converter.toDuration("  5 s  ")
        assertEquals(5, result.inWholeSeconds)
    }

    @Test
    fun toDuration_ThrowsOnNull() {
        assertFailsWith<IllegalArgumentException> {
            converter.toDuration(null)
        }
    }

    @Test
    fun toDuration_ThrowsOnInvalidFormat() {
        assertFailsWith<IllegalArgumentException> {
            converter.toDuration("not-a-duration")
        }
    }

    @Test
    fun toIntList_SplitsCommaValues() {
        val result = converter.toIntList("1,2,3")
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun toIntList_TrimsWhitespace() {
        val result = converter.toIntList(" 1 , 2 , 3 ")
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun toIntList_ReturnsEmptyOnNull() {
        val result = converter.toIntList(null)
        assertEquals(emptyList(), result)
    }

    @Test
    fun toIntList_ReturnsEmptyOnBlank() {
        val result = converter.toIntList("   ")
        assertEquals(emptyList(), result)
    }

    @Test
    fun toStringList_SplitsCommaValues() {
        val result = converter.toStringList("a,b,c")
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun toStringList_TrimsWhitespace() {
        val result = converter.toStringList(" a , b , c ")
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun toStringSet_DeduplicatesValues() {
        val result = converter.toStringSet("a,b,a,c")
        assertEquals(setOf("a", "b", "c"), result)
    }

    @Test
    fun toStringMap_ParsesKeyValuePairs() {
        val result = converter.toStringMap("key1=value1,key2=value2")
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), result)
    }

    @Test
    fun toStringMap_TrimsWhitespace() {
        val result = converter.toStringMap(" key1 = value1 , key2 = value2 ")
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), result)
    }

    @Test
    fun toStringMap_ReturnsEmptyOnNull() {
        val result = converter.toStringMap(null)
        assertEquals(emptyMap(), result)
    }

    @Test
    fun convert_AppliesConversionFunction() {
        val result = converter.convert("42") { it.toInt() * 2 }
        assertEquals(84, result)
    }

    @Test
    fun convert_ThrowsOnNull() {
        assertFailsWith<IllegalArgumentException> {
            converter.convert<Int>(null) { it.toInt() }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun toUuid_ParsesValidUuid() {
        val result = converter.toUuid("550e8400-e29b-41d4-a716-446655440000")
        assertEquals(Uuid.parse("550e8400-e29b-41d4-a716-446655440000"), result)
    }

    @Test
    fun toUuid_ThrowsOnNull() {
        assertFailsWith<IllegalArgumentException> {
            converter.toUuid(null)
        }
    }

    @Test
    fun toUuid_ThrowsOnInvalidFormat() {
        assertFailsWith<IllegalArgumentException> {
            converter.toUuid("not-a-uuid")
        }
    }

    @Test
    fun register_AndToRegistered_RoundTrips() {
        data class Point(val x: Int, val y: Int)
        converter.register(Point::class) { s ->
            val (x, y) = s.split(":").map { it.toInt() }
            Point(x, y)
        }
        val result = converter.toRegistered("3:4", Point::class)
        assertEquals(Point(3, 4), result)
    }

    @Test
    fun toRegistered_ThrowsWhenNothingRegistered() {
        assertFailsWith<IllegalStateException> {
            converter.toRegistered("anything", String::class)
        }
    }
}
