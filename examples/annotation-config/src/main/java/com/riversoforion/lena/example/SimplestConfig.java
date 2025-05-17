/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package com.riversoforion.lena.example;

import org.riversoforion.lena.config.annotations.ExternalConfiguration;

@ExternalConfiguration
public interface SimplestConfig {

    String serviceUrl();

    String serviceApiKey();

    String serviceApiSecret();
}
