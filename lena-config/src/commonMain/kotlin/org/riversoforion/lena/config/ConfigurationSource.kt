/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/** Retrieves a raw string value for the given [namespace] + [name] pair, or `null` if absent. */
public fun interface ConfigurationSource {
    public fun getValue(namespace: Namespace, name: Name): String?
}
