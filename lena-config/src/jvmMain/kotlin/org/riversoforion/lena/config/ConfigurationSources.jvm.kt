/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import org.riversoforion.lena.config.resolvers.PropertyNameResolver
import org.riversoforion.lena.config.resolvers.SystemPropertiesValueResolver

/** JVM-only extension: create a [ConfigurationSource] backed by `System.getProperties()`. */
public fun ConfigurationSources.forSystemProperties(): ConfigurationSource =
    SimpleConfigurationSource(PropertyNameResolver(), SystemPropertiesValueResolver())
