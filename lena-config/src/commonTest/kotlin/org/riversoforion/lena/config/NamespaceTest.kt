/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NamespaceTest {

    @Test
    fun root() {
        assertEquals("/", Namespace.root().name())
    }

    @Test
    fun of() {
        val cases = listOf(
            listOf("first") to "/first",
            listOf("first", "second") to "/first/second",
            listOf("number-1", "number-2", "number-3") to "/number-1/number-2/number-3",
            listOf("two-parts", "three-name-parts") to "/two-parts/three-name-parts",
            listOf("good.segment") to "/good-segment",
            listOf("valid_name") to "/valid-name",
            listOf("UppercaseAllowed") to "/uppercaseallowed",
            listOf("good-segment", "also/good/segment") to "/good-segment/also/good/segment",
            listOf("Valid-Segment", "good-segment") to "/valid-segment/good-segment",
            // Accented characters are deliberately excluded here: NFD decomposition (JVM) vs.
            // best-effort stripping (native) produce different results. See platform-specific
            // tests in jvmTest/nativeTest for that behavior.
            listOf("spaces are allowed", "and numb3rs", "and \$pec!al c#aracter\$") to
                "/spaces-are-allowed/and-numb3rs/and-pec-al-c-aracter-",
            emptyList<String>() to "/",
        )
        for ((segments, expected) in cases) {
            assertEquals(expected, Namespace.of(*segments.toTypedArray()).name(), "segments=$segments")
        }
    }

    @Test
    fun parse() {
        val cases = listOf(
            "/" to "/",
            "/first" to "/first",
            "/first/second" to "/first/second",
            "/number-1/number-2/number-3" to "/number-1/number-2/number-3",
            "      " to "/",
            "///" to "/",
            "first" to "/first",
            "first/second" to "/first/second",
            "/good-segment/ValiD-SegmenT" to "/good-segment/valid-segment",
            "/this_is_fine/this-is-fine" to "/this-is-fine/this-is-fine",
            "/not.wrong" to "/not-wrong",
            "/spaces and/special characters/a!!@w3d" to "/spaces-and/special-characters/a-w3d",
            "/allow//empty-segments" to "/allow/empty-segments",
            "/ whitespace /\taround segments/is-trimmed " to "/whitespace/around-segments/is-trimmed",
            "///first//second/" to "/first/second",
        )
        for ((input, expected) in cases) {
            assertEquals(expected, Namespace.parse(input).name(), "input='$input'")
        }
    }

    @Test
    fun child_WithValidName() {
        val parent = Namespace.of("parent")
        val cases = listOf(
            "sub-1" to ("/parent/sub-1" to 2),
            "sub-1/sub-2/sub-3" to ("/parent/sub-1/sub-2/sub-3" to 4),
            "/absolute/child" to ("/absolute/child" to 2),
            "valid name" to ("/parent/valid-name" to 2),
            "also__valid" to ("/parent/also-valid" to 2),
        )
        for ((input, expected) in cases) {
            val (expectedName, expectedSegments) = expected
            val child = parent.child(input)
            assertEquals(expectedName, child.name(), "input='$input'")
            assertEquals(expectedSegments, child.segments().size, "input='$input'")
        }
    }

    @Test
    fun child_WithInvalidName() {
        val parent = Namespace.of("parent")
        for (input in listOf("", "    ")) {
            assertFailsWith<IllegalArgumentException>("input='$input'") { parent.child(input) }
        }
    }

    @Test
    fun resolveProperty() {
        val namespace = Namespace.of("parent", "child")
        val propName = namespace.resolveProperty(Name.of("prop-name", "yet.anotherProp"))
        assertEquals(listOf("parent", "child", "prop-name", "yet.anotherProp"), propName)
    }

    @Test
    fun comparable() {
        val namespace = Namespace.of("parent", "child")
        val same = Namespace.of("parent", "child")
        val less = Namespace.of("parent", "a")
        val more = Namespace.of("parent", "d")

        assertEquals(0, namespace.compareTo(same))
        assertEquals(true, namespace.compareTo(less) > 0)
        assertEquals(true, namespace.compareTo(more) < 0)
    }
}
