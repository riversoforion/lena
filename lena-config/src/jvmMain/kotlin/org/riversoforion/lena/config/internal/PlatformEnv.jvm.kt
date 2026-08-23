/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.internal

internal actual fun platformGetEnv(name: String): String? = System.getenv(name)
