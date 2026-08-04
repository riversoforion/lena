/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KspGenerationTest {

    private class FakeConfigurationSource(private val values: Map<String, String>) : ConfigurationSource {
        override fun getValue(namespace: Namespace, name: Name): String? {
            val key = (namespace.segments() + name.segments()).joinToString("/")
            return values[key]
        }
    }

    @Test
    fun `generated implementation should resolve values correctly`() {
        val source = FakeConfigurationSource(mapOf(
            "service/url" to "https://api.example.com",
            "port" to "8080",
            "timeout" to "5000",
            "active" to "true",
            "optional" to "present"
        ))
        
        val config: KspTestConfig = KspTestConfigImpl(source)
        
        assertThat(config.serviceUrl()).isEqualTo("https://api.example.com")
        assertThat(config.port()).isEqualTo(8080)
        assertThat(config.timeout()).isEqualTo(5000L)
        assertThat(config.isActive()).isTrue()
        assertThat(config.optionalValue()).isEqualTo("present")
    }

    @Test
    fun `generated implementation should support optional values`() {
        val source = FakeConfigurationSource(mapOf(
            "service/url" to "https://api.example.com",
            "port" to "8080",
            "timeout" to "5000",
            "active" to "true"
            // optional is missing
        ))
        
        val config: KspTestConfig = KspTestConfigImpl(source)
        
        assertThat(config.optionalValue()).isNull()
    }

    @Test
    fun `generated implementation should register metadata`() {
        val source = FakeConfigurationSource(emptyMap())
        val config = KspTestConfigImpl(source)
        
        val metadata = PropertyRegistry.getMetadata(config::class)
        assertThat(metadata).isNotNull
        assertThat(metadata).hasSize(5)
        
        assertThat(metadata?.map { it.name.toString() }).containsExactly(
            "service/url", "port", "timeout", "active", "optional"
        )
    }
}
