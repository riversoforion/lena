/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class DefaultValueConverterTest {

    private final DefaultValueConverter converter = new DefaultValueConverter();

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
    @DisplayName("toBoolean converts input correctly")
    void toBoolean_Valid(String input, boolean expected) {

        boolean actual = converter.toBoolean(input);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(textBlock = """
                           -32768, -32768
                           -1, -1
                           0, 0
                           1, 1
                           32767, 32767
                           , 0
                           """)
    @DisplayName("toShort converts valid input correctly")
    void toShort_Valid(String input, short expected) {

        short actual = converter.toShort(input);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "\"{0}\"")
    @CsvSource(textBlock = """
                           -32769
                           32768
                           ''
                           other string
                           """)
    @DisplayName("toShort throws exception on invalid input")
    void toShort_Invalid(String input) {

        assertThatException().isThrownBy(() -> converter.toShort(input))
                             .isInstanceOf(IllegalArgumentException.class)
                             .withMessageContaining(input);
    }
}
