/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.annotations

/**
 * Specifies the configuration property name for a method in an [ExternalConfiguration] interface.
 *
 * The [value] is the property name, which can be a single segment (e.g., "port") or multiple
 * segments separated by the delimiter (e.g., "service/url").
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class ConfigurationProperty(public val value: String)
