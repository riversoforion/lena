/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.internal;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.Namespace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationPropertiesRegistry {

    private static final ConfigurationPropertiesRegistry INSTANCE = new ConfigurationPropertiesRegistry();

    private final Map<Namespace, ConfigurationProperties> registry = new ConcurrentHashMap<>();

    private ConfigurationPropertiesRegistry() {
        // Singleton
    }

    public static ConfigurationPropertiesRegistry instance() {

        return INSTANCE;
    }

    public ConfigurationProperties get(Namespace namespace) {

        if (registry.containsKey(namespace)) {
            return registry.get(namespace);
        }
        throw new IllegalArgumentException("Namespace " + namespace + " not registered");
    }

    public void register(ConfigurationProperties properties) {

        Namespace ns = properties.namespace();
        if (registry.containsKey(ns)) {
            throw new IllegalArgumentException("Namespace " + ns + " already registered");
        }
        registry.put(ns, properties);
    }
}
