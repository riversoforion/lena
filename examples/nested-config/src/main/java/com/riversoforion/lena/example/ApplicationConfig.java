/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;

import static org.riversoforion.lena.config.ConfigurationSources.*;

public class ApplicationConfig extends ConfigurationProperties {

    public ApplicationConfig() {

        super(prioritized(forEnvironment(), forSystemProperties()));
        returnNullForMissing();
        withNested(ServiceConfig.PREFIX, new ServiceConfig());
        withNested(NetworkConfig.PREFIX, new NetworkConfig());
    }

    public ServiceConfig service() {

        return nested(ServiceConfig.PREFIX, ServiceConfig.class);
    }

    public NetworkConfig net() {

        return nested(NetworkConfig.PREFIX, NetworkConfig.class);
    }

    public boolean isLocalMode() {

        return booleanVal("local");
    }
}
