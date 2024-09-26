/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private final ApplicationConfig config = new ApplicationConfig();

    @Test
    void configViaEnvironmentVariables() {

        envVars.set("SERVICE_URL", "http://localhost:8080")
               .set("SERVICE_API_KEY", "apiKey")
               .set("SERVICE_API_SECRET", "secret")
               .set("NET_CONNECTION_TIMEOUT", "1000")
               .set("NET_READ_TIMEOUT", "5000")
               .set("LOCAL_MODE", "Y");

        assertEquals("http://localhost:8080", config.serviceUrl());
        assertEquals("apiKey", config.serviceApiKey());
        assertEquals("secret", config.serviceApiSecret());
        assertEquals(1_000L, config.netConnectionTimeout());
        assertEquals(5_000L, config.netReadTimeout());
        assertTrue(config.isLocalMode());
    }

    @Test
    void configViaSystemProperties() {

        sysProps.set("service.url", "http://localhost:8080")
                .set("service.api.key", "apiKey")
                .set("service.api.secret", "secret")
                .set("net.connection.timeout", "1000")
                .set("net.read.timeout", "5000")
                .set("local.mode", "on");

        assertEquals("http://localhost:8080", config.serviceUrl());
        assertEquals("apiKey", config.serviceApiKey());
        assertEquals("secret", config.serviceApiSecret());
        assertEquals(1_000L, config.netConnectionTimeout());
        assertEquals(5_000L, config.netReadTimeout());
        assertTrue(config.isLocalMode());
    }

    @Test
    void mixedConfig() {

        sysProps.set("service.url", "http://localhost:8080")
                .set("service.api.key", "apiKey")
                .set("service.api.secret", "secret")
                .set("net.connection.timeout", "1000")
                .set("net.read.timeout", "5000")
                .set("local.mode", "off");
        // Environment variables take precedence
        envVars.set("SERVICE_API_SECRET", "a-different-secret")
               .set("NET_CONNECTION_TIMEOUT", "2000")
               .set("NET_READ_TIMEOUT", "5000")
               .set("LOCAL_MODE", "TRUE");

        assertEquals("http://localhost:8080", config.serviceUrl());
        assertEquals("apiKey", config.serviceApiKey());
        assertEquals("a-different-secret", config.serviceApiSecret());
        assertEquals(2_000L, config.netConnectionTimeout());
        assertEquals(5_000L, config.netReadTimeout());
        assertTrue(config.isLocalMode());
    }
}
