/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/** A [ConfigurationSource] that pairs a [NameResolver] with a [ValueResolver]. */
public class SimpleConfigurationSource internal constructor(
    internal val nameResolver: NameResolver,
    internal val valueResolver: ValueResolver,
) : ConfigurationSource {

    override fun getValue(namespace: Namespace, name: Name): String? =
        valueResolver.resolveValue(nameResolver.resolveName(namespace, name))
}
