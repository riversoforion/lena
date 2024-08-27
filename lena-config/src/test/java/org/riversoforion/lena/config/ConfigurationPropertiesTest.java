/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationPropertiesTest {

    @Mock
    SimpleConfigurationSource source;

    @DisplayName("Nested properties are stored and retrieved")
    @Test
    void nestedProperties_StoredAndRetrieved() {

        SimpleConfigurationSource nestedSource = mock(SimpleConfigurationSource.class);
        ConfigurationProperties nested = new NestedConfigProps(nestedSource);

        ConfigurationProperties props = new ConfigurationProperties(source).withNested("nest", nested);

        assertThat(props.nested("nest", NestedConfigProps.class)).isSameAs(nested);
    }

    @DisplayName("ValueConverter is used to convert boolean values")
    @Test
    void converter_WithBooleanValues(@Mock ValueConverter converter) {

        when(converter.toBoolean("Y")).thenReturn(true);
        when(source.getValue(anyString())).thenReturn(Optional.of("Y"));
        ConfigurationProperties props = new ConfigurationProperties(source).withConverter(converter);

        assertThat(props.booleanVal("prop")).isTrue();

        verify(converter).toBoolean("Y");
    }

    @DisplayName("ValueConverter is used to convert short values")
    @Test
    void converter_WithShortValues(@Mock ValueConverter converter) {

        when(converter.toShort("10")).thenReturn((short) 10);
        when(source.getValue(anyString())).thenReturn(Optional.of("10"));
        ConfigurationProperties props = new ConfigurationProperties(source).withConverter(converter);

        assertThat(props.shortVal("10")).isEqualTo((short) 10);

        verify(converter).toShort("10");
    }

    @DisplayName("ValueConverter is used to convert int values")
    @Test
    void converter_WithIntValues(@Mock ValueConverter converter) {

        when(converter.toInt("10")).thenReturn(10);
        when(source.getValue(anyString())).thenReturn(Optional.of("10"));
        ConfigurationProperties props = new ConfigurationProperties(source).withConverter(converter);

        assertThat(props.intVal("10")).isEqualTo(10);

        verify(converter).toInt("10");
    }

    @DisplayName("ValueConverter is used to convert long values")
    @Test
    void converter_WithLongValues(@Mock ValueConverter converter) {

        when(converter.toLong("10")).thenReturn(10L);
        when(source.getValue(anyString())).thenReturn(Optional.of("10"));
        ConfigurationProperties props = new ConfigurationProperties(source).withConverter(converter);

        assertThat(props.longVal("10")).isEqualTo(10L);

        verify(converter).toLong("10");
    }

    @DisplayName("ValueConverter is used to convert float values")
    @Test
    void converter_WithFloatValues(@Mock ValueConverter converter) {

        when(converter.toFloat("10.0")).thenReturn(10f);
        when(source.getValue(anyString())).thenReturn(Optional.of("10.0"));
        ConfigurationProperties props = new ConfigurationProperties(source).withConverter(converter);

        assertThat(props.floatVal("10.0")).isEqualTo(10f);

        verify(converter).toFloat("10.0");
    }

    @DisplayName("ValueConverter is used to convert double values")
    @Test
    void converter_WithDoubleValues(@Mock ValueConverter converter) {

        when(converter.toDouble("10.0")).thenReturn(10.0);
        when(source.getValue(anyString())).thenReturn(Optional.of("10.0"));
        ConfigurationProperties props = new ConfigurationProperties(source).withConverter(converter);

        assertThat(props.doubleVal("10.0")).isEqualTo(10.0);

        verify(converter).toDouble("10.0");
    }

    @DisplayName("Default values are used properly")
    @Test
    void defaultValues() {

        when(source.getValue(anyString())).thenReturn(Optional.empty());
        when(source.getValue("connectionTimeout")).thenReturn(Optional.of("60"));
        Map<String, String> defaults = Map.of("connectionTimeout", "30", "project", "lena-config");
        ConfigurationProperties props = new ConfigurationProperties(source).withDefaults(defaults);

        // Verify that a value from the ConfigurationSource takes precedence
        assertThat(props.stringVal("connectionTimeout")).isEqualTo("60");
        // Verify that a value missing from the ConfigurationSource is found in the defaults
        assertThat(props.stringVal("project")).isEqualTo("lena-config");
        // Verify that a value missing from both "falls through" to the missing logic
        assertThatException().isThrownBy(() -> props.stringVal("nonExistent"));
    }

    @DisplayName("returnNullForMissing returns null")
    @Test
    void returnNullForMissingValues() {

        when(source.getValue(anyString())).thenReturn(Optional.empty());
        when(source.getValue("connectionTimeout")).thenReturn(Optional.of("60"));
        ConfigurationProperties props = new ConfigurationProperties(source).returnNullForMissing();

        assertThat(props.stringVal("nonExistent")).isNull();
        assertThat(props.stringVal("connectionTimeout")).isEqualTo("60");
    }

    @DisplayName("throwExceptionForMissing throws exception")
    @Test
    void throwExceptionForMissingValues() {

        when(source.getValue(anyString())).thenReturn(Optional.empty());
        when(source.getValue("connectionTimeout")).thenReturn(Optional.of("60"));
        ConfigurationProperties props = new ConfigurationProperties(source).throwExceptionForMissing();

        assertThatException().isThrownBy(() -> props.stringVal("nonExistent"))
                             .isInstanceOf(IllegalArgumentException.class)
                             .withMessage("No configuration property named nonExistent");
        assertThat(props.stringVal("connectionTimeout")).isEqualTo("60");
    }

    @DisplayName("missing/set/default flags")
    @Test
    void missingSetDefaultFlags() {

        when(source.getValue(anyString())).thenReturn(Optional.empty());
        when(source.getValue("connectionTimeout")).thenReturn(Optional.of("60"));
        Map<String, String> defaults = Map.of("connectionTimeout", "30", "project", "lena-config");
        ConfigurationProperties props = new ConfigurationProperties(source).withDefaults(defaults);

        assertThat(props.isMissing("connectionTimeout")).isFalse();
        assertThat(props.isSet("connectionTimeout")).isTrue();
        assertThat(props.isDefault("connectionTimeout")).isFalse();

        assertThat(props.isMissing("project")).isFalse();
        assertThat(props.isSet("project")).isFalse();
        assertThat(props.isDefault("project")).isTrue();

        assertThat(props.isMissing("nonExistent")).isTrue();
        assertThat(props.isSet("nonExistent")).isFalse();
        assertThat(props.isDefault("nonExistent")).isFalse();
    }

    private static class NestedConfigProps extends ConfigurationProperties {

        protected NestedConfigProps(SimpleConfigurationSource source) {

            super(source);
        }
    }
}
