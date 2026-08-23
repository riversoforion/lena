/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/**
 * A [ConfigurationSource] that queries multiple delegate sources. Subclasses control ordering
 * via [orderedSources].
 *
 * Each entry in [sources] may carry an optional [Namespace] that is prepended to lookup
 * namespaces before delegating (used for scoped sub-configurations).
 */
public abstract class CompositeConfigurationSource protected constructor(
    protected val sources: List<Pair<ConfigurationSource, Namespace?>>,
) : ConfigurationSource {

    public companion object {
        public fun namespaced(source: ConfigurationSource, namespace: Namespace): ConfigurationSource =
            object : CompositeConfigurationSource(listOf(source to namespace)) {
                override fun orderedSources(
                    all: List<Pair<ConfigurationSource, Namespace?>>
                ): List<Pair<ConfigurationSource, Namespace?>> = all
            }

        public fun namespaced(source: ConfigurationSource): ConfigurationSource =
            namespaced(source, Namespace.root())
    }

    override fun getValue(namespace: Namespace, name: Name): String? =
        orderedSources(sources).firstNotNullOfOrNull { (source, ns) ->
            val effectiveNamespace = if (ns != null && ns != Namespace.root()) {
                Namespace.parse(ns.name() + namespace.name())
            } else {
                namespace
            }
            source.getValue(effectiveNamespace, name)
        }

    protected abstract fun orderedSources(
        all: List<Pair<ConfigurationSource, Namespace?>>,
    ): List<Pair<ConfigurationSource, Namespace?>>
}
