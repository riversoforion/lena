/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

    private static final String PROP_NAME_10 = "ten";
    private static final String PROP_NAME_20 = "twenty";
    private static final Namespace ROOT = Namespace.root();

    @Mock
    ConfigurationSource source;
    @Mock
    ConfigurationPropertiesRegistry registry;
    MockedStatic<ConfigurationPropertiesRegistry> mockRegistry = mockStatic(ConfigurationPropertiesRegistry.class);

    @BeforeEach
    void setup() {

        mockRegistry.when(ConfigurationPropertiesRegistry::instance).thenReturn(registry);
    }

    @AfterEach
    void tearDown() {

        mockRegistry.close();
    }

    @Test
    @DisplayName("Children are created during construction")
    void construction_CreatesChildren() {

        TestConfigProps props = new TestConfigProps(source);

        assertThat(props.child()).isNotNull().isInstanceOf(ChildConfigProps.class);
    }

    @Test
    @DisplayName("ValueConverter is used to convert boolean values")
    void converter_WithBooleanValues(@Mock ValueConverter converter) {

        when(converter.toBoolean("Y")).thenReturn(true);
        when(source.getValue(any(), anyString())).thenReturn(Optional.of("Y"));
        ConfigurationProperties props = propsWith(converter, null);

        assertThat(props.booleanVal("prop")).isTrue();

        verify(converter).toBoolean("Y");
    }

    @Test
    @DisplayName("ValueConverter is used to convert short values")
    void converter_WithShortValues(@Mock ValueConverter converter) {

        when(converter.toShort(anyString())).thenAnswer(valueParser(Short::parseShort));
        mockSourceIntegralValues();
        ConfigurationProperties props = propsWith(converter, null);

        assertThat(props.shortVal(PROP_NAME_10)).isEqualTo((short) 10);
        assertThat(props.shortVal(PROP_NAME_20)).isEqualTo((short) 20);

        verify(converter).toShort("10");
        verify(converter).toShort("20");
    }

    @Test
    @DisplayName("ValueConverter is used to convert int values")
    void converter_WithIntValues(@Mock ValueConverter converter) {

        when(converter.toInt(anyString())).thenAnswer(valueParser(Integer::parseInt));
        mockSourceIntegralValues();
        ConfigurationProperties props = propsWith(converter, null);

        assertThat(props.intVal(PROP_NAME_10)).isEqualTo(10);
        assertThat(props.intVal(PROP_NAME_20)).isEqualTo(20);

        verify(converter).toInt("10");
        verify(converter).toInt("20");
    }

    @Test
    @DisplayName("ValueConverter is used to convert long values")
    void converter_WithLongValues(@Mock ValueConverter converter) {

        when(converter.toLong(anyString())).thenAnswer(valueParser(Long::parseLong));
        mockSourceIntegralValues();
        ConfigurationProperties props = propsWith(converter, null);

        assertThat(props.longVal(PROP_NAME_10)).isEqualTo(10L);
        assertThat(props.longVal(PROP_NAME_20)).isEqualTo(20L);

        verify(converter).toLong("10");
        verify(converter).toLong("20");
    }

    @Test
    @DisplayName("ValueConverter is used to convert float values")
    void converter_WithFloatValues(@Mock ValueConverter converter) {

        when(converter.toFloat(anyString())).thenAnswer(valueParser(Float::parseFloat));
        mockSourceFloatValues();
        ConfigurationProperties props = propsWith(converter, null);

        assertThat(props.floatVal(PROP_NAME_10)).isEqualTo(10f);
        assertThat(props.floatVal(PROP_NAME_20)).isEqualTo(20f);

        verify(converter).toFloat("10.0");
        verify(converter).toFloat("20.0");
    }

    @Test
    @DisplayName("ValueConverter is used to convert double values")
    void converter_WithDoubleValues(@Mock ValueConverter converter) {

        when(converter.toDouble(anyString())).thenAnswer(valueParser(Double::parseDouble));
        mockSourceFloatValues();
        ConfigurationProperties props = propsWith(converter, null);

        assertThat(props.doubleVal(PROP_NAME_10)).isEqualTo(10.0);
        assertThat(props.doubleVal(PROP_NAME_20)).isEqualTo(20.0);

        verify(converter).toDouble("10.0");
        verify(converter).toDouble("20.0");
    }

    @Test
    @DisplayName("default values are used properly")
    void defaultValues() {

        when(source.getValue(eq(ROOT), anyString())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq("connectionTimeout"))).thenReturn(Optional.of("60"));
        Map<String, String> defaults = Map.of("connectionTimeout", "30", "project", "lena-config");
        ConfigurationProperties props = propsWith(null, defaults);

        // Verify that a value from the ConfigurationSource takes precedence
        assertThat(props.stringVal("connectionTimeout"))
                .as("verify value from ConfigurationSource takes precedence")
                .isEqualTo("60");
        // Verify that a value missing from the ConfigurationSource is found in the defaults
        assertThat(props.stringVal("project"))
                .as("verify default value is used")
                .isEqualTo("lena-config");
        // Verify that a value missing from both "falls through" to the missing logic
        assertThatException()
                .as("verify missing property falls through")
                .isThrownBy(() -> props.stringVal("nonExistent"));
    }

    @Test
    @DisplayName("returnNullForMissing returns null")
    void returnNullForMissingValues() {

        when(source.getValue(eq(ROOT), anyString())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq("connectionTimeout"))).thenReturn(Optional.of("60"));
        ConfigurationProperties props = new TestConfigProps(source);
        props.returnNullForMissing();

        assertThat(props.stringVal("nonExistent")).isNull();
        assertThat(props.stringVal("connectionTimeout")).isEqualTo("60");
    }

    @Test
    @DisplayName("throwExceptionForMissing throws exception")
    void throwExceptionForMissingValues() {

        when(source.getValue(eq(ROOT), anyString())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq("connectionTimeout"))).thenReturn(Optional.of("60"));
        ConfigurationProperties props = new TestConfigProps(source);
        props.throwExceptionForMissing();

        assertThatException().isThrownBy(() -> props.stringVal("nonExistent"))
                             .isInstanceOf(IllegalArgumentException.class)
                             .withMessage("No configuration property named nonExistent");
        assertThat(props.stringVal("connectionTimeout")).isEqualTo("60");
    }

    @Test
    @DisplayName("missing/set/default flags")
    void flagsForMissingSetAndDefault() {

        when(source.getValue(eq(ROOT), anyString())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq("connectionTimeout"))).thenReturn(Optional.of("60"));
        Map<String, String> defaults = Map.of("connectionTimeout", "30", "project", "lena-config");
        ConfigurationProperties props = propsWith(null, defaults);

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

        when(source.getValue(ROOT, PROP_NAME_10)).thenReturn(Optional.of("10"));
        when(source.getValue(ROOT, PROP_NAME_20)).thenReturn(Optional.of("20"));
    }

    private void mockSourceFloatValues() {

        when(source.getValue(ROOT, PROP_NAME_10)).thenReturn(Optional.of("10.0"));
        when(source.getValue(ROOT, PROP_NAME_20)).thenReturn(Optional.of("20.0"));
    }

    private <T> Answer<T> valueParser(Function<String, T> parser) {

        return (invocation) -> parser.apply(invocation.getArgument(0));
    }

    private TestConfigProps propsWith(ValueConverter converter, Map<String, String> defaults) {

        return new TestConfigProps(source) {
            @Override
            protected ValueConverter createConverter() {

                return converter != null ? converter : super.createConverter();
            }

            @Override
            protected Map<String, String> createDefaults() {

                return defaults != null ? defaults : super.createDefaults();
            }
        };
    }

    private class TestConfigProps extends ConfigurationProperties {

        private ChildConfigProps child;

        protected TestConfigProps(ConfigurationSource source) {

            super(source);
        }

        @Override
        protected void createChildren() {

            child = new ChildConfigProps(ConfigurationPropertiesTest.this.source, namespace().child("child"));
        }

        public ChildConfigProps child() {

            return child;
        }
    }

    private static class ChildConfigProps extends ConfigurationProperties {

        protected ChildConfigProps(ConfigurationSource source, Namespace namespace) {

            super(source, namespace);
        }
    }
}
