/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.riversoforion.lena.config.ConfigurationProperties
import org.riversoforion.lena.config.ConfigurationSource
import org.riversoforion.lena.config.Namespace

/**
 * Exposes a set of environment variables and/or system properties with type-safe accessors,
 * including nested sub-configurations composed explicitly (no global registry).
 */
class ApplicationConfig(
    ns: Namespace = Namespace.root(),
    backingSource: ConfigurationSource = defaultConfigurationSource(),
) : ConfigurationProperties(backingSource, ns) {

    val service: ServiceConfig = ServiceConfig(backingSource, namespace.child("service"))
    val net: NetworkConfig = NetworkConfig(backingSource, namespace.child("net"))

    val localMode: Boolean by boolean("local", "mode")
}
