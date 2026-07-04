/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.jvm.JvmStatic
import org.riversoforion.lena.config.resolvers.EnvironmentNameResolver
import org.riversoforion.lena.config.resolvers.EnvironmentValueResolver

/**
 * Factory for creating [ConfigurationSource] instances.
 *
 * Common sources (all targets):
 * - [forEnvironment] — reads process environment variables
 * - [custom] — composes a [NameResolver] + [ValueResolver] pair
 * - [prioritized] — first-match wins across an ordered list of sources
 *
 * JVM-only source: `ConfigurationSources.forSystemProperties()` (in jvmMain).
 */
public object ConfigurationSources {

    @JvmStatic
    public fun forEnvironment(): ConfigurationSource =
        SimpleConfigurationSource(EnvironmentNameResolver(), EnvironmentValueResolver())

    @JvmStatic
    public fun custom(names: NameResolver, values: ValueResolver): ConfigurationSource =
        SimpleConfigurationSource(names, values)

    @JvmStatic
    public fun custom(names: NameResolver, values: ValueResolver, namespace: Namespace): ConfigurationSource =
        CompositeConfigurationSource.namespaced(custom(names, values), namespace)

    @JvmStatic
    public fun prioritized(
        first: ConfigurationSource,
        second: ConfigurationSource,
        vararg others: ConfigurationSource,
    ): ConfigurationSource {
        val all = (listOf(first, second) + others).map { it to null as Namespace? }
        return PrioritizedConfigurationSource(all)
    }
}
