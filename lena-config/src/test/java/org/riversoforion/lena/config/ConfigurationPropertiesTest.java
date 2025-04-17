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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationPropertiesTest {

    public static final Name PROP_NAME_CONNECTION_TIMEOUT = Name.of("connectionTimeout");
    public static final Name PROP_NAME_PROJECT = Name.of("project");
    public static final Name PROP_NAME_NON_EXISTENT = Name.of("nonExistent");
    private static final Name PROP_NAME_10 = Name.of("ten");
    private static final Name PROP_NAME_20 = Name.of("twenty");
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
        when(source.getValue(any(), any())).thenReturn(Optional.of("Y"));
        ConfigurationProperties props = propsWith(converter, null);

        assertThat(props.booleanVal(Name.of("prop"))).isTrue();

        verify(converter).toBoolean("Y");
    }

    @Test
    @DisplayName("ValueConverter is used to convert short values")
    void converter_WithShortValues(@Mock ValueConverter converter) {

        when(converter.toShort(any())).thenAnswer(valueParser(Short::parseShort));
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

        when(converter.toInt(any())).thenAnswer(valueParser(Integer::parseInt));
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

        when(converter.toLong(any())).thenAnswer(valueParser(Long::parseLong));
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

        when(converter.toFloat(any())).thenAnswer(valueParser(Float::parseFloat));
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

        when(converter.toDouble(any())).thenAnswer(valueParser(Double::parseDouble));
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

        when(source.getValue(eq(ROOT), any())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq(PROP_NAME_CONNECTION_TIMEOUT))).thenReturn(Optional.of("60"));
        Map<Name, String> defaults = Map.of(PROP_NAME_CONNECTION_TIMEOUT, "30", PROP_NAME_PROJECT, "lena-config");
        ConfigurationProperties props = propsWith(null, defaults);

        // Verify that a value from the ConfigurationSource takes precedence
        assertThat(props.stringVal(PROP_NAME_CONNECTION_TIMEOUT))
                .as("verify value from ConfigurationSource takes precedence")
                .isEqualTo("60");
        // Verify that a value missing from the ConfigurationSource is found in the defaults
        assertThat(props.stringVal(PROP_NAME_PROJECT))
                .as("verify default value is used")
                .isEqualTo("lena-config");
        // Verify that a value missing from both "falls through" to the missing logic
        assertThatException()
                .as("verify missing property falls through")
                .isThrownBy(() -> props.stringVal(PROP_NAME_NON_EXISTENT));
    }

    @Test
    @DisplayName("returnNullForMissing returns null")
    void returnNullForMissingValues() {

        when(source.getValue(eq(ROOT), any())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq(PROP_NAME_CONNECTION_TIMEOUT))).thenReturn(Optional.of("60"));
        ConfigurationProperties props = new TestConfigProps(source);
        props.returnNullForMissing();

        assertThat(props.stringVal(PROP_NAME_NON_EXISTENT)).isNull();
        assertThat(props.stringVal(PROP_NAME_CONNECTION_TIMEOUT)).isEqualTo("60");
    }

    @Test
    @DisplayName("throwExceptionForMissing throws exception")
    void throwExceptionForMissingValues() {

        when(source.getValue(eq(ROOT), any())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq(PROP_NAME_CONNECTION_TIMEOUT))).thenReturn(Optional.of("60"));
        ConfigurationProperties props = new TestConfigProps(source);
        props.throwExceptionForMissing();

        assertThatException().isThrownBy(() -> props.stringVal(PROP_NAME_NON_EXISTENT))
                             .isInstanceOf(IllegalArgumentException.class)
                             .withMessage("No configuration property named nonexistent");
        assertThat(props.stringVal(PROP_NAME_CONNECTION_TIMEOUT)).isEqualTo("60");
    }

    @Test
    @DisplayName("missing/set/default flags")
    void flagsForMissingSetAndDefault() {

        when(source.getValue(eq(ROOT), any())).thenReturn(Optional.empty());
        when(source.getValue(eq(ROOT), eq(PROP_NAME_CONNECTION_TIMEOUT))).thenReturn(Optional.of("60"));
        Map<Name, String> defaults = Map.of(PROP_NAME_CONNECTION_TIMEOUT, "30", PROP_NAME_PROJECT, "lena-config");
        ConfigurationProperties props = propsWith(null, defaults);

        assertThat(props.isMissing(PROP_NAME_CONNECTION_TIMEOUT)).isFalse();
        assertThat(props.isSet(PROP_NAME_CONNECTION_TIMEOUT)).isTrue();
        assertThat(props.isDefault(PROP_NAME_CONNECTION_TIMEOUT)).isFalse();

        assertThat(props.isMissing(PROP_NAME_PROJECT)).isFalse();
        assertThat(props.isSet(PROP_NAME_PROJECT)).isFalse();
        assertThat(props.isDefault(PROP_NAME_PROJECT)).isTrue();

        assertThat(props.isMissing(PROP_NAME_NON_EXISTENT)).isTrue();
        assertThat(props.isSet(PROP_NAME_NON_EXISTENT)).isFalse();
        assertThat(props.isDefault(PROP_NAME_NON_EXISTENT)).isFalse();
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

    private TestConfigProps propsWith(ValueConverter converter, Map<Name, String> defaults) {

        return new TestConfigProps(source) {
            @Override
            protected ValueConverter createConverter() {

                return converter != null ? converter : super.createConverter();
            }

            @Override
            protected Map<Name, String> createDefaults() {

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
