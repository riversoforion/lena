/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ComplexTypeDelegatesTest {

    private class FakeConfigurationSource(private val values: Map<String, String>) : ConfigurationSource {
        override fun getValue(namespace: Namespace, name: Name): String? {
            val key = (namespace.segments() + name.segments()).joinToString("/")
            return values[key]
        }
    }

    private class ComplexTypeConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
        val timeout: Duration by duration("timeout")
        val timeoutWithDefault: Duration by duration(
            "timeout-missing",
            default = 10.toDuration(DurationUnit.SECONDS)
        )
        val ports: List<Int> by intList("ports")
        val portsWithDefault: List<Int> by intList("ports-missing", default = listOf(8080))
        val hosts: List<String> by stringList("hosts")
        val tags: Set<String> by stringSet("tags")
        val config: Map<String, String> by stringMap("config")
        val customValue: Int by converted("custom") { it.toInt() * 2 }
        val customValueWithDefault: Int by converted("custom-missing", default = 42) { it.toInt() * 2 }
    }

    @Test
    fun duration_ResolvesValue() {
        val source = FakeConfigurationSource(mapOf("timeout" to "PT5S"))
        val config = ComplexTypeConfig(source)
        assertEquals(5.toDuration(DurationUnit.SECONDS), config.timeout)
    }

    @Test
    fun duration_UsesDefault() {
        val source = FakeConfigurationSource(emptyMap())
        val config = ComplexTypeConfig(source)
        assertEquals(10.toDuration(DurationUnit.SECONDS), config.timeoutWithDefault)
    }

    @Test
    fun intList_ResolvesCommaSeparatedValues() {
        val source = FakeConfigurationSource(mapOf("ports" to "8080,8081,8082"))
        val config = ComplexTypeConfig(source)
        assertEquals(listOf(8080, 8081, 8082), config.ports)
    }

    @Test
    fun intList_UsesDefault() {
        val source = FakeConfigurationSource(emptyMap())
        val config = ComplexTypeConfig(source)
        assertEquals(listOf(8080), config.portsWithDefault)
    }

    @Test
    fun stringList_ResolvesCommaSeparatedValues() {
        val source = FakeConfigurationSource(mapOf("hosts" to "localhost,example.com,test.com"))
        val config = ComplexTypeConfig(source)
        assertEquals(listOf("localhost", "example.com", "test.com"), config.hosts)
    }

    @Test
    fun stringSet_DeduplicatesAndResolvesValues() {
        val source = FakeConfigurationSource(mapOf("tags" to "app,web,app,prod"))
        val config = ComplexTypeConfig(source)
        assertEquals(setOf("app", "web", "prod"), config.tags)
    }

    @Test
    fun stringMap_ResolvesKeyValuePairs() {
        val source = FakeConfigurationSource(
            mapOf("config" to "db.host=localhost,db.port=5432")
        )
        val config = ComplexTypeConfig(source)
        assertEquals(mapOf("db.host" to "localhost", "db.port" to "5432"), config.config)
    }

    @Test
    fun converted_AppliesConversionFunction() {
        val source = FakeConfigurationSource(mapOf("custom" to "21"))
        val config = ComplexTypeConfig(source)
        assertEquals(42, config.customValue)
    }

    @Test
    fun converted_UsesDefault() {
        val source = FakeConfigurationSource(emptyMap())
        val config = ComplexTypeConfig(source)
        assertEquals(42, config.customValueWithDefault)
    }

    @Test
    fun duration_ThrowsWhenMissing() {
        val source = FakeConfigurationSource(emptyMap())
        val config = ComplexTypeConfig(source)
        assertFailsWith<IllegalArgumentException> {
            config.timeout
        }
    }

    @Test
    fun intList_ReturnsEmptyWhenMissing() {
        val source = FakeConfigurationSource(emptyMap())
        val config = object : ConfigurationProperties(source) {
            val items: List<Int> by intList("missing")
        }
        assertEquals(emptyList(), config.items)
    }
}
