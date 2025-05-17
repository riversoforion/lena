/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.annotations;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.ConfigurationSource;
import org.riversoforion.lena.config.NameResolver;
import org.riversoforion.lena.config.SimpleConfigurationSource;
import org.riversoforion.lena.config.ValueResolver;

/**
 * Definition for a custom configuration source. Provides more control over the configuration primitives that are used
 * by the generated {@link ConfigurationProperties} implementation class.
 */
public @interface CustomConfigurationSource {

    /**
     * Defines a custom {@link NameResolver}. The class must have a public, no-arg constructor.
     */
    Class<? extends NameResolver> names();

    /**
     * Defines a custom {@link ValueResolver}. The class must have a public, no-arg constructor.
     */
    Class<? extends ValueResolver> values();

    /**
     * Defines a custom {@link ConfigurationSource} class. The class must have a public constructor that takes the
     * following arguments, in the given order:
     * <ol>
     *     <li>{@link NameResolver}</li>
     *     <li>{@link ValueResolver}</li>
     *     <li>{@code Map<String, String>} (if {@link #extraConfig() extraConfig} is not empty)</li>
     * </ol>
     */
    Class<? extends ConfigurationSource> source() default SimpleConfigurationSource.class;

    /**
     * Custom configuration properties, defined as a string array, where each member is of the form {@code name = value}
     * or {@code name: value}. If present and non-empty, the {@link ConfigurationSource} implementation constructor must
     * accept a {@code Map<String, String>} as its 3rd argument.
     */
    String[] extraConfig() default {};
}
