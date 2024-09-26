/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;

import static org.riversoforion.lena.config.ConfigurationSources.*;

/**
 * Exposes a set of environment variables and/or system properties with type-safe accessors.
 */
public class ApplicationConfig extends ConfigurationProperties {

    protected ApplicationConfig() {

        // Environment variables take precedence
        super(prioritized(forEnvironment(), forSystemProperties()));
        // For demonstration purposes, allow missing values rather than blowing up
        returnNullForMissing();
    }

    /**
     * Service URL.
     * <ul>
     *     <li>Environment variable: {@code SERVICE_URL}</li>
     *     <li>System property: {@code service.url}</li>
     * </ul>
     */
    public String serviceUrl() {

        return stringVal("service.url");
    }

    /**
     * Service API key.
     * <ul>
     *     <li>Environment variable: {@code SERVICE_API_KEY}</li>
     *     <li>System property: {@code service.api.key}</li>
     * </ul>
     */
    public String serviceApiKey() {

        return stringVal("service.api.key");
    }

    /**
     * Service API secret.
     * <ul>
     *     <li>Environment variable: {@code SERVICE_API_SECRET}</li>
     *     <li>System property: {@code service.api.secret}</li>
     * </ul>
     */
    public String serviceApiSecret() {

        return stringVal("service.api.secret");
    }

    /**
     * Network connection timeout (in milliseconds).
     * <ul>
     *     <li>Environment variable: {@code NET_CONNECTION_TIMEOUT}</li>
     *     <li>System property: {@code net.connection.timeout}</li>
     * </ul>
     */
    public long netConnectionTimeout() {

        return longVal("net.connection.timeout");
    }

    /**
     * Network read timeout (in milliseconds).
     * <ul>
     *     <li>Environment variable: {@code NET_READ_TIMEOUT}</li>
     *     <li>System property: {@code net.read.timeout}</li>
     * </ul>
     */
    public long netReadTimeout() {

        return longVal("net.read.timeout");
    }

    /**
     * Running in "local mode".
     * <ul>
     *     <li>Environment variable: {@code LOCAL_MODE}</li>
     *     <li>System property: {@code local.mode}</li>
     * </ul>
     */
    public boolean isLocalMode() {

        return booleanVal("local.mode");
    }
}
