/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.riversoforion.lena.config.resolvers.EnvironmentNameResolver;
import org.riversoforion.lena.config.resolvers.EnvironmentValueResolver;
import org.riversoforion.lena.config.resolvers.PropertyNameResolver;
import org.riversoforion.lena.config.resolvers.SystemPropertiesValueResolver;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConfigurationSourcesTest {

    @Test
    @DisplayName("forEnvironment creates correct resolvers")
    void forEnvironment_CreatesCorrectResolvers() {

        ConfigurationSource source = ConfigurationSources.forEnvironment();

        assertThat(source).isNotNull()
                          .isInstanceOf(SimpleConfigurationSource.class);
        SimpleConfigurationSource simpleSource = (SimpleConfigurationSource) source;
        assertThat(simpleSource.getNameResolver()).isInstanceOf(EnvironmentNameResolver.class);
        assertThat(simpleSource.getValueResolver()).isInstanceOf(EnvironmentValueResolver.class);
    }

    @Test
    @DisplayName("forSystemProperties creates correct resolvers")
    void forSystemProperties_CreatesCorrectResolvers() {

        ConfigurationSource source = ConfigurationSources.forSystemProperties();

        assertThat(source).isNotNull()
                          .isInstanceOf(SimpleConfigurationSource.class);
        SimpleConfigurationSource simpleSource = (SimpleConfigurationSource) source;
        assertThat(simpleSource.getNameResolver()).isInstanceOf(PropertyNameResolver.class);
        assertThat(simpleSource.getValueResolver()).isInstanceOf(SystemPropertiesValueResolver.class);
    }

    @Test
    @DisplayName("custom source is created properly")
    void customSource_CreatesCorrectly(@Mock NameResolver names, @Mock ValueResolver values) {

        ConfigurationSource source = ConfigurationSources.custom(names, values);

        assertThat(source).isNotNull()
                          .isInstanceOf(SimpleConfigurationSource.class);
        SimpleConfigurationSource simpleSource = (SimpleConfigurationSource) source;
        assertThat(simpleSource.getNameResolver()).isSameAs(names);
        assertThat(simpleSource.getValueResolver()).isSameAs(values);
    }

    @Test
    @DisplayName("prioritized source is created properly")
    void prioritizedSource_CreatesCorrectly(@Mock ConfigurationSource first, @Mock ConfigurationSource second, @Mock ConfigurationSource third) {

        ConfigurationSource source = ConfigurationSources.prioritized(first, second, third);

        assertThat(source).isNotNull()
                          .isInstanceOf(PrioritizedConfigurationSource.class);
    }
}
