/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.riversoforion.lena.config.ConfigurationSource
import org.riversoforion.lena.config.Namespace

expect fun defaultConfigurationSource(): ConfigurationSource

expect fun createApplicationConfig(source: ConfigurationSource, ns: Namespace): ApplicationConfig
