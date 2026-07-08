/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultValueConverterTest {

    private val converter = DefaultValueConverter()

    @Test
    fun toBoolean_Valid() {
        val cases = listOf(
            "TRUE" to true, "true" to true, "Yes" to true, "yes" to true, "y" to true, "Y" to true,
            "on" to true, "On" to true, "1" to true,
            "FALSE" to false, "No" to false, "no" to false, "off" to false, "OFF" to false,
            "" to false, null to false,
        )
        for ((input, expected) in cases) {
            assertEquals(expected, converter.toBoolean(input), "input='$input'")
        }
    }

    @Test
    fun toShort_Valid() {
        val cases = listOf(
            "-32768" to (-32768).toShort(),
            "-1" to (-1).toShort(),
            "0" to 0.toShort(),
            "1" to 1.toShort(),
            "32767" to 32767.toShort(),
            null to 0.toShort(),
        )
        for ((input, expected) in cases) {
            assertEquals(expected, converter.toShort(input), "input='$input'")
        }
    }

    @Test
    fun toShort_Invalid() {
        // Exception message format (e.g. whether it echoes the offending input) is a JVM-specific
        // NumberFormatException detail, not a cross-platform contract — see the jvmTest variant
        // of this test for that assertion.
        for (input in listOf("-32769", "32768", "", "other string")) {
            assertFailsWith<IllegalArgumentException>("input='$input'") { converter.toShort(input) }
        }
    }
}
