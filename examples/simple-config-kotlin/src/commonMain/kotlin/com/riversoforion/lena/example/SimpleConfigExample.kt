/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.riversoforion.lena.config.Namespace

class SimpleConfigExample {

    private val config: ApplicationConfig = createApplicationConfig(
        defaultConfigurationSource(),
        Namespace.root()
    )

    fun run(args: Array<String>) {
        println("=== Simple Configuration Example (Kotlin) ===")

        if (args.contains("-h") || args.contains("--help")) {
            printHelp()
            return
        }

        printConfig()
    }

    private fun printConfig() {
        println("${"serviceUrl".padStart(25)} = ${config.serviceUrl()}")
        println("${"serviceApiKey".padStart(25)} = ${config.serviceApiKey()}")
        println("${"serviceApiSecret".padStart(25)} = ${config.serviceApiSecret()}")
        println("${"netConnectionTimeout".padStart(25)} = ${config.netConnectionTimeout()}")
        println("${"netReadTimeout".padStart(25)} = ${config.netReadTimeout()}")
        println("${"isLocalMode".padStart(25)} = ${config.isLocalMode()}")
    }

    private fun printHelp() {
        println("""
            Demonstrates a simple usage of ConfigurationProperties (Kotlin/KMP)
            
            Run with environment variables or system properties set. The following configuration properties are supported:
                Environment Variable    System Property         Type
                SERVICE_URL             service.url             string
                SERVICE_API_KEY         service.api.key         string
                SERVICE_API_SECRET      service.api.secret      string
                NET_CONNECTION_TIMEOUT  net.connection.timeout  long
                NET_READ_TIMEOUT        net.read.timeout        long
                LOCAL_MODE              local.mode              flag
        """.trimIndent())
    }
}

fun main(args: Array<String>) {
    SimpleConfigExample().run(args)
}
