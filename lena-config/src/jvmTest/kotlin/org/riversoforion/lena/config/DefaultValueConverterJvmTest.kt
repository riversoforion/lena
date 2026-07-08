/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * On the JVM, [Short.parseShort] throws a [NumberFormatException] whose message echoes the
 * offending input (e.g. `For input string: "-32769"`). Kotlin/Native's message format differs,
 * so this assertion lives here rather than in the cross-platform `DefaultValueConverterTest`.
 */
class DefaultValueConverterJvmTest {

    private val converter = DefaultValueConverter()

    @Test
    fun toShort_Invalid_MessageContainsInput() {
        for (input in listOf("-32769", "32768", "", "other string")) {
            val exception = assertFailsWith<IllegalArgumentException>("input='$input'") { converter.toShort(input) }
            assertTrue(exception.message.orEmpty().contains(input), "message should contain '$input'")
        }
    }
}
