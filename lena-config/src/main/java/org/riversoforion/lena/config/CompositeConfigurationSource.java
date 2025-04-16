/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CompositeConfigurationSource implements ConfigurationSource {

    private final List<NamespacedWrapper> configurationSources;

    protected CompositeConfigurationSource(ConfigurationSource... configurationSources) {

        this.configurationSources = Arrays.stream(configurationSources).map(CompositeConfigurationSource::namespaced).collect(Collectors.toList());
    }

    protected CompositeConfigurationSource(Collection<ConfigurationSource> configurationSources) {

        this.configurationSources = configurationSources.stream().map(CompositeConfigurationSource::namespaced).collect(Collectors.toList());
    }

    protected CompositeConfigurationSource(Map<ConfigurationSource, Namespace> configurationSources) {

        this.configurationSources = configurationSources.entrySet().stream().map(CompositeConfigurationSource::namespaced).collect(Collectors.toList());
    }

    public static NamespacedWrapper namespaced(ConfigurationSource configurationSource, Namespace namespace) {

        if (configurationSource instanceof NamespacedWrapper namespaced) {
            return namespaced;
        }
        return new NamespacedWrapper(namespace, configurationSource);
    }

    public static NamespacedWrapper namespaced(ConfigurationSource configurationSource) {

        return namespaced(configurationSource, null);
    }

    public static NamespacedWrapper namespaced(Map.Entry<ConfigurationSource, Namespace> entry) {

        return namespaced(entry.getKey(), entry.getValue());
    }

    @Override
    public final Optional<String> getValue(Namespace namespace, Name name) {

        return orderedSources(configurationSources).map(wrapper -> wrapper.getValue(namespace, name))
                                                   .filter(Optional::isPresent)
                                                   .map(Optional::get)
                                                   .findFirst();
    }

    protected abstract Stream<NamespacedWrapper> orderedSources(List<NamespacedWrapper> sources);

    public static class NamespacedWrapper implements ConfigurationSource {

        private final Optional<Namespace> namespace;
        private final ConfigurationSource configurationSource;

        private NamespacedWrapper(Namespace namespace, ConfigurationSource configurationSource) {

            this.namespace = Optional.ofNullable(namespace);
            this.configurationSource = Objects.requireNonNull(configurationSource);
        }

        public Optional<Namespace> namespace() {

            return namespace;
        }

        public ConfigurationSource configurationSource() {

            return configurationSource;
        }

        @Override
        public Optional<String> getValue(Namespace namespace, Name name) {

            return configurationSource.getValue(this.namespace.orElse(namespace), name);
        }
    }
}
