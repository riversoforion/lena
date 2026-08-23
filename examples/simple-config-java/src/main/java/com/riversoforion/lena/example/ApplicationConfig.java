/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.annotations.ConfigurationProperty;
import org.riversoforion.lena.config.annotations.ExternalConfiguration;

/**
 * Exposes a set of environment variables and/or system properties with type-safe accessors.
 *
 * <p>Values that are absent from both the environment and system properties cause the accessor
 * to throw {@link org.riversoforion.lena.config.MissingConfigurationException}.
 */
@ExternalConfiguration
public interface ApplicationConfig {

    /**
     * service/url
     * <ul>
     * <li>Environment Variable: <code>SERVICE_URL</code></li>
     * <li>System Property: <code>service.url</code></li>
     * </ul>
     */
    @ConfigurationProperty("service/url")
    String serviceUrl();

    /**
     * service/api/key
     * <ul>
     * <li>Environment Variable: <code>SERVICE_API_KEY</code></li>
     * <li>System Property: <code>service.api.key</code></li>
     * </ul>
     */
    @ConfigurationProperty("service/api/key")
    String serviceApiKey();

    /**
     * service/api/secret
     * <ul>
     * <li>Environment Variable: <code>SERVICE_API_SECRET</code></li>
     * <li>System Property: <code>service.api.secret</code></li>
     * </ul>
     */
    @ConfigurationProperty("service/api/secret")
    String serviceApiSecret();

    /**
     * net/connection/timeout
     * <ul>
     * <li>Environment Variable: <code>NET_CONNECTION_TIMEOUT</code></li>
     * <li>System Property: <code>net.connection.timeout</code></li>
     * </ul>
     */
    @ConfigurationProperty("net/connection/timeout")
    long netConnectionTimeout();

    /**
     * net/read/timeout
     * <ul>
     * <li>Environment Variable: <code>NET_READ_TIMEOUT</code></li>
     * <li>System Property: <code>net.read.timeout</code></li>
     * </ul>
     */
    @ConfigurationProperty("net/read/timeout")
    long netReadTimeout();

    /**
     * local/mode
     * <ul>
     * <li>Environment Variable: <code>LOCAL_MODE</code></li>
     * <li>System Property: <code>local.mode</code></li>
     * </ul>
     */
    @ConfigurationProperty("local/mode")
    boolean isLocalMode();
}
