/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * JVM uses `java.text.Normalizer` (NFD) to decompose accented characters before stripping
 * non-ASCII bytes, so 'é' reduces to its base letter 'e' rather than being dropped. See the
 * native-target counterpart of this test for the (different, documented) behavior there.
 */
class NameNormalizationJvmTest {

    @Test
    fun accentedCharacters_DecomposeToBaseLetter() {
        val name = Name.of("this-is-fine", "this-one-too", "también_bueno")
        assertEquals("this-is-fine/this-one-too/tambien-bueno", name.toString())
    }
}
