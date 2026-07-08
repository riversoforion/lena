/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfigurationPropertiesTest {

    @Test
    fun delegates_ResolveAndConvert() {
        val source = FakeConfigurationSource(
            mapOf(
                "str" to "hello",
                "bool" to "true",
                "short" to "5",
                "int" to "10",
                "long" to "100000",
                "float" to "1.25",
                "double" to "2.5",
            )
        )
        val config = DelegateConfig(source)

        assertEquals("hello", config.str)
        assertEquals("fallback", config.strDefault)
        assertNull(config.strOptional)
        assertTrue(config.bool)
        assertTrue(config.boolDefault)
        assertEquals(5.toShort(), config.shortVal)
        assertEquals(7.toShort(), config.shortDefault)
        assertEquals(10, config.intVal)
        assertEquals(42, config.intDefault)
        assertEquals(100_000L, config.longVal)
        assertEquals(100L, config.longDefault)
        assertEquals(1.25f, config.floatVal)
        assertEquals(1.5f, config.floatDefault)
        assertEquals(2.5, config.doubleVal)
        assertEquals(2.5, config.doubleDefault)
    }

    @Test
    fun delegates_ThrowWhenMissingAndNoDefault() {
        val config = DelegateConfig(FakeConfigurationSource(emptyMap()))
        assertFailsWith<MissingConfigurationException> { config.missingRequired }
    }

    @Test
    fun missingSetFlags() {
        val source = FakeConfigurationSource(mapOf("str" to "hello"))
        val config = DelegateConfig(source)

        assertTrue(config.isMissing(Name.of("does-not-exist")))
        assertFalse(config.isSet(Name.of("does-not-exist")))
        assertFalse(config.isMissing(Name.of("str")))
        assertTrue(config.isSet(Name.of("str")))
    }

    @Test
    fun protectedAccessors_JavaStyleSubclass() {
        val source = FakeConfigurationSource(
            mapOf(
                "url" to "http://example.com",
                "port" to "8080",
                "timeout" to "5000",
                "ratio" to "0.5",
                "flag" to "yes",
                "sample" to "3",
                "rate" to "1.5",
            )
        )
        val config = JavaStyleConfig(source)

        assertEquals("http://example.com", config.url())
        assertEquals("http://example.com", config.optionalUrl())
        assertNull(config.missingOptional())
        assertEquals(8080, config.port())
        assertEquals(5_000L, config.timeout())
        assertEquals(0.5, config.ratio())
        assertTrue(config.flag())
        assertEquals(3.toShort(), config.sample())
        assertEquals(1.5f, config.rate())
        assertFailsWith<MissingConfigurationException> { config.requiredMissing() }
    }

    @Test
    fun customConverter_IsUsed() {
        // AlwaysTrueConverter.toBoolean ignores its input, so this only passes if
        // ConfigurationProperties actually routes through the overridden createConverter().
        val source = FakeConfigurationSource(mapOf("any" to "false"))
        val config = CustomConverterConfig(source)

        assertTrue(config.bool)
    }

    @Test
    fun explicitNestedComposition() {
        val source = FakeConfigurationSource(mapOf("child/value" to "nested"))
        val config = ParentConfig(source)

        assertEquals("nested", config.child.value)
    }
}

private class FakeConfigurationSource(private val values: Map<String, String>) : ConfigurationSource {
    override fun getValue(namespace: Namespace, name: Name): String? {
        val key = (namespace.segments() + name.segments()).joinToString("/")
        return values[key]
    }
}

private class DelegateConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    val str: String by string("str")
    val strDefault: String by string("str-default", default = "fallback")
    val strOptional: String? by optionalString("str-optional")
    val bool: Boolean by boolean("bool")
    val boolDefault: Boolean by boolean("bool-default", default = true)
    val shortVal: Short by short("short")
    val shortDefault: Short by short("short-default", default = 7)
    val intVal: Int by int("int")
    val intDefault: Int by int("int-default", default = 42)
    val longVal: Long by long("long")
    val longDefault: Long by long("long-default", default = 100L)
    val floatVal: Float by float("float")
    val floatDefault: Float by float("float-default", default = 1.5f)
    val doubleVal: Double by double("double")
    val doubleDefault: Double by double("double-default", default = 2.5)
    val missingRequired: String by string("does-not-exist")
}

private class JavaStyleConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    fun url(): String = stringVal(Name.of("url"))
    fun optionalUrl(): String? = optionalStringVal(Name.of("url"))
    fun missingOptional(): String? = optionalStringVal(Name.of("does-not-exist"))
    fun requiredMissing(): String = stringVal(Name.of("does-not-exist"))
    fun port(): Int = intVal(Name.of("port"))
    fun timeout(): Long = longVal(Name.of("timeout"))
    fun ratio(): Double = doubleVal(Name.of("ratio"))
    fun flag(): Boolean = booleanVal(Name.of("flag"))
    fun sample(): Short = shortVal(Name.of("sample"))
    fun rate(): Float = floatVal(Name.of("rate"))
}

private class AlwaysTrueConverter : ValueConverter {
    override fun toBoolean(value: String?): Boolean = true
    override fun toShort(value: String?): Short = 99
    override fun toInt(value: String?): Int = 99
    override fun toLong(value: String?): Long = 99L
    override fun toFloat(value: String?): Float = 99f
    override fun toDouble(value: String?): Double = 99.0
}

private class CustomConverterConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    override fun createConverter(): ValueConverter = AlwaysTrueConverter()
    val bool: Boolean by boolean("any")
}

private class ChildConfig(source: ConfigurationSource, ns: Namespace) : ConfigurationProperties(source, ns) {
    val value: String by string("value")
}

private class ParentConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    val child: ChildConfig = ChildConfig(source, namespace.child("child"))
}
