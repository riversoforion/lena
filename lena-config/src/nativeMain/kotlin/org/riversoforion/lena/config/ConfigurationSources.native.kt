/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import org.riversoforion.lena.config.internal.customImpl
import org.riversoforion.lena.config.internal.forEnvironmentImpl
import org.riversoforion.lena.config.internal.prioritizedImpl

public actual object ConfigurationSources {

    public actual fun forEnvironment(): ConfigurationSource = forEnvironmentImpl()

    public actual fun custom(names: NameResolver, values: ValueResolver): ConfigurationSource =
        customImpl(names, values)

    public actual fun custom(
        names: NameResolver,
        values: ValueResolver,
        namespace: Namespace,
    ): ConfigurationSource = customImpl(names, values, namespace)

    public actual fun prioritized(
        first: ConfigurationSource,
        second: ConfigurationSource,
        vararg others: ConfigurationSource,
    ): ConfigurationSource = prioritizedImpl(first, second, others)
}
