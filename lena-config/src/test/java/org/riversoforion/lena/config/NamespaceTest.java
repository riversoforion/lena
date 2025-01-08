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

    @DisplayName("of (valid segments)")
    @ParameterizedTest(name = "''{0}''")
    @MethodSource("validSegments")
    void of_WithValidSegments(String expectedName, String[] segments) {

        assertThat(Namespace.of(segments).name()).isEqualTo(expectedName);
    }

    static Stream<Arguments> validSegments() {

        return Stream.of(arguments("/first", strings("first")),
                         arguments("/first/second", strings("first", "second")),
                         arguments("/number-1/number-2/number-3", strings("number-1", "number-2", "number-3")),
                         arguments("/two-parts/three-name-parts", strings("two-parts", "three-name-parts")),
                         arguments("/", strings()),
                         arguments("/", strings((String) null)),
                         arguments("/", null));
    }

    @DisplayName("of (invalid segments)")
    @ParameterizedTest(name = "''{0}''")
    @MethodSource("invalidSegments")
    void of_WithInvalidSegments(String[] segments) {

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Namespace.of(segments));
    }

    static Stream<Arguments> invalidSegments() {

        return Stream.of(arguments(strings("bad.segment")),
                         arguments(strings("invalid_name")),
                         arguments(strings("NoUppercase")),
                         arguments(strings("good-segment", "bad/segment")),
                         arguments(strings("Bad-Segment", "good-segment")),
                         arguments(strings("this-is-fine", "this-one-too", "no_bueno")),
                         arguments(strings("no spaces allowed")));
    }

    @DisplayName("parse (valid name)")
    @ParameterizedTest(name = "''{0}''")
    @CsvSource(textBlock = """
                           /
                           /first
                           /first/second
                           /number-1/number-2/number-3
                           """)
    void parse_WithValidName(String name) {

        assertThat(Namespace.parse(name).name()).isEqualTo(name);
    }

    @DisplayName("parse (invalid name)")
    @ParameterizedTest(name = "''{0}''")
    @NullAndEmptySource
    @CsvSource(textBlock = """
                           '      '
                           first
                           first/second
                           /good-segment/Bad-Segment
                           /this_is_wrong/this-is-fine
                           /also.wrong
                           /can't have spaces/or/special characters
                           /no//empty-segments
                           /no/ whitespace /\tinside segments
                           """)
    void parse_WithInvalidName(String name) {

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Namespace.parse(name));
    }

    @DisplayName("isValidSegment (valid)")
    @ParameterizedTest(name = "''{0}''")
    @CsvSource(textBlock = """
                           valid
                           i-am-valid
                           v4l1d
                           this-is-a-6-part-name
                           """)
    void isValidSegment_WithValidSegment(String segment) {

        assertThat(Namespace.isSegmentValid(segment)).isTrue();
    }

    @DisplayName("isValidSegment (invalid)")
    @ParameterizedTest(name = "''{0}''")
    @NullAndEmptySource
    @CsvSource(textBlock = """
                           i'm-invalid
                           also_not_valid
                           in.valid
                           ' beginning-space'
                           'trailing-space '
                           spaces in the middle
                           """)
    void isValidSegment_WithInvalidSegment(String segment) {

        assertThat(Namespace.isSegmentValid(segment)).isFalse();
    }

    @DisplayName("child (valid name)")
    @ParameterizedTest(name = "''{0}'' resolves to ''{1}''")
    @CsvSource(textBlock = """
                           sub-1, /parent/sub-1
                           sub-1/sub-2/sub-3, /parent/sub-1/sub-2/sub-3
                           /absolute/child, /absolute/child
                           """)
    void child_WithValidName(String name, String expectedChildName) {

        Namespace parent = Namespace.of("parent");
        Namespace child = parent.child(name);
        assertThat(child.name()).isEqualTo(expectedChildName);
    }

    @DisplayName("child (invalid name)")
    @ParameterizedTest(name = "''{0}''")
    @NullAndEmptySource
    @CsvSource(textBlock = """
                           '    '
                           invalid name
                           also_not_valid
                           """)
    void child_WithInvalidName(String name) {

        Namespace parent = Namespace.of("parent");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> parent.child(name));
    }

    @DisplayName("resolveProperty (valid name)")
    @Test
    void resolveProperty_WithValidPropertyName() {

        Namespace namespace = Namespace.of("parent", "child");
        List<String> propName = namespace.resolveProperty("prop-name");
        assertThat(propName).containsExactly("parent", "child", "prop-name");
    }

    @DisplayName("resolveProperty (invalid name)")
    @Test
    void resolveProperty_WithInvalidPropertyName() {

        Namespace namespace = Namespace.of("parent", "child");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> namespace.resolveProperty("invalid.prop.name"));
    }

    private static Object strings(String... strings) {

        return strings;
    }

    @DisplayName("Comparable implementation")
    @Test
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

    @DisplayName("CharSequence implementation")
    @Test
    void charSequence() {
        Namespace namespace = Namespace.of("parent", "child");
        assertThat(namespace.length()).isEqualTo(13);
        assertThat(namespace.charAt(0)).isEqualTo('/');
        assertThat(namespace.charAt(10)).isEqualTo('i');
        assertThat(namespace.subSequence(1, 7)).isEqualTo("parent");
    }
}
