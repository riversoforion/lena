/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals

class LazyDefaultsTest {

    private class FakeConfigurationSource(private val values: Map<String, String>) : ConfigurationSource {
        override fun getValue(namespace: Namespace, name: Name): String? {
            val key = (namespace.segments() + name.segments()).joinToString("/")
            return values[key]
        }
    }

    @Test
    fun lazyDefault_DeferrsEvaluation() {
        var evalCount = 0
        val source = FakeConfigurationSource(mapOf("a" to "10"))
        val config = object : ConfigurationProperties(source) {
            val a: Int by int("a")
            val b: Int by int("b") { evalCount++; a * 2 }
        }

        // Evaluation should happen on access, not during construction
        assertEquals(0, evalCount)
        assertEquals(20, config.b)
        assertEquals(1, evalCount)
    }

    @Test
    fun lazyDefault_AllowsForwardReference() {
        val source = FakeConfigurationSource(emptyMap())
        val config = object : ConfigurationProperties(source) {
            val connectionTimeout: Long by long("connection/timeout", default = 5_000L)
            val handshakeTimeout: Long by long(
                "handshake/timeout",
                default = { connectionTimeout + 1_000L }
            )
        }

        assertEquals(5_000L, config.connectionTimeout)
        assertEquals(6_000L, config.handshakeTimeout)
    }

    @Test
    fun lazyDefault_AllowsCrossPropertyReferencing() {
        val source = FakeConfigurationSource(mapOf("base" to "100"))
        val config = object : ConfigurationProperties(source) {
            val base: Int by int("base")
            val derived: Int by int("derived") { base + 50 }
        }

        assertEquals(100, config.base)
        assertEquals(150, config.derived)
    }

    @Test
    fun lazyDefault_StringVariant() {
        var evalCount = 0
        val source = FakeConfigurationSource(mapOf("prefix" to "hello"))
        val config = object : ConfigurationProperties(source) {
            val prefix: String by string("prefix")
            val fullMessage: String by string("full") { evalCount++; "$prefix world" }
        }

        assertEquals(0, evalCount)
        assertEquals("hello world", config.fullMessage)
        assertEquals(1, evalCount)
    }

    @Test
    fun lazyDefault_BooleanVariant() {
        var evalCount = 0
        val source = FakeConfigurationSource(mapOf("enabled" to "true"))
        val config = object : ConfigurationProperties(source) {
            val enabled: Boolean by boolean("enabled")
            val verbose: Boolean by boolean("verbose") { evalCount++; enabled }
        }

        assertEquals(0, evalCount)
        assertEquals(true, config.verbose)
        assertEquals(1, evalCount)
    }
}
