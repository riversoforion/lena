/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.ConfigurationSource;
import org.riversoforion.lena.config.Name;
import org.riversoforion.lena.config.Namespace;

public class NetworkConfig extends ConfigurationProperties {

    public NetworkConfig(ConfigurationSource source, Namespace ns) {
        super(source, ns);
    }

    public long getReadTimeout() {
        return longVal(Name.of("read", "timeout"), 5000L);
    }

    public long getConnectionTimeout() {
        return longVal(Name.of("connection", "timeout"), 5000L);
    }
}
