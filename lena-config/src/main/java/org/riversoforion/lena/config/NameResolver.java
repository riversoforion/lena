/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

public interface NameResolver {

    String resolveName(Namespace namespace, String name);

    static void validateNotEmpty(String name) throws IllegalArgumentException {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Configuration property name cannot be null or blank");
        }
    }
}
