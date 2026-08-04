/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import java.io.PrintStream

class NestedConfigExample : ExampleApplication() {

    private val config = ApplicationConfig()

    fun printConfig(out: PrintStream) {
        out.printf("%30s = %s%n", "service.url", config.service.url)
        out.printf("%30s = %s%n", "service.apiKey", config.service.apiKey)
        out.printf("%30s = %s%n", "service.apiSecret", config.service.apiSecret)
        out.printf("%30s = %s%n", "service.net.readTimeout", config.service.net.readTimeout)
        out.printf("%30s = %s%n", "service.net.connectionTimeout", config.service.net.connectionTimeout)
        out.printf("%30s = %s%n", "net.readTimeout", config.net.readTimeout)
        out.printf("%30s = %s%n", "net.connectionTimeout", config.net.connectionTimeout)
        out.printf("%30s = %s%n", "localMode", config.localMode)
    }

    override fun printHelp(out: PrintStream) {
        out.print(
            """
            Demonstrates usage of nested ConfigurationProperties, composed via Kotlin
            property delegates (val url: String by string("url")).

            Run with environment variables or system properties set. The top-level `net` and the
            `service`-nested `net` are independent configuration properties and must each be
            supplied separately if you want to override their defaults. The following
            configuration properties are supported:
                Environment Variable            System Property                  Type    Default
                SERVICE_URL                     service.url                      string  (required)
                SERVICE_API_KEY                 service.api.key                  string  (required)
                SERVICE_API_SECRET              service.api.secret               string  (required)
                SERVICE_NET_READ_TIMEOUT        service.net.read.timeout         long    5000
                SERVICE_NET_CONNECTION_TIMEOUT  service.net.connection.timeout   long    5000
                NET_READ_TIMEOUT                net.read.timeout                 long    5000
                NET_CONNECTION_TIMEOUT          net.connection.timeout           long    5000
                LOCAL_MODE                      local.mode                       flag    (required)

            """.trimIndent()
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("=== Nested Configuration Example ===")
            val example = NestedConfigExample()
            if (example.isHelpRequested(args)) {
                example.printHelp(System.out)
                return
            }
            example.printConfig(System.out)
        }
    }
}
