/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationPropertiesTest {

    public static final String PROP_NAME_10 = "ten";
    public static final String PROP_NAME_20 = "twenty";
    @Mock
    SimpleConfigurationSource source;

    @DisplayName("Nested properties are stored and retrieved")
    @Test
    void nestedProperties_StoredAndRetrieved() {

        SimpleConfigurationSource nestedSource = mock(SimpleConfigurationSource.class);
        ConfigurationProperties nested = new NestedConfigProps(nestedSource);

        ConfigurationProperties props = new ConfigurationProperties(source);
        props.addNested("nest", nested);

        assertThat(props.nested("nest", NestedConfigProps.class)).isSameAs(nested);
    }

    @DisplayName("ValueConverter is used to convert boolean values")
    @Test
    void converter_WithBooleanValues(@Mock ValueConverter converter) {

        when(converter.toBoolean("Y")).thenReturn(true);
        when(source.getValue(anyString())).thenReturn(Optional.of("Y"));
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initConverter(converter);

        assertThat(props.booleanVal("prop")).isTrue();

        verify(converter).toBoolean("Y");
    }

    @DisplayName("ValueConverter is used to convert short values")
    @Test
    void converter_WithShortValues(@Mock ValueConverter converter) {

        when(converter.toShort(anyString())).thenAnswer(valueParser(Short::parseShort));
        mockSourceIntegralValues();
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initConverter(converter);

        assertThat(props.shortVal(PROP_NAME_10)).isEqualTo((short) 10);
        assertThat(props.shortVal(PROP_NAME_20)).isEqualTo((short) 20);

        verify(converter).toShort("10");
        verify(converter).toShort("20");
    }

    @DisplayName("ValueConverter is used to convert int values")
    @Test
    void converter_WithIntValues(@Mock ValueConverter converter) {

        when(converter.toInt(anyString())).thenAnswer(valueParser(Integer::parseInt));
        mockSourceIntegralValues();
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initConverter(converter);

        assertThat(props.intVal(PROP_NAME_10)).isEqualTo(10);
        assertThat(props.intVal(PROP_NAME_20)).isEqualTo(20);

        verify(converter).toInt("10");
        verify(converter).toInt("20");
    }

    @DisplayName("ValueConverter is used to convert long values")
    @Test
    void converter_WithLongValues(@Mock ValueConverter converter) {

        when(converter.toLong(anyString())).thenAnswer(valueParser(Long::parseLong));
        mockSourceIntegralValues();
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initConverter(converter);

        assertThat(props.longVal(PROP_NAME_10)).isEqualTo(10L);
        assertThat(props.longVal(PROP_NAME_20)).isEqualTo(20L);

        verify(converter).toLong("10");
        verify(converter).toLong("20");
    }

    @DisplayName("ValueConverter is used to convert float values")
    @Test
    void converter_WithFloatValues(@Mock ValueConverter converter) {

        when(converter.toFloat(anyString())).thenAnswer(valueParser(Float::parseFloat));
        mockSourceFloatValues();
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initConverter(converter);

        assertThat(props.floatVal(PROP_NAME_10)).isEqualTo(10f);
        assertThat(props.floatVal(PROP_NAME_20)).isEqualTo(20f);

        verify(converter).toFloat("10.0");
        verify(converter).toFloat("20.0");
    }

    @DisplayName("ValueConverter is used to convert double values")
    @Test
    void converter_WithDoubleValues(@Mock ValueConverter converter) {

        when(converter.toDouble(anyString())).thenAnswer(valueParser(Double::parseDouble));
        mockSourceFloatValues();
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initConverter(converter);

        assertThat(props.doubleVal(PROP_NAME_10)).isEqualTo(10.0);
        assertThat(props.doubleVal(PROP_NAME_20)).isEqualTo(20.0);

        verify(converter).toDouble("10.0");
        verify(converter).toDouble("20.0");
    }

    @DisplayName("Default values are used properly")
    @Test
    void defaultValues() {

        when(source.getValue(anyString())).thenReturn(Optional.empty());
        when(source.getValue("connectionTimeout")).thenReturn(Optional.of("60"));
        Map<String, String> defaults = Map.of("connectionTimeout", "30", "project", "lena-config");
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initDefaults(defaults);

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
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.returnNullForMissing();

        assertThat(props.stringVal("nonExistent")).isNull();
        assertThat(props.stringVal("connectionTimeout")).isEqualTo("60");
    }

    @DisplayName("throwExceptionForMissing throws exception")
    @Test
    void throwExceptionForMissingValues() {

        when(source.getValue(anyString())).thenReturn(Optional.empty());
        when(source.getValue("connectionTimeout")).thenReturn(Optional.of("60"));
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.throwExceptionForMissing();

        assertThatException().isThrownBy(() -> props.stringVal("nonExistent"))
                             .isInstanceOf(IllegalArgumentException.class)
                             .withMessage("No configuration property named nonExistent");
        assertThat(props.stringVal("connectionTimeout")).isEqualTo("60");
    }

    @DisplayName("missing/set/default flags")
    @Test
    void flagsForMissingSetAndDefault() {

        when(source.getValue(anyString())).thenReturn(Optional.empty());
        when(source.getValue("connectionTimeout")).thenReturn(Optional.of("60"));
        Map<String, String> defaults = Map.of("connectionTimeout", "30", "project", "lena-config");
        ConfigurationProperties props = new ConfigurationProperties(source);
        props.initDefaults(defaults);

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

    private void mockSourceIntegralValues() {

        when(source.getValue(PROP_NAME_10)).thenReturn(Optional.of("10"));
        when(source.getValue(PROP_NAME_20)).thenReturn(Optional.of("20"));
    }

    private void mockSourceFloatValues() {
        when(source.getValue(PROP_NAME_10)).thenReturn(Optional.of("10.0"));
        when(source.getValue(PROP_NAME_20)).thenReturn(Optional.of("20.0"));
    }

    private <T> Answer<T> valueParser(Function<String, T> parser) {
        return (invocation) -> parser.apply(invocation.getArgument(0));
    }

    private static class NestedConfigProps extends ConfigurationProperties {

        protected NestedConfigProps(SimpleConfigurationSource source) {

            super(source);
        }
    }
}
