/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.riversoforion.lena.config.ConfigurationProperties
import org.riversoforion.lena.config.ConfigurationSource
import org.riversoforion.lena.config.Namespace

class NetworkConfig(source: ConfigurationSource, ns: Namespace) : ConfigurationProperties(source, ns) {

    val readTimeout: Long by long("read", "timeout")
    val connectTimeout: Long by long("connect", "timeout")
}
