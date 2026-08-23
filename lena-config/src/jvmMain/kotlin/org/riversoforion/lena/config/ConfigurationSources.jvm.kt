/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.jvm.JvmStatic
import org.riversoforion.lena.config.internal.customImpl
import org.riversoforion.lena.config.internal.forEnvironmentImpl
import org.riversoforion.lena.config.internal.prioritizedImpl
import org.riversoforion.lena.config.resolvers.PropertyNameResolver
import org.riversoforion.lena.config.resolvers.SystemPropertiesValueResolver

public actual object ConfigurationSources {

    @JvmStatic
    public actual fun forEnvironment(): ConfigurationSource = forEnvironmentImpl()

    @JvmStatic
    public actual fun custom(names: NameResolver, values: ValueResolver): ConfigurationSource =
        customImpl(names, values)

    @JvmStatic
    public actual fun custom(
        names: NameResolver,
        values: ValueResolver,
        namespace: Namespace,
    ): ConfigurationSource = customImpl(names, values, namespace)

    @JvmStatic
    public actual fun prioritized(
        first: ConfigurationSource,
        second: ConfigurationSource,
        vararg others: ConfigurationSource,
    ): ConfigurationSource = prioritizedImpl(first, second, others)

    /** JVM-only: creates a [ConfigurationSource] backed by `System.getProperties()`. */
    @JvmStatic
    public fun forSystemProperties(): ConfigurationSource =
        SimpleConfigurationSource(PropertyNameResolver(), SystemPropertiesValueResolver())
}
