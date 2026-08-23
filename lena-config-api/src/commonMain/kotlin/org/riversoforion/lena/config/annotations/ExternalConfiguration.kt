/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.annotations

/**
 * Marks a [org.riversoforion.lena.config.ConfigurationProperties] subclass for processing
 * by the lena-config-ksp symbol processor (implementation deferred).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExternalConfiguration
