/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.Arrays;
import java.util.stream.Stream;

public interface NameResolver {

    String resolveName(Namespace namespace, String name, String... additionalNames);

    static void validateNotEmpty(String name) throws IllegalArgumentException {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Configuration property name cannot be null or blank");
        }
    }

    static String[] allValidNames(String name, String... additionalNames) {

        if (additionalNames == null || additionalNames.length == 0) {
            validateNotEmpty(name);
            return new String[]{ name };
        }
        return Stream.concat(Stream.of(name), Arrays.stream(additionalNames))
                     .peek(NameResolver::validateNotEmpty)
                     .toArray(String[]::new);
    }
}
