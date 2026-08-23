/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example

import org.riversoforion.lena.config.annotations.ConfigurationProperty
import org.riversoforion.lena.config.annotations.ExternalConfiguration

/**
 * Exposes a set of environment variables and/or system properties with type-safe accessors.
 */
@ExternalConfiguration
interface ApplicationConfig {

    /**
     * service/url
     * - Environment Variable: SERVICE_URL
     * - System Property: service.url
     */
    @ConfigurationProperty("service/url")
    fun serviceUrl(): String

    /**
     * service/api/key
     * - Environment Variable: SERVICE_API_KEY
     * - System Property: service.api.key
     */
    @ConfigurationProperty("service/api/key")
    fun serviceApiKey(): String

    /**
     * service/api/secret
     * - Environment Variable: SERVICE_API_SECRET
     * - System Property: service.api.secret
     */
    @ConfigurationProperty("service/api/secret")
    fun serviceApiSecret(): String

    /**
     * net/connection/timeout
     * - Environment Variable: NET_CONNECTION_TIMEOUT
     * - System Property: net.connection.timeout
     */
    @ConfigurationProperty("net/connection/timeout")
    fun netConnectionTimeout(): Long

    /**
     * net/read/timeout
     * - Environment Variable: NET_READ_TIMEOUT
     * - System Property: net.read.timeout
     */
    @ConfigurationProperty("net/read/timeout")
    fun netReadTimeout(): Long

    /**
     * local/mode
     * - Environment Variable: LOCAL_MODE
     * - System Property: local.mode
     */
    @ConfigurationProperty("local/mode")
    fun isLocalMode(): Boolean
}
