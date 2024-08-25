/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.riversoforion.lena.config.resolvers.EnvironmentNameResolver;
import org.riversoforion.lena.config.resolvers.EnvironmentValueResolver;
import org.riversoforion.lena.config.resolvers.PropertyNameResolver;
import org.riversoforion.lena.config.resolvers.SystemPropertiesValueResolver;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConfigurationSourceTest {

    @DisplayName("getValue delegates to resolvers")
    @Test
    void getValue_DelegatesToResolvers() {

        NameResolver names = mock();
        ValueResolver values = mock();
        when(names.resolveName("this.prop")).thenReturn("THIS_PROP");
        when(values.resolveValue("THIS_PROP")).thenReturn(Optional.of("some_value"));

        ConfigurationSource source = new ConfigurationSource(names, values);
        Optional<String> value = source.getValue("this.prop");

        assertThat(value).contains("some_value");
        verify(names).resolveName("this.prop");
        verify(values).resolveValue("THIS_PROP");
    }

    @DisplayName("forEnvironment creates correct resolvers")
    @Test
    void forEnvironment_CreatesCorrectResolvers() {

        ConfigurationSource source = ConfigurationSource.forEnvironment();

        assertThat(source).isNotNull();
        assertThat(source.getNameResolver()).isInstanceOf(EnvironmentNameResolver.class);
        assertThat(source.getValueResolver()).isInstanceOf(EnvironmentValueResolver.class);
    }

    @DisplayName("forEnvironment with a prefix creates correct resolvers")
    @Test
    void forEnvironment_WithPrefix_CreatesCorrectResolvers() {

        ConfigurationSource source = ConfigurationSource.forEnvironment("my.prefix");

        assertThat(source).isNotNull();
        assertThat(source.getNameResolver()).isInstanceOf(EnvironmentNameResolver.class);
        assertThat(source.getValueResolver()).isInstanceOf(EnvironmentValueResolver.class);
    }

    @DisplayName("forSystemProperties creates correct resolvers")
    @Test
    void forSystemProperties_CreatesCorrectResolvers() {

        ConfigurationSource source = ConfigurationSource.forSystemProperties();

        assertThat(source).isNotNull();
        assertThat(source.getNameResolver()).isInstanceOf(PropertyNameResolver.class);
        assertThat(source.getValueResolver()).isInstanceOf(SystemPropertiesValueResolver.class);
    }

    @DisplayName("forSystemProperties with a prefix creates correct resolvers")
    @Test
    void forSystemProperties_WithPrefix_CreatesCorrectResolvers() {

        ConfigurationSource source = ConfigurationSource.forSystemProperties("abc.123");

        assertThat(source).isNotNull();
        assertThat(source.getNameResolver()).isInstanceOf(PropertyNameResolver.class);
        assertThat(source.getValueResolver()).isInstanceOf(SystemPropertiesValueResolver.class);
    }
}
