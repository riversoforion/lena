/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;

import static org.riversoforion.lena.config.ConfigurationSources.*;

public class ServiceConfig extends ConfigurationProperties {

    static final String PREFIX = "service";

    public ServiceConfig() {

        super(prioritized(forEnvironment(PREFIX), forSystemProperties(PREFIX)));
        addNested("net", new NetworkConfig());
        returnNullForMissing();
    }

    public NetworkConfig net() {

        return nested("net", NetworkConfig.class);
    }

    public String url() {

        return stringVal("url");
    }

    public String apiKey() {

        return stringVal("api.key");
    }

    public String apiSecret() {

        return stringVal("api.secret");
    }
}
