/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(SystemStubsExtension::class)
class EnvironmentValueResolverTest {

    @SystemStub
    private lateinit var envVars: EnvironmentVariables

    private val resolver = EnvironmentValueResolver()

    @Test
    fun resolveValue_Found() {
        envVars.set("RIVER", "Lena")
            .and("COUNTRY", "RU")
            .execute {
                assertEquals("RU", resolver.resolveValue("COUNTRY"))
                assertEquals("Lena", resolver.resolveValue("RIVER"))
            }
    }

    @Test
    fun resolveValue_NotFound() {
        envVars.execute {
            assertNull(resolver.resolveValue("RIVER"))
        }
    }
}
