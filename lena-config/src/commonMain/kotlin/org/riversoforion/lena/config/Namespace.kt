/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/**
 * A normalized configuration namespace (scoping prefix).
 *
 * Always begins with [Name.SEPARATOR]. The root namespace is represented as `"/"`.
 * Segments follow the same normalization rules as [Name].
 *
 * Examples:
 * - `Namespace.root()` → `"/"`
 * - `Namespace.of("app", "service")` → `"/app/service"`
 * - `Namespace.of("app").child("network")` → `"/app/network"`
 */
public class Namespace private constructor(
    private val name: Name,
    private val fullName: String,
) : Comparable<Namespace> {

    public companion object {
        public fun root(): Namespace = of()

        public fun of(vararg segments: String): Namespace {
            val flat = segments.filter { it.isNotBlank() }.flatMap { Name.splitSegment(it) }
            return fromSegmentList(flat)
        }

        public fun parse(name: String): Namespace {
            val segments = name.split(Name.SEPARATOR)
            return if (segments.size == 1 && segments[0].isEmpty()) root()
            else fromSegmentList(segments)
        }

        private fun fromSegmentList(segments: List<String>): Namespace {
            val name = Name.forNamespace(segments)
            val fullName = Name.SEPARATOR + name.toString()
            return Namespace(name, fullName)
        }
    }

    public fun name(): String = fullName

    public fun segments(): List<String> = name.segments()

    public fun child(childName: String): Namespace {
        require(childName.isNotBlank()) { "Child namespace must contain at least one new segment" }
        return if (childName.startsWith(Name.SEPARATOR)) {
            parse(childName)
        } else {
            val childSegments = childName.split(Name.SEPARATOR).filter { it.isNotBlank() }
            require(childSegments.isNotEmpty()) { "Child namespace must contain at least one new segment" }
            fromSegmentList(name.segments() + childSegments)
        }
    }

    public fun resolveProperty(propName: Name): List<String> {
        return name.segments() + propName.segments()
    }

    override fun toString(): String = fullName

    override fun equals(other: Any?): Boolean {
        if (other !is Namespace) return false
        return fullName == other.fullName
    }

    override fun hashCode(): Int = fullName.hashCode()

    override fun compareTo(other: Namespace): Int = fullName.compareTo(other.fullName)
}
