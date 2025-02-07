/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.riversoforion.lena.config.resolvers.EnvironmentNameResolver;
import org.riversoforion.lena.config.resolvers.EnvironmentValueResolver;
import org.riversoforion.lena.config.resolvers.PropertyNameResolver;
import org.riversoforion.lena.config.resolvers.SystemPropertiesValueResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

public class ConfigurationSources {

    public static ConfigurationSource forEnvironment() {

        NameResolver names = new EnvironmentNameResolver();
        ValueResolver values = new EnvironmentValueResolver();
        return new SimpleConfigurationSource(names, values);
    }

    public static ConfigurationSource forSystemProperties() {

        NameResolver names = new PropertyNameResolver();
        ValueResolver values = new SystemPropertiesValueResolver();
        return new SimpleConfigurationSource(names, values);
    }

    public static ConfigurationSource custom(NameResolver names, ValueResolver values) {

        Objects.requireNonNull(names);
        Objects.requireNonNull(values);
        return new SimpleConfigurationSource(names, values);
    }

    public static ConfigurationSource custom(NameResolver names, ValueResolver values, Namespace namespace) {

        Objects.requireNonNull(names);
        Objects.requireNonNull(values);
        Objects.requireNonNull(namespace);
        return CompositeConfigurationSource.namespaced(custom(names, values), namespace);
    }

    public static ConfigurationSource prioritized(ConfigurationSource first, ConfigurationSource second, ConfigurationSource... other) {

        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        List<ConfigurationSource> sources = new ArrayList<>();
        sources.add(first);
        sources.add(second);
        if (other != null) {
            sources.addAll(Arrays.asList(other));
        }
        return new PrioritizedConfigurationSource(sources);
    }

    public static ConfigurationSource prioritized(SortedMap<ConfigurationSource, Namespace> sources) {

        Objects.requireNonNull(sources);
        if (sources.size() < 2) {
            throw new IllegalArgumentException("There must be at least two prioritized configuration sources");
        }

        return new PrioritizedConfigurationSource(sources);
    }
}
