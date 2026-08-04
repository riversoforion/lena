/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.internal

import java.text.Normalizer

private val NON_ASCII = Regex("[^\\p{ASCII}]")

internal actual fun normalizeForName(input: String): String {
    val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
    return NON_ASCII.replace(decomposed, "")
}
