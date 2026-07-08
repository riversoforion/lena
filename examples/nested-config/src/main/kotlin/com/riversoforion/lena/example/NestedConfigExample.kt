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
        out.printf("%30s = %s%n", "service.net.connectTimeout", config.service.net.connectTimeout)
        out.printf("%30s = %s%n", "net.readTimeout", config.net.readTimeout)
        out.printf("%30s = %s%n", "net.connectTimeout", config.net.connectTimeout)
        out.printf("%30s = %s%n", "localMode", config.localMode)
    }

    override fun printHelp(out: PrintStream) {
        out.print(
            """
            Demonstrates usage of nested ConfigurationProperties, composed via Kotlin
            property delegates (val url: String by string("url")).

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
