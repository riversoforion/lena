/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;

import static org.riversoforion.lena.config.ConfigurationSources.*;

public class NetworkConfig extends ConfigurationProperties {

    static final String PREFIX = "net";

    public NetworkConfig() {

        super(prioritized(forEnvironment(PREFIX), forSystemProperties(PREFIX)));
        returnNullForMissing();
    }

    public long readTimeout() {

        return longVal("readTimeout");
    }

    public long connectTimeout() {

        return longVal("connectTimeout");
    }
}
