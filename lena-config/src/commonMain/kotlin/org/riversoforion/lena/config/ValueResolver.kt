/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/** Looks up a value by its resolved key string, returning `null` if absent. */
public fun interface ValueResolver {
    public fun resolveValue(name: String): String?
}
