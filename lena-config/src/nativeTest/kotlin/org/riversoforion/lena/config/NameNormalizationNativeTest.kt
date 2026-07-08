/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Native targets have no `java.text.Normalizer`; accented characters are stripped outright
 * rather than decomposed to their base letter (documented limitation). See the JVM counterpart
 * of this test for the (different) NFD-decomposition behavior there.
 *
 * This is also the `nativeTest` proof-of-life that common logic (here, [Name.of]) actually runs
 * on a native target, not just JVM.
 */
class NameNormalizationNativeTest {

    @Test
    fun accentedCharacters_AreStrippedNotDecomposed() {
        val name = Name.of("this-is-fine", "this-one-too", "también_bueno")
        assertEquals("this-is-fine/this-one-too/tambin-bueno", name.toString())
    }
}
