/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.riversoforion.lena.config.Name
import org.riversoforion.lena.config.NameResolver
import org.riversoforion.lena.config.Namespace

private val SANITIZER = Regex("[^a-zA-Z0-9]+")
private val WORD_FINDER = Regex("\\S+")
private const val SEPARATOR = "_"

/**
 * Converts a [Namespace] + [Name] pair into an environment-variable-style key:
 * uppercase, underscore-separated, with non-alphanumeric characters removed.
 *
 * Example: `Namespace.of("app")` + `Name.of("service", "url")` → `APP_SERVICE_URL`.
 */
public open class EnvironmentNameResolver : NameResolver {

    override fun resolveName(namespace: Namespace, name: Name): String {
        val parts = namespace.resolveProperty(name)
        // Each part contributes exactly one token even when it sanitizes to "" (a fully
        // symbolic segment, e.g. "()$%") — that positional emptiness is what produces the
        // leading/trailing/doubled underscores callers rely on (e.g. "()$%" -> "_").
        return parts.map { sanitize(it) }
            .flatMap { split(it) }
            .joinToString(SEPARATOR) { it.uppercase() }
    }

    protected open fun sanitize(part: String): String =
        SANITIZER.replace(part, " ").trim()

    protected open fun split(part: String): List<String> =
        if (part.isEmpty()) listOf(part)
        else WORD_FINDER.findAll(part).map { it.value }.toList()
}
