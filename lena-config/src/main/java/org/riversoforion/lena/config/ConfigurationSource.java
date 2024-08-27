/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.Optional;

public interface ConfigurationSource {

    Optional<String> getValue(String name);
}
