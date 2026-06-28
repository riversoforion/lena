/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import org.riversoforion.lena.config.internal.normalizeForName

/**
 * An immutable, normalized hierarchical configuration property name.
 *
 * Segments are joined with [SEPARATOR] (`/`) and normalized: trimmed, lowercased,
 * non-ASCII characters removed (via Unicode NFD decomposition on JVM; best-effort on native),
 * and runs of non-alphanumeric characters replaced by `-`.
 *
 * Examples:
 * - `Name.of("service", "url")` → `"service/url"`
 * - `Name.of("NET", "read", "timeout")` → `"net/read/timeout"`
 */
public class Name private constructor(
    private val rawSegments: List<String>,
    private val fullName: String,
) : Comparable<Name> {

    public companion object {
        public const val SEPARATOR: String = "/"

        private val NON_ALPHANUMERIC: Regex = Regex("[^a-z0-9]+")

        public fun of(vararg segments: String): Name {
            require(segments.isNotEmpty()) { "Name must contain at least one segment" }
            return fromSegments(segments.toList(), allowEmpty = false)
        }

        public fun of(segments: List<String>): Name {
            require(segments.isNotEmpty()) { "Name must contain at least one segment" }
            return fromSegments(segments, allowEmpty = false)
        }

        internal fun forNamespace(segments: List<String>): Name =
            fromSegments(segments, allowEmpty = true)

        internal fun splitSegment(segment: String): List<String> =
            segment.split(SEPARATOR).filter { it.isNotBlank() }

        internal fun normalizeSegment(segment: String): String {
            if (segment.isBlank()) return ""
            val decomposed = normalizeForName(segment.trim())
            return NON_ALPHANUMERIC.replace(decomposed.lowercase(), "-")
        }

        private fun fromSegments(segments: List<String>, allowEmpty: Boolean): Name {
            val raw = segments
                .filter { it.isNotBlank() }
                .flatMap { splitSegment(it) }
            require(allowEmpty || raw.isNotEmpty()) { "Name must contain at least one segment" }
            val full = raw.mapNotNull { normalizeSegment(it).ifEmpty { null } }
                .joinToString(SEPARATOR)
            return Name(raw, full)
        }
    }

    public fun segments(): List<String> = rawSegments.toList()

    override fun toString(): String = fullName

    override fun equals(other: Any?): Boolean {
        if (other !is Name) return false
        return fullName == other.fullName
    }

    override fun hashCode(): Int = fullName.hashCode()

    override fun compareTo(other: Name): Int = fullName.compareTo(other.fullName)
}
