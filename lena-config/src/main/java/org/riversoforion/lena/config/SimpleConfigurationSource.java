/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.Optional;

public final class SimpleConfigurationSource implements ConfigurationSource {

    private final NameResolver names;
    private final ValueResolver values;

    SimpleConfigurationSource(NameResolver names, ValueResolver values) {

        this.names = names;
        this.values = values;
    }

    @Override
    public Optional<String> getValue(Namespace namespace, String name) {

        String resolvedName = this.names.resolveName(namespace, name);
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
}
