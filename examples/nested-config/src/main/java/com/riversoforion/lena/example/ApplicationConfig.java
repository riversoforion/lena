/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.Name;
import org.riversoforion.lena.config.Namespace;

import static org.riversoforion.lena.config.ConfigurationSources.*;

public class ApplicationConfig extends ConfigurationProperties {

    private ServiceConfig service;
    private NetworkConfig net;

    public ApplicationConfig() {

        this(Namespace.root());
    }

    ApplicationConfig(Namespace testingNs) {

        super(prioritized(forEnvironment(), forSystemProperties()), testingNs);
        returnNullForMissing();
    }

    @Override
    protected void createChildren() {

        this.service = new ServiceConfig(namespace().child("service"));
        this.net = new NetworkConfig(namespace().child("net"));
    }

    public ServiceConfig service() {

        return service;
    }

    public NetworkConfig net() {

        return net;
    }

    public boolean isLocalMode() {

        return booleanVal(Name.of("local", "mode"));
    }
}
