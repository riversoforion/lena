/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.riversoforion.lena.config.resolvers.EnvironmentNameResolver;
import org.riversoforion.lena.config.resolvers.EnvironmentValueResolver;
import org.riversoforion.lena.config.resolvers.PropertyNameResolver;
import org.riversoforion.lena.config.resolvers.SystemPropertiesValueResolver;

import java.util.Optional;

public final class ConfigurationSource {

    private final NameResolver names;
    private final ValueResolver values;

    ConfigurationSource(NameResolver names, ValueResolver values) {

        this.names = names;
        this.values = values;
    }

    public Optional<String> getValue(String name) {

        String resolvedName = this.names.resolveName(name);
        return this.values.resolveValue(resolvedName);
    }

    // For testing
    NameResolver getNameResolver() {

        return this.names;
    }

    // For testing
    ValueResolver getValueResolver() {

        return this.values;
    }

    public static ConfigurationSource forEnvironment() {

        return forEnvironment(null);
    }

    public static ConfigurationSource forEnvironment(String prefix) {

        NameResolver names = new EnvironmentNameResolver(prefix);
        ValueResolver values = new EnvironmentValueResolver();
        return new ConfigurationSource(names, values);
    }

    public static ConfigurationSource forSystemProperties() {

        return forSystemProperties(null);
    }

    public static ConfigurationSource forSystemProperties(String prefix) {

        NameResolver names = new PropertyNameResolver(prefix);
        ValueResolver values = new SystemPropertiesValueResolver();
        return new ConfigurationSource(names, values);
    }

    public static ConfigurationSource custom(NameResolver names, ValueResolver values) {

        return new ConfigurationSource(names, values);
    }
}
