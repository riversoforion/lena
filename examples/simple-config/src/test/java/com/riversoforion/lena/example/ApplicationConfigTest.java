/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.riversoforion.lena.config.Namespace;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SystemStubsExtension.class)
class ApplicationConfigTest {

    @SystemStub
    private EnvironmentVariables envVars;
    @SystemStub
    private SystemProperties sysProps;

    @Test
    void configViaEnvironmentVariables() {

        envVars.set("ENVVARS_SERVICE_URL", "http://localhost:8080")
               .set("ENVVARS_SERVICE_API_KEY", "apiKey")
               .set("ENVVARS_SERVICE_API_SECRET", "secret")
               .set("ENVVARS_NET_CONNECTION_TIMEOUT", "1000")
               .set("ENVVARS_NET_READ_TIMEOUT", "5000")
               .set("ENVVARS_LOCAL_MODE", "Y");
        ApplicationConfig config = new ApplicationConfig(Namespace.of("envvars"));

        assertEquals("http://localhost:8080", config.serviceUrl());
        assertEquals("apiKey", config.serviceApiKey());
        assertEquals("secret", config.serviceApiSecret());
        assertEquals(1_000L, config.netConnectionTimeout());
        assertEquals(5_000L, config.netReadTimeout());
        assertTrue(config.isLocalMode());
    }

    @Test
    void configViaSystemProperties() {

        sysProps.set("sysprops.service.url", "http://localhost:8080")
                .set("sysprops.service.api.key", "apiKey")
                .set("sysprops.service.api.secret", "secret")
                .set("sysprops.net.connection.timeout", "1000")
                .set("sysprops.net.read.timeout", "5000")
                .set("sysprops.local.mode", "on");
        ApplicationConfig config = new ApplicationConfig(Namespace.of("sysprops"));

        assertEquals("http://localhost:8080", config.serviceUrl());
        assertEquals("apiKey", config.serviceApiKey());
        assertEquals("secret", config.serviceApiSecret());
        assertEquals(1_000L, config.netConnectionTimeout());
        assertEquals(5_000L, config.netReadTimeout());
        assertTrue(config.isLocalMode());
    }

    @Test
    void mixedConfig() {

        sysProps.set("mixed.service.url", "http://localhost:8080")
                .set("mixed.service.api.key", "apiKey")
                .set("mixed.service.api.secret", "secret")
                .set("mixed.net.connection.timeout", "1000")
                .set("mixed.net.read.timeout", "5000")
                .set("mixed.local.mode", "off");
        // Environment variables take precedence
        envVars.set("MIXED_SERVICE_API_SECRET", "a-different-secret")
               .set("MIXED_NET_CONNECTION_TIMEOUT", "2000")
               .set("MIXED_NET_READ_TIMEOUT", "5000")
               .set("MIXED_LOCAL_MODE", "TRUE");
        ApplicationConfig config = new ApplicationConfig(Namespace.of("mixed"));

        assertEquals("http://localhost:8080", config.serviceUrl());
        assertEquals("apiKey", config.serviceApiKey());
        assertEquals("a-different-secret", config.serviceApiSecret());
        assertEquals(2_000L, config.netConnectionTimeout());
        assertEquals(5_000L, config.netReadTimeout());
        assertTrue(config.isLocalMode());
    }
}
