/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NameTest {

    @Test
    fun of() {
        val cases = listOf(
            listOf("first") to "first",
            listOf("first", "second") to "first/second",
            listOf("number-1", "number-2", "number-3") to "number-1/number-2/number-3",
            listOf("two-parts", "three-name-parts") to "two-parts/three-name-parts",
            listOf("good.segment") to "good-segment",
            listOf("valid_name") to "valid-name",
            listOf("UppercaseAllowed") to "uppercaseallowed",
            listOf("Valid-Segment", "good-segment") to "valid-segment/good-segment",
            // Accented characters are deliberately excluded here: NFD decomposition (JVM) vs.
            // best-effort stripping (native) produce different results. See platform-specific
            // tests in jvmTest/nativeTest for that behavior.
            listOf("spaces are allowed", "and numb3rs", "and \$pec!al c#aracter\$") to
                "spaces-are-allowed/and-numb3rs/and-pec-al-c-aracter-",
        )
        for ((segments, expected) in cases) {
            val name = Name.of(segments)
            assertEquals(expected, name.toString(), "segments=$segments")
            assertEquals(segments, name.segments(), "segments=$segments")
        }
    }

    @Test
    fun of_WithEmbeddedSegments() {
        var name = Name.of("good-segment", "also/good/segment")
        assertEquals("good-segment/also/good/segment", name.toString())
        assertEquals(listOf("good-segment", "also", "good", "segment"), name.segments())

        name = Name.of("*./()")
        assertEquals("-/-", name.toString())
        assertEquals(listOf("*.", "()"), name.segments())
    }

    @Test
    fun of_WithInvalidSegments() {
        for (segment in listOf("", "   ", "\t  \r\n")) {
            assertFailsWith<IllegalArgumentException>("segment='$segment'") { Name.of(segment) }
        }
    }

    @Test
    fun of_WithEmptySegments() {
        assertFailsWith<IllegalArgumentException> { Name.of(emptyList()) }
    }

    @Test
    fun of_WithMultipleEmptySegments() {
        assertFailsWith<IllegalArgumentException> { Name.of("", "  ", "") }
    }

    @Test
    fun normalizeSegment() {
        val cases = listOf(
            "segment" to "segment",
            "segment-name" to "segment-name",
            "Segment-NAME" to "segment-name",
            "s3gment name" to "s3gment-name",
            "\$3gment_nam*" to "-3gment-nam-",
            " segment name " to "segment-name",
            "" to "",
            "    " to "",
        )
        for ((input, expected) in cases) {
            assertEquals(expected, Name.normalizeSegment(input), "input='$input'")
        }
    }

    @Test
    fun objectContract() {
        val name = Name.of("parent", "child", "grandchild")
        val same = Name.of("parent", "child", "grandchild")
        val different = Name.of("parent", "child", "other")

        assertEquals(name, same)
        assertEquals(name.hashCode(), same.hashCode())
        assertEquals(name.toString(), same.toString())
        assertEquals(false, name == different)
    }

    @Test
    fun comparable() {
        val name = Name.of("parent", "child")
        val same = Name.of("parent", "child")
        val less = Name.of("parent", "a")
        val more = Name.of("parent", "d")

        assertEquals(0, name.compareTo(same))
        assertEquals(true, name.compareTo(less) > 0)
        assertEquals(true, name.compareTo(more) < 0)
    }
}
