/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.jvm.JvmOverloads
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    /** Exposed for subclasses that need [ValueConverter.register] — see [converted] and [custom]. */
    protected val converter: ValueConverter = createConverter()

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
     * For nullable access use [optionalStringVal]; for a fallback value instead of an exception,
     * use the [stringVal] overload that takes a `default`.
     */
    protected fun stringVal(name: Name): String =
        sourceVal(name) ?: throw MissingConfigurationException(name)

    /** Resolved string — returns [default] instead of throwing if the property is absent. */
    protected fun stringVal(name: Name, default: String): String = sourceVal(name) ?: default

    /** Resolved nullable string — returns `null` if the property is absent. */
    protected fun optionalStringVal(name: Name): String? = sourceVal(name)

    protected fun booleanVal(name: Name): Boolean = converter.toBoolean(stringVal(name))
    protected fun booleanVal(name: Name, default: Boolean): Boolean =
        sourceVal(name)?.let { converter.toBoolean(it) } ?: default

    protected fun shortVal(name: Name): Short = converter.toShort(stringVal(name))
    protected fun shortVal(name: Name, default: Short): Short =
        sourceVal(name)?.let { converter.toShort(it) } ?: default

    protected fun intVal(name: Name): Int = converter.toInt(stringVal(name))
    protected fun intVal(name: Name, default: Int): Int =
        sourceVal(name)?.let { converter.toInt(it) } ?: default

    protected fun longVal(name: Name): Long = converter.toLong(stringVal(name))
    protected fun longVal(name: Name, default: Long): Long =
        sourceVal(name)?.let { converter.toLong(it) } ?: default

    protected fun floatVal(name: Name): Float = converter.toFloat(stringVal(name))
    protected fun floatVal(name: Name, default: Float): Float =
        sourceVal(name)?.let { converter.toFloat(it) } ?: default

    protected fun doubleVal(name: Name): Double = converter.toDouble(stringVal(name))
    protected fun doubleVal(name: Name, default: Double): Double =
        sourceVal(name)?.let { converter.toDouble(it) } ?: default

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

    // Lazy defaults for forward references and deferred evaluation
    protected fun long(vararg segments: String, default: () -> Long): ReadOnlyProperty<Any?, Long> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toLong(it) } ?: default() }
    }

    protected fun int(vararg segments: String, default: () -> Int): ReadOnlyProperty<Any?, Int> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toInt(it) } ?: default() }
    }

    protected fun string(vararg segments: String, default: () -> String): ReadOnlyProperty<Any?, String> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name) ?: default() }
    }

    protected fun boolean(vararg segments: String, default: () -> Boolean): ReadOnlyProperty<Any?, Boolean> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toBoolean(it) } ?: default() }
    }

    // -------------------------------------------------------------------------
    // Complex type accessors (Java API)
    // -------------------------------------------------------------------------

    protected fun <T : Any> enumVal(name: Name, klass: KClass<T>): T =
        converter.toEnum(stringVal(name), klass)

    protected fun <T : Any> enumVal(
        name: Name,
        klass: KClass<T>,
        default: T,
    ): T = sourceVal(name)?.let { converter.toEnum(it, klass) }
        ?: default

    protected fun durationVal(name: Name): Duration = converter.toDuration(stringVal(name))

    protected fun durationVal(name: Name, default: Duration): Duration =
        sourceVal(name)?.let { converter.toDuration(it) } ?: default

    protected fun intListVal(name: Name): List<Int> = converter.toIntList(sourceVal(name))

    protected fun stringListVal(name: Name): List<String> = converter.toStringList(sourceVal(name))

    protected fun stringSetVal(name: Name): Set<String> = converter.toStringSet(sourceVal(name))

    protected fun stringMapVal(name: Name): Map<String, String> = converter.toStringMap(sourceVal(name))

    @OptIn(ExperimentalUuidApi::class)
    protected fun uuidVal(name: Name): Uuid = converter.toUuid(stringVal(name))

    @OptIn(ExperimentalUuidApi::class)
    protected fun uuidVal(name: Name, default: Uuid): Uuid =
        sourceVal(name)?.let { converter.toUuid(it) } ?: default

    protected fun durationListVal(name: Name): List<Duration> =
        converter.toStringList(sourceVal(name)).map { converter.toDuration(it) }

    protected fun durationListVal(name: Name, default: List<Duration>): List<Duration> =
        sourceVal(name)?.let { s -> converter.toStringList(s).map { converter.toDuration(it) } } ?: default

    protected fun <T : Any> convertVal(name: Name, fn: (String) -> T): T =
        converter.convert(stringVal(name), fn)

    protected fun <T : Any> convertVal(name: Name, default: T, fn: (String) -> T): T =
        sourceVal(name)?.let { converter.convert(it, fn) } ?: default

    // -------------------------------------------------------------------------
    // Complex type delegates (Kotlin API)
    // -------------------------------------------------------------------------

    protected inline fun <reified T : Enum<T>> enum(vararg segments: String): ReadOnlyProperty<Any?, T> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> enumVal(name, T::class) }
    }

    protected inline fun <reified T : Enum<T>> enum(vararg segments: String, default: T): ReadOnlyProperty<Any?, T> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> enumVal(name, T::class, default) }
    }

    protected fun duration(vararg segments: String): ReadOnlyProperty<Any?, Duration> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> durationVal(name) }
    }

    protected fun duration(vararg segments: String, default: Duration): ReadOnlyProperty<Any?, Duration> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> durationVal(name, default) }
    }

    protected fun durationList(vararg segments: String): ReadOnlyProperty<Any?, List<Duration>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> durationListVal(name) }
    }

    protected fun durationList(
        vararg segments: String,
        default: List<Duration>,
    ): ReadOnlyProperty<Any?, List<Duration>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> durationListVal(name, default) }
    }

    @OptIn(ExperimentalUuidApi::class)
    protected fun uuid(vararg segments: String): ReadOnlyProperty<Any?, Uuid> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> uuidVal(name) }
    }

    @OptIn(ExperimentalUuidApi::class)
    protected fun uuid(vararg segments: String, default: Uuid): ReadOnlyProperty<Any?, Uuid> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> uuidVal(name, default) }
    }

    protected fun intList(vararg segments: String): ReadOnlyProperty<Any?, List<Int>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> intListVal(name) }
    }

    protected fun intList(
        vararg segments: String,
        default: List<Int>,
    ): ReadOnlyProperty<Any?, List<Int>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toIntList(it) } ?: default }
    }

    protected fun stringList(vararg segments: String): ReadOnlyProperty<Any?, List<String>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> stringListVal(name) }
    }

    protected fun stringList(
        vararg segments: String,
        default: List<String>,
    ): ReadOnlyProperty<Any?, List<String>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toStringList(it) } ?: default }
    }

    protected fun stringSet(vararg segments: String): ReadOnlyProperty<Any?, Set<String>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> stringSetVal(name) }
    }

    protected fun stringSet(
        vararg segments: String,
        default: Set<String>,
    ): ReadOnlyProperty<Any?, Set<String>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toStringSet(it) } ?: default }
    }

    protected fun stringMap(vararg segments: String): ReadOnlyProperty<Any?, Map<String, String>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> stringMapVal(name) }
    }

    protected fun stringMap(
        vararg segments: String,
        default: Map<String, String>,
    ): ReadOnlyProperty<Any?, Map<String, String>> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toStringMap(it) } ?: default }
    }

    protected fun <T : Any> converted(
        vararg segments: String,
        fn: (String) -> T,
    ): ReadOnlyProperty<Any?, T> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> convertVal(name, fn) }
    }

    protected fun <T : Any> converted(
        vararg segments: String,
        default: T,
        fn: (String) -> T,
    ): ReadOnlyProperty<Any?, T> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> convertVal(name, default, fn) }
    }

    /**
     * Reads a type registered via [ValueConverter.register] — e.g. in an `init` block:
     * `converter.register<CustomType> { CustomType.parse(it) }`, then
     * `val custom: CustomType by custom("key")`.
     */
    protected inline fun <reified T : Any> custom(vararg segments: String): ReadOnlyProperty<Any?, T> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> converter.toRegistered<T>(stringVal(name)) }
    }

    protected inline fun <reified T : Any> custom(vararg segments: String, default: T): ReadOnlyProperty<Any?, T> {
        val name = Name.of(*segments)
        return ReadOnlyProperty { _, _ -> sourceVal(name)?.let { converter.toRegistered<T>(it) } ?: default }
    }
}
