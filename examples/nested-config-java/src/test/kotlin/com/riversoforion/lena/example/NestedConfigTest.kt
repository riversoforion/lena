/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.riversoforion.lena.config.Namespace
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.properties.SystemProperties

@ExtendWith(SystemStubsExtension::class)
class NestedConfigTest {

    @SystemStub
    private lateinit var envVars: EnvironmentVariables

    @SystemStub
    private lateinit var sysProps: SystemProperties

    @Test
    fun configViaEnvironmentVariables() {
        envVars.set("ENVVARS_SERVICE_URL", "http://localhost:8080")
            .set("ENVVARS_SERVICE_API_KEY", "apiKey")
            .set("ENVVARS_SERVICE_API_SECRET", "secret")
            .set("ENVVARS_NET_CONNECTION_TIMEOUT", "1000")
            .set("ENVVARS_NET_READ_TIMEOUT", "5000")
            .set("ENVVARS_LOCAL_MODE", "Y")
        val config = ApplicationConfig(Namespace.of("envvars"))

        assertThat(config.service.url).isEqualTo("http://localhost:8080")
        assertThat(config.service.apiKey).isEqualTo("apiKey")
        assertThat(config.service.apiSecret).isEqualTo("secret")
        assertThat(config.net.connectionTimeout).isEqualTo(1_000L)
        assertThat(config.net.readTimeout).isEqualTo(5_000L)
        assertThat(config.isLocalMode).isTrue()
    }

    @Test
    fun configViaSystemProperties() {
        sysProps.set("sysprops.service.url", "http://localhost:8080")
            .set("sysprops.service.api.key", "apiKey")
            .set("sysprops.service.api.secret", "secret")
            .set("sysprops.net.connection.timeout", "1000")
            .set("sysprops.net.read.timeout", "5000")
            .set("sysprops.local.mode", "on")
        val config = ApplicationConfig(Namespace.of("sysprops"))

        assertThat(config.service.url).isEqualTo("http://localhost:8080")
        assertThat(config.service.apiKey).isEqualTo("apiKey")
        assertThat(config.service.apiSecret).isEqualTo("secret")
        assertThat(config.net.connectionTimeout).isEqualTo(1_000L)
        assertThat(config.net.readTimeout).isEqualTo(5_000L)
        assertThat(config.isLocalMode).isTrue()
    }

    @Test
    fun mixedConfig() {
        sysProps.set("service.url", "http://localhost:8080")
            .set("service.api.key", "apiKey")
            .set("service.api.secret", "secret")
            .set("net.connection.timeout", "1000")
            .set("net.read.timeout", "5000")
            .set("local.mode", "off")
        // Environment variables take precedence
        envVars.set("SERVICE_API_SECRET", "a-different-secret")
            .set("NET_CONNECTION_TIMEOUT", "2000")
            .set("NET_READ_TIMEOUT", "5000")
            .set("LOCAL_MODE", "TRUE")
        val config = ApplicationConfig(Namespace.root())

        assertThat(config.service.url).isEqualTo("http://localhost:8080")
        assertThat(config.service.apiKey).isEqualTo("apiKey")
        assertThat(config.service.apiSecret).isEqualTo("a-different-secret")
        assertThat(config.net.connectionTimeout).isEqualTo(2_000L)
        assertThat(config.net.readTimeout).isEqualTo(5_000L)
        assertThat(config.isLocalMode).isTrue()
    }
}
