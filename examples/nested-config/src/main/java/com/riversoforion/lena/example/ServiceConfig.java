/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.Name;
import org.riversoforion.lena.config.Namespace;

import static org.riversoforion.lena.config.ConfigurationSources.*;

public class ServiceConfig extends ConfigurationProperties {

    private NetworkConfig net;

    public ServiceConfig(Namespace ns) {

        super(prioritized(forEnvironment(), forSystemProperties()), ns);
        returnNullForMissing();
    }

    @Override
    protected void createChildren() {

        this.net = new NetworkConfig(namespace().child("net"));
    }

    public NetworkConfig net() {

        return net;
    }

    public String url() {

        return stringVal(Name.of("url"));
    }

    public String apiKey() {

        return stringVal(Name.of("api/key"));
    }

    public String apiSecret() {

        return stringVal(Name.of("api/secret"));
    }
}
