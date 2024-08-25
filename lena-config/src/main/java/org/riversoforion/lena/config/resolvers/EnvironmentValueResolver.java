/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.riversoforion.lena.config.ValueResolver;

import java.util.Optional;

public class EnvironmentValueResolver implements ValueResolver {

    @Override
    public Optional<String> resolveValue(String name) {

        return Optional.ofNullable(System.getenv(name));
    }
}
