/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.internal

// Native targets have no java.text.Normalizer. We remove non-ASCII characters directly
// without NFD decomposition, so accented characters (e.g. 'é') are dropped rather than
// reduced to their base letter. This is a known, documented limitation.
private val NON_ASCII = Regex("[^\\p{ASCII}]")

internal actual fun normalizeForName(input: String): String =
    NON_ASCII.replace(input, "")
