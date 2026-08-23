/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import org.riversoforion.lena.config.annotations.ConfigurationProperty
import org.riversoforion.lena.config.annotations.ExternalConfiguration

@ExternalConfiguration
public interface KspTestConfig {

    @ConfigurationProperty("service/url")
    public fun serviceUrl(): String

    @ConfigurationProperty("port")
    public fun port(): Int

    @ConfigurationProperty("timeout")
    public fun timeout(): Long

    @ConfigurationProperty("active")
    public fun isActive(): Boolean

    @ConfigurationProperty("optional")
    public fun optionalValue(): String?
}
