/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NamespaceTest {

    @Test
    void root() {

        assertThat(Namespace.root().name()).isEqualTo("/");
    }

    @ParameterizedTest(name = "''{0}''")
    @MethodSource("ofSegments")
    @DisplayName("factory method 'of'")
    void of(String expectedName, String[] segments) {

        assertThat(Namespace.of(segments).name()).isEqualTo(expectedName);
    }

    static Stream<Arguments> ofSegments() {

        return Stream.of(arguments("/first", strings("first")),
                         arguments("/first/second", strings("first", "second")),
                         arguments("/number-1/number-2/number-3", strings("number-1", "number-2", "number-3")),
                         arguments("/two-parts/three-name-parts", strings("two-parts", "three-name-parts")),
                         arguments("/good-segment", strings("good.segment")),
                         arguments("/valid-name", strings("valid_name")),
                         arguments("/uppercaseallowed", strings("UppercaseAllowed")),
                         arguments("/good-segment/also/good/segment", strings("good-segment", "also/good/segment")),
                         arguments("/valid-segment/good-segment", strings("Valid-Segment", "good-segment")),
                         arguments("/this-is-fine/this-one-too/tambien-bueno", strings("this-is-fine", "this-one-too", "también_bueno")),
                         arguments("/spaces-are-allowed/and-numb3rs/and-pec-al-c-aracter-",
                                   strings("spaces are allowed", "and numb3rs", "and $pec!al c#aracter$")),
                         arguments("/", strings()),
                         arguments("/", strings((String) null)),
                         arguments("/", null));
    }

    @ParameterizedTest(name = "''{1}'' -> ''{0}''")
    @CsvSource(textBlock = """
                           /, /
                           /first, /first
                           /first/second, /first/second
                           /number-1/number-2/number-3, /number-1/number-2/number-3
                           /, '      '
                           /, ///
                           /first, first
                           /first/second, first/second
                           /good-segment/valid-segment, /good-segment/ValiD-SegmenT
                           /this-is-fine/this-is-fine, /this_is_fine/this-is-fine
                           /not-wrong, /not.wrong
                           /spaces-and/special-characters/a-w3d, /spaces and/special characters/a!!@w3d
                           /allow/empty-segments, /allow//empty-segments
                           /whitespace/around-segments/is-trimmed, '/ whitespace /\taround segments/is-trimmed '
                           /first/second, ///first//second/
                           """)
    @DisplayName("factory method 'parse'")
    void parse(String expectedName, String name) {

        assertThat(Namespace.parse(name).name()).isEqualTo(expectedName);
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

        assertThat(Namespace.normalizeSegment(segment)).isEqualTo(expectedResult);
    }

    @ParameterizedTest(name = "''{0}'' resolves to ''{2}''")
    @CsvSource(textBlock = """
                           /parent/sub-1, 2, sub-1
                           /parent/sub-1/sub-2/sub-3, 4, sub-1/sub-2/sub-3
                           /absolute/child, 2, /absolute/child
                           /parent/valid-name, 2, valid name
                           /parent/also-valid, 2, also__valid
                           """)
    @DisplayName("create child namespaces with valid name")
    void child_WithValidName(String expectedChildName, int expectedSegments, String name) {

        Namespace parent = Namespace.of("parent");
        Namespace child = parent.child(name);
        assertThat(child.name()).isEqualTo(expectedChildName);
        assertThat(child.segments()).hasSize(expectedSegments);
    }

    @ParameterizedTest(name = "''{0}''")
    @NullAndEmptySource
    @CsvSource(textBlock = """
                           '    '
                           """)
    @DisplayName("create child namespaces with invalid name")
    void child_WithInvalidName(String name) {

        Namespace parent = Namespace.of("parent");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> parent.child(name));
    }

    @Test
    @DisplayName("property names resolve")
    void resolveProperty() {

        Namespace namespace = Namespace.of("parent", "child");
        List<String> propName = namespace.resolveProperty("prop-name", "yet.anotherProp");
        assertThat(propName).containsExactly("parent", "child", "prop-name", "yet.anotherProp");
    }

    private static Object strings(String... strings) {

        return strings;
    }

    @Test
    @DisplayName("Comparable implementation")
    void comparable() {
        Namespace namespace = Namespace.of("parent", "child");
        Namespace same = Namespace.of("parent", "child");
        Namespace less = Namespace.of("parent", "a");
        Namespace more = Namespace.of("parent", "d");

        assertThat(namespace.compareTo(null)).isPositive();
        assertThat(namespace.compareTo(same)).isZero();
        assertThat(namespace.compareTo(less)).isPositive();
        assertThat(namespace.compareTo(more)).isNegative();
    }

    @Test
    @DisplayName("CharSequence implementation")
    void charSequence() {
        Namespace namespace = Namespace.of("parent", "child");
        assertThat(namespace.length()).isEqualTo(13);
        assertThat(namespace.charAt(0)).isEqualTo('/');
        assertThat(namespace.charAt(10)).isEqualTo('i');
        assertThat(namespace.subSequence(1, 7)).isEqualTo("parent");
    }
}
