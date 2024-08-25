/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class DefaultValueConverterTest {

    private final DefaultValueConverter converter = new DefaultValueConverter();

    @DisplayName("toBoolean converts input correctly")
    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(textBlock = """
                           TRUE, true
                           true, true
                           Yes, true
                           yes, true
                           y, true
                           Y, true
                           on, true
                           On, true
                           1, true
                           FALSE, false
                           No, false
                           no, false
                           off, false
                           OFF, false
                           '', false
                           , false
                           """)
    void toBoolean_Valid(String input, boolean expected) {

        boolean actual = converter.toBoolean(input);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("toShort converts valid input correctly")
    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(textBlock = """
                           -32768, -32768
                           -1, -1
                           0, 0
                           1, 1
                           32767, 32767
                           , 0
                           """)
    void toShort_Valid(String input, short expected) {

        short actual = converter.toShort(input);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("toShort throws exception on invalid input")
    @ParameterizedTest(name = "\"{0}\"")
    @CsvSource(textBlock = """
                           -32769
                           32768
                           ''
                           other string
                           """)
    void toShort_Invalid(String input) {

        assertThatException().isThrownBy(() -> converter.toShort(input))
                             .isInstanceOf(IllegalArgumentException.class)
                             .withMessageContaining(input);
    }
}
