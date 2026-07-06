/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.jvm.JvmOverloads
import kotlin.properties.ReadOnlyProperty

/**
 * Abstract base class for user-defined configuration beans.
 *
 * ## Kotlin usage — property delegates
 * ```kotlin
 * class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
 *     val serviceUrl: String by string("service", "url")
 *     val port: Int        by int("port")
 *     val localMode: Boolean by boolean("local", "mode")
 *     val tag: String?     by optionalString("tag")
 *     val timeout: Long    by long("timeout", default = 5_000L)
 * }
 * ```
 *
 * ## Java usage — protected accessor methods
 * ```java
 * public class AppConfig extends ConfigurationProperties {
 *     public AppConfig(ConfigurationSource source) { super(source); }
 *     public String serviceUrl() { return stringVal(Name.of("service", "url")); }
 *     public int port()          { return intVal(Name.of("port")); }
 * }
 * ```
 *
 * ## Nested configs — explicit composition (no global registry)
 * ```kotlin
 * class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
 *     val network = NetworkConfig(source, namespace.child("network"))
 * }
 * ```
 */
@Suppress("TooManyFunctions") // by design: one typed accessor + delegate per primitive type
public abstract class ConfigurationProperties @JvmOverloads constructor(
    private val source: ConfigurationSource,
    public val namespace: Namespace = Namespace.root(),
) {
    private val converter: ValueConverter = createConverter()

    protected open fun createConverter(): ValueConverter = DefaultValueConverter()

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /** Returns `true` if the property has no source value and no default. */
    public fun isMissing(name: Name): Boolean = source.getValue(namespace, name) == null

    /** Returns `true` if the property has a value in the backing source. */
    public fun isSet(name: Name): Boolean = source.getValue(namespace, name) != null

    // -------------------------------------------------------------------------
    // Raw and typed accessors (primary Java API; also used internally by delegates)
    // -------------------------------------------------------------------------

    /** Raw string lookup — returns `null` if the property is absent. */
    protected fun sourceVal(name: Name): String? = source.getValue(namespace, name)

    /**
     * Resolved string — returns the source value, or throws [MissingConfigurationException].
     * For nullable access use [optionalStringVal].
     */
    protected fun stringVal(name: Name): String =
        sourceVal(name) ?: throw MissingConfigurationException(name)

    /** Resolved nullable string — returns `null` if the property is absent. */
    protected fun optionalStringVal(name: Name): String? = sourceVal(name)

    protected fun booleanVal(name: Name): Boolean = converter.toBoolean(stringVal(name))
    protected fun shortVal(name: Name): Short = converter.toShort(stringVal(name))
    protected fun intVal(name: Name): Int = converter.toInt(stringVal(name))
    protected fun longVal(name: Name): Long = converter.toLong(stringVal(name))
    protected fun floatVal(name: Name): Float = converter.toFloat(stringVal(name))
    protected fun doubleVal(name: Name): Double = converter.toDouble(stringVal(name))

    // -------------------------------------------------------------------------
    // Property delegates — Kotlin API
    //
    // Non-nullable delegates throw MissingConfigurationException on absent values.
    // Nullable (optional*) delegates return null.
    // Overloads with `default` return the given default instead of throwing.
    // -------------------------------------------------------------------------

    protected fun string(vararg segments: String): ReadOnlyProperty<Any?, String> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> stringVal(name) }
    }

    protected fun string(vararg segments: String, default: String): ReadOnlyProperty<Any?, String> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name) ?: default }
    }

    protected fun optionalString(vararg segments: String): ReadOnlyProperty<Any?, String?> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> optionalStringVal(name) }
    }

    protected fun boolean(vararg segments: String): ReadOnlyProperty<Any?, Boolean> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> booleanVal(name) }
    }

    protected fun boolean(vararg segments: String, default: Boolean): ReadOnlyProperty<Any?, Boolean> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toBoolean(it) } ?: default }
    }

    protected fun short(vararg segments: String): ReadOnlyProperty<Any?, Short> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> shortVal(name) }
    }

    protected fun short(vararg segments: String, default: Short): ReadOnlyProperty<Any?, Short> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toShort(it) } ?: default }
    }

    protected fun int(vararg segments: String): ReadOnlyProperty<Any?, Int> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> intVal(name) }
    }

    protected fun int(vararg segments: String, default: Int): ReadOnlyProperty<Any?, Int> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toInt(it) } ?: default }
    }

    protected fun long(vararg segments: String): ReadOnlyProperty<Any?, Long> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> longVal(name) }
    }

    protected fun long(vararg segments: String, default: Long): ReadOnlyProperty<Any?, Long> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toLong(it) } ?: default }
    }

    protected fun float(vararg segments: String): ReadOnlyProperty<Any?, Float> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> floatVal(name) }
    }

    protected fun float(vararg segments: String, default: Float): ReadOnlyProperty<Any?, Float> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toFloat(it) } ?: default }
    }

    protected fun double(vararg segments: String): ReadOnlyProperty<Any?, Double> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> doubleVal(name) }
    }

    protected fun double(vararg segments: String, default: Double): ReadOnlyProperty<Any?, Double> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toDouble(it) } ?: default }
    }
}
