/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.annotations;

import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.ConfigurationSource;
import org.riversoforion.lena.config.DefaultValueConverter;
import org.riversoforion.lena.config.Namespace;
import org.riversoforion.lena.config.ValueConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as an "external configuration" facade. This will typically be an interface, but could also be a
 * class.
 * <p>
 * For interfaces, at least one {@link #source() source} is required. The {@link #namespace() namespace} will be the
 * root namespace by default. The generated implementation class will extend {@link ConfigurationProperties} and have a
 * no-arg constructor that creates an appropriate {@link ConfigurationSource} and {@link Namespace}.
 * </p>
 * <p>
 * Classes must:
 * </p>
 * <ul>
 *     <li>Extend {@link ConfigurationProperties}</li>
 *     <li>Be abstract</li>
 *     <li>Define at least one of the following public constructors (in priority order):
 *         <ol>
 *             <li>Override one of the protected constructors for {@code ConfigurationProperties}</li>
 *             <li>Constructor with zero arguments</li>
 *         </ol>
 *     </li>
 * </ul>
 * <p>
 * The {@link #source() source} and {@link #namespace() namespace} are optional. If omitted, the generated subclass
 * will assume that the class' constructor passes the necessary values to the {@code ConfigurationProperties}
 * super-constructor. A zero-arg constructor is required in this case.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface ExternalConfiguration {

    /**
     * A list of {@link ExternalConfigurationSource} annotations that define one or more external configuration sources
     * to act as a facade over.
     */
    ExternalConfigurationSource[] source() default {};

    /**
     * The top-level namespace for this external configuration. All properties defined by this facade or its children
     * facades will be relative to this namespace, unless overridden at the source or property level.
     */
    String namespace() default "";

    /**
     * Defines a custom {@link ValueConverter}.
     */
    Class<? extends ValueConverter> converter() default DefaultValueConverter.class;

    /**
     * Configuration defaults, defined as a string array, where each member is of the form {@code name = value} or
     * {@code name: value}.
     */
    String[] defaults() default {};

    /**
     * If true, the generated {@link ConfigurationProperties} will throw an exception for missing values. Otherwise, it
     * will return {@code nulls}.
     */
    boolean missingThrowsException() default true;
}
