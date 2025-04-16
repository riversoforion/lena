/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.riversoforion.lena.config.Name;
import org.riversoforion.lena.config.NameResolver;
import org.riversoforion.lena.config.Namespace;

import java.util.List;

public class PropertyNameResolver implements NameResolver {

    private static final String SEPARATOR = ".";

    @Override
    public String resolveName(Namespace namespace, Name name) {

        return joinParts(namespace.resolveProperty(name));
    }

    protected String joinParts(List<String> parts) {

        return String.join(SEPARATOR, parts);
    }
}
