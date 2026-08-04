/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.reflect.KClass

/**
 * Metadata about a configuration property discovered by the processor.
 */
public data class PropertyMetadata(
    val name: Name,
    val type: KClass<*>,
    val defaultValue: Any? = null
)

/**
 * Registry of properties for [ConfigurationProperties] classes.
 * Primarily used by generated code to expose metadata for introspection.
 */
public object PropertyRegistry {
    private val properties = mutableMapOf<KClass<*>, List<PropertyMetadata>>()

    /** Registers properties for the given configuration properties class. */
    public fun register(klass: KClass<*>, metadata: List<PropertyMetadata>) {
        properties[klass] = metadata
    }

    /** Returns the metadata for all properties of the given configuration properties class. */
    public fun getMetadata(klass: KClass<*>): List<PropertyMetadata>? =
        properties[klass]
}
