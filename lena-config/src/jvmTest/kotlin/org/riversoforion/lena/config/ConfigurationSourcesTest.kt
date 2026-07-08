/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import org.riversoforion.lena.config.resolvers.EnvironmentNameResolver
import org.riversoforion.lena.config.resolvers.EnvironmentValueResolver
import org.riversoforion.lena.config.resolvers.PropertyNameResolver
import org.riversoforion.lena.config.resolvers.SystemPropertiesValueResolver
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class ConfigurationSourcesTest {

    @Test
    fun forEnvironment_CreatesCorrectResolvers() {
        val source = ConfigurationSources.forEnvironment()

        val simpleSource = assertIs<SimpleConfigurationSource>(source)
        assertIs<EnvironmentNameResolver>(simpleSource.nameResolver)
        assertIs<EnvironmentValueResolver>(simpleSource.valueResolver)
    }

    @Test
    fun forSystemProperties_CreatesCorrectResolvers() {
        val source = ConfigurationSources.forSystemProperties()

        val simpleSource = assertIs<SimpleConfigurationSource>(source)
        assertIs<PropertyNameResolver>(simpleSource.nameResolver)
        assertIs<SystemPropertiesValueResolver>(simpleSource.valueResolver)
    }

    @Test
    fun customSource_CreatesCorrectly() {
        val names = NameResolver { _, _ -> "n/a" }
        val values = ValueResolver { null }

        val source = ConfigurationSources.custom(names, values)

        val simpleSource = assertIs<SimpleConfigurationSource>(source)
        assertSame(names, simpleSource.nameResolver)
        assertSame(values, simpleSource.valueResolver)
    }

    @Test
    fun prioritizedSource_CreatesCorrectly() {
        val first = ConfigurationSource { _, _ -> null }
        val second = ConfigurationSource { _, _ -> null }
        val third = ConfigurationSource { _, _ -> null }

        val source = ConfigurationSources.prioritized(first, second, third)

        assertIs<PrioritizedConfigurationSource>(source)
    }
}
