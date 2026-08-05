/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

class NestedConfigExample {

    private val config = ApplicationConfig()

    fun run(args: Array<String>) {
        println("=== Nested Configuration Example (Kotlin) ===")

        if (args.contains("-h") || args.contains("--help")) {
            printHelp()
            return
        }

        printConfig()
    }

    private fun printConfig() {
        println("${"service.url".padStart(35)} = ${config.service.url}")
        println("${"service.apiKey".padStart(35)} = ${config.service.apiKey}")
        println("${"service.apiSecret".padStart(35)} = ${config.service.apiSecret}")
        println("${"service.net.readTimeout".padStart(35)} = ${config.service.net.readTimeout}")
        println("${"service.net.connectionTimeout".padStart(35)} = ${config.service.net.connectionTimeout}")
        println("${"net.readTimeout".padStart(35)} = ${config.net.readTimeout}")
        println("${"net.connectionTimeout".padStart(35)} = ${config.net.connectionTimeout}")
        println("${"localMode".padStart(35)} = ${config.localMode}")
    }

    private fun printHelp() {
        println(
            """
            Demonstrates usage of nested ConfigurationProperties, composed via Kotlin
            property delegates (val url: String by string("url")). (Kotlin/KMP)

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
}

fun main(args: Array<String>) {
    NestedConfigExample().run(args)
}
