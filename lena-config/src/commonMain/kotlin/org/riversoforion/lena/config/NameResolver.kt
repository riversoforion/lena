/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/** Converts a [Namespace] + [Name] pair into a concrete lookup key string (e.g. env var name). */
public fun interface NameResolver {
    public fun resolveName(namespace: Namespace, name: Name): String
}
