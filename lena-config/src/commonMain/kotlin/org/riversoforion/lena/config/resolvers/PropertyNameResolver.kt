/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.riversoforion.lena.config.Name
import org.riversoforion.lena.config.NameResolver
import org.riversoforion.lena.config.Namespace

/**
 * Converts a [Namespace] + [Name] pair into a dot-separated Java system-property-style key.
 *
 * Example: `Namespace.of("app")` + `Name.of("service", "url")` → `app.service.url`.
 */
public open class PropertyNameResolver : NameResolver {

    override fun resolveName(namespace: Namespace, name: Name): String =
        joinParts(namespace.resolveProperty(name))

    protected open fun joinParts(parts: List<String>): String = parts.joinToString(".")
}
