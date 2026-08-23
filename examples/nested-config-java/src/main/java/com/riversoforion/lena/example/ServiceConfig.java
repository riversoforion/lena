/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.ConfigurationSource;
import org.riversoforion.lena.config.Name;
import org.riversoforion.lena.config.Namespace;

public class ServiceConfig extends ConfigurationProperties {

    private final NetworkConfig net;

    public ServiceConfig(ConfigurationSource source, Namespace ns) {
        super(source, ns);
        this.net = new NetworkConfig(source, getNamespace().child("net"));
    }

    public NetworkConfig getNet() {
        return net;
    }

    public String getUrl() {
        return stringVal(Name.of("url"));
    }

    public String getApiKey() {
        return stringVal(Name.of("api", "key"));
    }

    public String getApiSecret() {
        return stringVal(Name.of("api", "secret"));
    }
}
