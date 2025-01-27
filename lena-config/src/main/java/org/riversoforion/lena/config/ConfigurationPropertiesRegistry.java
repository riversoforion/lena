/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ConfigurationPropertiesRegistry {

    private static final ConfigurationPropertiesRegistry INSTANCE = new ConfigurationPropertiesRegistry();

    private final Map<Namespace, ConfigurationProperties> registry = new ConcurrentHashMap<>();

    private ConfigurationPropertiesRegistry() {
        // Singleton
    }

    static ConfigurationPropertiesRegistry instance() {

        return INSTANCE;
    }

    ConfigurationProperties get(Namespace namespace) {

        if (registry.containsKey(namespace)) {
            return registry.get(namespace);
        }
        throw new IllegalArgumentException("Namespace " + namespace + " not registered");
    }

    void register(ConfigurationProperties properties) {

        Namespace ns = properties.namespace();
        if (registry.containsKey(ns)) {
            throw new IllegalArgumentException("Namespace " + ns + " already registered");
        }
        registry.put(ns, properties);
    }
}
