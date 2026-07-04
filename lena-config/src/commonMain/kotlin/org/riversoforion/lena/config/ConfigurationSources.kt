/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/**
 * Factory for creating [ConfigurationSource] instances.
 *
 * Common sources (all targets):
 * - [forEnvironment] — reads process environment variables
 * - [custom] — composes a [NameResolver] + [ValueResolver] pair
 * - [prioritized] — first-match wins across an ordered list of sources
 *
 * JVM-only source: [forSystemProperties][org.riversoforion.lena.config.ConfigurationSources.forSystemProperties]
 * is a member of the JVM `actual` and callable as `ConfigurationSources.forSystemProperties()` from both
 * Kotlin and Java on JVM targets.
 */
public expect object ConfigurationSources {

    public fun forEnvironment(): ConfigurationSource

    public fun custom(names: NameResolver, values: ValueResolver): ConfigurationSource

    public fun custom(names: NameResolver, values: ValueResolver, namespace: Namespace): ConfigurationSource

    public fun prioritized(
        first: ConfigurationSource,
        second: ConfigurationSource,
        vararg others: ConfigurationSource,
    ): ConfigurationSource
}
