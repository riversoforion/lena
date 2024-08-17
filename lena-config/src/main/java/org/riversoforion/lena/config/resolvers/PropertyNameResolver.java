/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

public class PropertyNameResolver extends PrefixedNameResolver {

    private static final String SEPARATOR = ".";

    public PropertyNameResolver(String prefix) {

        super(prefix);
    }

    @Override
    protected String separator() {

        return SEPARATOR;
    }

    @Override
    protected String normalize(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw;
    }
}
