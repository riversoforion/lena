/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.ConfigurationSource;
import org.riversoforion.lena.config.ConfigurationSources;
import org.riversoforion.lena.config.Name;
import org.riversoforion.lena.config.Namespace;

/**
 * Exposes a set of environment variables and/or system properties with type-safe accessors,
 * including nested sub-configurations composed explicitly (no global registry).
 */
public class ApplicationConfig extends ConfigurationProperties {

    private final ServiceConfig service;
    private final NetworkConfig net;

    public ApplicationConfig() {
        this(Namespace.root(), ConfigurationSources.prioritized(
            ConfigurationSources.forEnvironment(),
            ConfigurationSources.forSystemProperties()
        ));
    }

    public ApplicationConfig(Namespace ns) {
        this(ns, ConfigurationSources.prioritized(
            ConfigurationSources.forEnvironment(),
            ConfigurationSources.forSystemProperties()
        ));
    }

    public ApplicationConfig(Namespace ns, ConfigurationSource source) {
        super(source, ns);
        this.service = new ServiceConfig(source, getNamespace().child("service"));
        this.net = new NetworkConfig(source, getNamespace().child("net"));
    }

    public ServiceConfig getService() {
        return service;
    }

    public NetworkConfig getNet() {
        return net;
    }

    public boolean isLocalMode() {
        return booleanVal(Name.of("local", "mode"));
    }
}
