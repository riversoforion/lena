/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.annotations;

import org.riversoforion.lena.config.NameResolver;
import org.riversoforion.lena.config.ValueResolver;

/**
 * Describes a single configuration source for external configuration. For well-known sources (environment variables,
 * system properties), simply specifying the {@linkplain #value() type} is the minimum required. For custom sources,
 * additional configuration is needed. See the annotation properties for details.
 */
public @interface ExternalConfigurationSource {

    /**
     * The type of this configuration source. If the type is {@link ExternalConfigurationSourceType#CUSTOM CUSTOM}, then
     * a {@link #custom()} annotation must be defined.
     */
    ExternalConfigurationSourceType value();

    /**
     * The namespace to be used for this configuration source. Overrides the one defined in
     * {@link ExternalConfiguration @ExternalConfiguration}.
     */
    String namespace() default "";

    /**
     * Custom configuration source definition.
     */
    CustomConfigurationSource custom() default @CustomConfigurationSource(names = NameResolver.class,
                                                                          values = ValueResolver.class);
}
