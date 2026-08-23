/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.riversoforion.lena.config.ConfigurationSource
import org.riversoforion.lena.config.ConfigurationSources.forEnvironment
import org.riversoforion.lena.config.ConfigurationSources.forSystemProperties
import org.riversoforion.lena.config.ConfigurationSources.prioritized

actual fun defaultConfigurationSource(): ConfigurationSource = prioritized(forEnvironment(), forSystemProperties())
