/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.riversoforion.lena.config.ValueResolver
import org.riversoforion.lena.config.internal.platformGetEnv

/** Looks up a value from the process environment using [platformGetEnv]. */
public class EnvironmentValueResolver : ValueResolver {
    override fun resolveValue(name: String): String? = platformGetEnv(name)
}
