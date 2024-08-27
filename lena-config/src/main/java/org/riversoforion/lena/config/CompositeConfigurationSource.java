/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class CompositeConfigurationSource implements ConfigurationSource {

    private final List<ConfigurationSource> configurationSources = new ArrayList<>();

    protected CompositeConfigurationSource(ConfigurationSource... configurationSources) {

        this.configurationSources.addAll(Arrays.asList(configurationSources));
    }

    protected CompositeConfigurationSource(Collection<ConfigurationSource> configurationSources) {

        this.configurationSources.addAll(configurationSources);
    }

    protected ConfigurationSource configurationSource(int index) {
        return configurationSources.get(index);
    }

    protected List<ConfigurationSource> configurationSources() {

        return Collections.unmodifiableList(configurationSources);
    }
}
