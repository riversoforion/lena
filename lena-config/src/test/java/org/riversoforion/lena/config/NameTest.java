/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NameTest {

    @ParameterizedTest(name = "''{0}''")
    @MethodSource("ofSegments")
    @DisplayName("factory method 'of'")
    void of(String expectedName, String[] segments) {

        Name name = Name.of(segments);
        assertThat(name.toString()).isEqualTo(expectedName);
        assertThat(name.segments()).isEqualTo(List.of(segments));
    }

    static Stream<Arguments> ofSegments() {

        return Stream.of(arguments("first", strings("first")),
                         arguments("first/second", strings("first", "second")),
                         arguments("number-1/number-2/number-3", strings("number-1", "number-2", "number-3")),
                         arguments("two-parts/three-name-parts", strings("two-parts", "three-name-parts")),
                         arguments("good-segment", strings("good.segment")),
                         arguments("valid-name", strings("valid_name")),
                         arguments("uppercaseallowed", strings("UppercaseAllowed")),
                         arguments("good-segment/also/good/segment", strings("good-segment", "also/good/segment")),
                         arguments("valid-segment/good-segment", strings("Valid-Segment", "good-segment")),
                         arguments("this-is-fine/this-one-too/tambien-bueno", strings("this-is-fine", "this-one-too", "también_bueno")),
                         arguments("spaces-are-allowed/and-numb3rs/and-pec-al-c-aracter-",
                                   strings("spaces are allowed", "and numb3rs", "and $pec!al c#aracter$")));
    }

    @SuppressWarnings("UnnecessaryStringEscape")
    @ParameterizedTest(name = "''{0}''")
    @CsvSource(textBlock = """
                           ,
                           ''
                           '   '
                           '\t  \r\n'
                           """)
    @DisplayName("factory method 'of' with invalid segments")
    void of_WithInvalidSegments(String segment) {

        assertThatException().isThrownBy(() -> Name.of(segment))
                             .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("factory method 'of' with null segment")
    void of_WithNullSegment() {

        //noinspection DataFlowIssue
        assertThatException().isThrownBy(() -> Name.of((String[]) null))
                             .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("factory method 'of' with empty segments")
    void of_WithEmptySegments() {

        assertThatException().isThrownBy(() -> Name.of(new String[0]))
                             .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "''{1}'' -> ''{0}''")
    @CsvSource(textBlock = """
                           segment, segment
                           segment-name, segment-name
                           segment-name, Segment-NAME
                           s3gment-name, s3gment name
                           -3gment-nam-, $3gment_nam*
                           segment-name, ' segment name '
                           '', ''
                           '',
                           '', '    '
                           """)
    @DisplayName("normalize segment")
    void normalizeSegment(String expectedResult, String segment) {

        assertThat(Name.normalizeSegment(segment)).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("Object implementation")
    void object() {

        Name name = Name.of("parent", "child", "grandchild");
        Name same = Name.of("parent", "child", "grandchild");
        Name different = Name.of("parent", "child", "other");

        assertThat((Object) name).isEqualTo(same)
                                 .hasSameHashCodeAs(same)
                                 .hasToString(same.toString());
        assertThat((Object) name).isNotEqualTo(different)
                                 .doesNotHaveSameHashCodeAs(different)
                                 .doesNotHaveToString(different.toString());
    }

    @Test
    @DisplayName("Comparable implementation")
    void comparable() {

        Name name = Name.of("parent", "child");
        Name same = Name.of("parent", "child");
        Name less = Name.of("parent", "a");
        Name more = Name.of("parent", "d");

        assertThat(name.compareTo(null)).isPositive();
        assertThat(name.compareTo(same)).isZero();
        assertThat(name.compareTo(less)).isPositive();
        assertThat(name.compareTo(more)).isNegative();
    }

    @Test
    @DisplayName("CharSequence implementation")
    void charSequence() {

        Name name = Name.of("parent", "child");
        assertThat(name.length()).isEqualTo(12);
        assertThat(name.charAt(0)).isEqualTo('p');
        assertThat(name.charAt(6)).isEqualTo('/');
        assertThat(name.subSequence(0, 6)).isEqualTo("parent");
        assertThat(name.subSequence(7, name.length())).isEqualTo("child");
    }

    private static Object strings(String... strings) {

        return strings;
    }
}
