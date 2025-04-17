/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.List;
import java.util.SortedMap;
import java.util.stream.Stream;

public class PrioritizedConfigurationSource extends CompositeConfigurationSource {

    PrioritizedConfigurationSource(List<ConfigurationSource> sources) {

        super(sources);
    }

    PrioritizedConfigurationSource(SortedMap<ConfigurationSource, Namespace> sources) {
        super(sources);
    }

    @Override
    protected Stream<NamespacedWrapper> orderedSources(List<NamespacedWrapper> sources) {

        return sources.stream();
    }
}
