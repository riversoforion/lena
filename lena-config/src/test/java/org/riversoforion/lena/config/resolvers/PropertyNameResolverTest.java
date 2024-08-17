/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PropertyNameResolverTest {

    @DisplayName("resolveName with valid scenarios")
    @ParameterizedTest(name = "{0}+{1} -> {2}")
    @CsvSource(textBlock = """
                           , myProp, myProp
                           '', my.otherProp, my.otherProp
                           , prop name, prop name
                           '', name+with#funky?Characters!, name+with#funky?Characters!
                           my.prefix, myProp, my.prefix.myProp
                           my.prefix, my.otherProp, my.prefix.my.otherProp
                           my+prefix, %some$Other@PROP, my+prefix.%some$Other@PROP
                           """)
    void resolveName_Valid(String prefix, String name, String expected) {

        PropertyNameResolver resolver = new PropertyNameResolver(prefix);

        String result = resolver.resolveName(name);

        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("resolveName with invalid scenarios")
    @ParameterizedTest(name = "{0}+{1}")
    @CsvSource(textBlock = """
                           ,
                           , ''
                           '',
                           '    ', '  '
                           my.prefix,
                           my.prefix, ''
                           my+prefix,
                           my+prefix, ''
                           """)
    void resolveName_Invalid(String prefix, String name) {

        PropertyNameResolver resolver = new PropertyNameResolver(prefix);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> resolver.resolveName(name));
    }
}
