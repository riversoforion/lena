/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.properties.SystemProperties
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(SystemStubsExtension::class)
class SystemPropertiesValueResolverTest {

    @SystemStub
    private lateinit var sysProps: SystemProperties

    private val resolver = SystemPropertiesValueResolver()

    @Test
    fun resolveValue_Found() {
        sysProps.set("river.name", "Lena")
            .set("river.country", "RU")
            .execute {
                assertEquals("Lena", resolver.resolveValue("river.name"))
                assertEquals("RU", resolver.resolveValue("river.country"))
            }
    }

    @Test
    fun resolveValue_NotFound() {
        sysProps.execute {
            assertNull(resolver.resolveValue("river.name"))
        }
    }
}
