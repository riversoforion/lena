/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.List;
import java.util.Optional;

public class PrioritizedConfigurationSource extends CompositeConfigurationSource {

    PrioritizedConfigurationSource(List<ConfigurationSource> sources) {

        super(sources);
    }

    @Override
    public Optional<String> getValue(String name) {

        return this.configurationSources()
                   .stream()
                   .map(source -> source.getValue(name))
                   .filter(Optional::isPresent)
                   .findFirst()
                   .orElse(Optional.empty());
    }
}
