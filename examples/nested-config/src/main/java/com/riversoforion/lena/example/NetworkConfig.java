/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.Name;
import org.riversoforion.lena.config.Namespace;

import static org.riversoforion.lena.config.ConfigurationSources.*;

public class NetworkConfig extends ConfigurationProperties {

    public NetworkConfig(Namespace ns) {

        super(prioritized(forEnvironment(), forSystemProperties()), ns);
        returnNullForMissing();
    }

    public long readTimeout() {

        return longVal(Name.of("read/timeout"));
    }

    public long connectTimeout() {

        return longVal(Name.of("connect/timeout"));
    }
}
