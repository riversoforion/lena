/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.internal

/**
 * Reads a single environment variable by name. Returns `null` if the variable is not set.
 *
 * On JVM: delegates to `System.getenv`.
 * On native: delegates to POSIX `getenv` via `platform.posix`.
 */
internal expect fun platformGetEnv(name: String): String?
