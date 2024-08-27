/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
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

public class ConfigurationSources {

    public static ConfigurationSource forEnvironment() {

        return forEnvironment(null);
    }

    public static ConfigurationSource forEnvironment(String prefix) {

        NameResolver names = new EnvironmentNameResolver(prefix);
        ValueResolver values = new EnvironmentValueResolver();
        return new SimpleConfigurationSource(names, values);
    }

    public static ConfigurationSource forSystemProperties() {

        return forSystemProperties(null);
    }

    public static ConfigurationSource forSystemProperties(String prefix) {

        NameResolver names = new PropertyNameResolver(prefix);
        ValueResolver values = new SystemPropertiesValueResolver();
        return new SimpleConfigurationSource(names, values);
    }

    public static ConfigurationSource custom(NameResolver names, ValueResolver values) {

        Objects.requireNonNull(names);
        Objects.requireNonNull(values);
        return new SimpleConfigurationSource(names, values);
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
}
