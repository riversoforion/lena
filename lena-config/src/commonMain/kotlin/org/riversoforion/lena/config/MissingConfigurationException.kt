/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

/** Thrown when a required configuration property has no value and no default. */
public class MissingConfigurationException(public val name: Name) :
    IllegalArgumentException("No configuration property named '$name'")
