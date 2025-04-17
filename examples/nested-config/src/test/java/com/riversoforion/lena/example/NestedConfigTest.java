/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
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
class NestedConfigTest {

    @SystemStub
    private EnvironmentVariables envVars;
    @SystemStub
    private SystemProperties sysProps;

    @Test
    void configViaEnvironmentVariables() {

        envVars.set("ENVVARS_SERVICE_URL", "http://localhost:8080")
               .set("ENVVARS_SERVICE_API_KEY", "apiKey")
               .set("ENVVARS_SERVICE_API_SECRET", "secret")
               .set("ENVVARS_NET_CONNECT_TIMEOUT", "1000")
               .set("ENVVARS_NET_READ_TIMEOUT", "5000")
               .set("ENVVARS_LOCAL_MODE", "Y");
        ApplicationConfig config = new ApplicationConfig(Namespace.of("envvars"));

        assertEquals("http://localhost:8080", config.service().url());
        assertEquals("apiKey", config.service().apiKey());
        assertEquals("secret", config.service().apiSecret());
        assertEquals(1_000L, config.net().connectTimeout());
        assertEquals(5_000L, config.net().readTimeout());
        assertTrue(config.isLocalMode());
    }

    @Test
    void configViaSystemProperties() {

        sysProps.set("sysprops.service.url", "http://localhost:8080")
                .set("sysprops.service.api.key", "apiKey")
                .set("sysprops.service.api.secret", "secret")
                .set("sysprops.net.connect.timeout", "1000")
                .set("sysprops.net.read.timeout", "5000")
                .set("sysprops.local.mode", "on");
        ApplicationConfig config = new ApplicationConfig(Namespace.of("sysprops"));

        assertEquals("http://localhost:8080", config.service().url());
        assertEquals("apiKey", config.service().apiKey());
        assertEquals("secret", config.service().apiSecret());
        assertEquals(1_000L, config.net().connectTimeout());
        assertEquals(5_000L, config.net().readTimeout());
        assertTrue(config.isLocalMode());
    }

    @Test
    void mixedConfig() {

        sysProps.set("service.url", "http://localhost:8080")
                .set("service.api.key", "apiKey")
                .set("service.api.secret", "secret")
                .set("net.connect.timeout", "1000")
                .set("net.read.timeout", "5000")
                .set("local.mode", "off");
        // Environment variables take precedence
        envVars.set("SERVICE_API_SECRET", "a-different-secret")
               .set("NET_CONNECT_TIMEOUT", "2000")
               .set("NET_READ_TIMEOUT", "5000")
               .set("LOCAL_MODE", "TRUE");
        ApplicationConfig config = new ApplicationConfig(Namespace.root());

        assertEquals("http://localhost:8080", config.service().url());
        assertEquals("apiKey", config.service().apiKey());
        assertEquals("a-different-secret", config.service().apiSecret());
        assertEquals(2_000L, config.net().connectTimeout());
        assertEquals(5_000L, config.net().readTimeout());
        assertTrue(config.isLocalMode());
    }
}
