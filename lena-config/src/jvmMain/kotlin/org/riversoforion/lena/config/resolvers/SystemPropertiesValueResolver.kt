/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.riversoforion.lena.config.ValueResolver

/** Looks up values from JVM system properties. JVM-only — no native equivalent. */
public class SystemPropertiesValueResolver : ValueResolver {
    override fun resolveValue(name: String): String? = System.getProperty(name)
}
