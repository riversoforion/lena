/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.riversoforion.lena.config.ConfigurationProperties
import org.riversoforion.lena.config.ConfigurationSource
import org.riversoforion.lena.config.Namespace

class ServiceConfig(source: ConfigurationSource, ns: Namespace) : ConfigurationProperties(source, ns) {

    val net: NetworkConfig = NetworkConfig(source, namespace.child("net"))

    val url: String by string("url")
    val apiKey: String by string("api", "key")
    val apiSecret: String by string("api", "secret")
}
