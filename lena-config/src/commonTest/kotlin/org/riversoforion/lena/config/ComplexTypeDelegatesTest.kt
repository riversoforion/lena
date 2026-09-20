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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

        @OptIn(ExperimentalUuidApi::class)
        val id: Uuid by uuid("id")

        @OptIn(ExperimentalUuidApi::class)
        val idWithDefault: Uuid by uuid("id-missing", default = Uuid.parse("00000000-0000-0000-0000-000000000000"))

        val timeouts: List<Duration> by durationList("timeouts")
        val timeoutsWithDefault: List<Duration> by durationList(
            "timeouts-missing",
            default = listOf(1.toDuration(DurationUnit.SECONDS))
        )

        init {
            converter.register<Point> { s ->
                val (x, y) = s.split(",").map { it.trim().toInt() }
                Point(x, y)
            }
        }

        val point: Point by custom("point")
        val pointWithDefault: Point by custom("point-missing", default = Point(0, 0))
    }

    private data class Point(val x: Int, val y: Int)

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

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun uuid_ResolvesValue() {
        val source = FakeConfigurationSource(
            mapOf("id" to "550e8400-e29b-41d4-a716-446655440000")
        )
        val config = ComplexTypeConfig(source)
        assertEquals(Uuid.parse("550e8400-e29b-41d4-a716-446655440000"), config.id)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun uuid_UsesDefault() {
        val source = FakeConfigurationSource(emptyMap())
        val config = ComplexTypeConfig(source)
        assertEquals(Uuid.parse("00000000-0000-0000-0000-000000000000"), config.idWithDefault)
    }

    @Test
    fun durationList_ResolvesCommaSeparatedValues() {
        val source = FakeConfigurationSource(mapOf("timeouts" to "5s,2m,1h"))
        val config = ComplexTypeConfig(source)
        assertEquals(
            listOf(
                5.toDuration(DurationUnit.SECONDS),
                2.toDuration(DurationUnit.MINUTES),
                1.toDuration(DurationUnit.HOURS),
            ),
            config.timeouts,
        )
    }

    @Test
    fun durationList_UsesDefault() {
        val source = FakeConfigurationSource(emptyMap())
        val config = ComplexTypeConfig(source)
        assertEquals(listOf(1.toDuration(DurationUnit.SECONDS)), config.timeoutsWithDefault)
    }

    @Test
    fun custom_UsesRegisteredConverter() {
        val source = FakeConfigurationSource(mapOf("point" to "3, 4"))
        val config = ComplexTypeConfig(source)
        assertEquals(Point(3, 4), config.point)
    }

    @Test
    fun custom_UsesDefaultWhenMissing() {
        val source = FakeConfigurationSource(emptyMap())
        val config = ComplexTypeConfig(source)
        assertEquals(Point(0, 0), config.pointWithDefault)
    }
}
