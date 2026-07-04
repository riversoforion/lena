/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.internal

import org.riversoforion.lena.config.CompositeConfigurationSource
import org.riversoforion.lena.config.ConfigurationSource
import org.riversoforion.lena.config.NameResolver
import org.riversoforion.lena.config.Namespace
import org.riversoforion.lena.config.PrioritizedConfigurationSource
import org.riversoforion.lena.config.SimpleConfigurationSource
import org.riversoforion.lena.config.ValueResolver
import org.riversoforion.lena.config.resolvers.EnvironmentNameResolver
import org.riversoforion.lena.config.resolvers.EnvironmentValueResolver

internal fun forEnvironmentImpl(): ConfigurationSource =
    SimpleConfigurationSource(EnvironmentNameResolver(), EnvironmentValueResolver())

internal fun customImpl(names: NameResolver, values: ValueResolver): ConfigurationSource =
    SimpleConfigurationSource(names, values)

internal fun customImpl(names: NameResolver, values: ValueResolver, namespace: Namespace): ConfigurationSource =
    CompositeConfigurationSource.namespaced(customImpl(names, values), namespace)

internal fun prioritizedImpl(
    first: ConfigurationSource,
    second: ConfigurationSource,
    others: Array<out ConfigurationSource>,
): ConfigurationSource {
    val all = (listOf(first, second) + others).map { it to null as Namespace? }
    return PrioritizedConfigurationSource(all)
}
