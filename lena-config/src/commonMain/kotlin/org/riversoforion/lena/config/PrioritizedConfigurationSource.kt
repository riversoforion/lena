/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/**
 * A [CompositeConfigurationSource] that returns the first non-null value found across its
 * sources, in declaration order (first = highest priority).
 */
public class PrioritizedConfigurationSource internal constructor(
    sources: List<Pair<ConfigurationSource, Namespace?>>,
) : CompositeConfigurationSource(sources) {

    override fun orderedSources(
        all: List<Pair<ConfigurationSource, Namespace?>>,
    ): List<Pair<ConfigurationSource, Namespace?>> = all
}
