/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.internal

/**
 * Applies Unicode NFD decomposition to [input] and strips non-ASCII characters, producing a
 * string containing only ASCII characters. Used to normalize Name/Namespace segments before
 * further processing.
 *
 * On JVM: uses `java.text.Normalizer` for correct NFD decomposition.
 * On native: best-effort — non-ASCII characters are removed without decomposition first, so
 * accented letters are dropped rather than converted to their base letter. This is a known
 * limitation of the native target.
 */
internal expect fun normalizeForName(input: String): String
