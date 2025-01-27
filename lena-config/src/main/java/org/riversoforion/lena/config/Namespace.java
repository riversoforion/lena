/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.text.Normalizer.normalize;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

/**
 * Configuration namespace, including property names. A normalized namespace is as follows:
 * <ul>
 *     <li></li>
 * </ul>
 */
// TODO Document
public class Namespace implements Comparable<Namespace>, CharSequence {

    public static final String SEPARATOR = "/";
    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern LETTERS_AND_NUMBERS = Pattern.compile("[^a-z0-9]+");

    private final List<String> rawSegments;
    private final String name;

    private Namespace(List<String> segments) {

        this.rawSegments = segments == null ? List.of() : segments.stream().filter(Namespace::notEmpty).toList();
        StringJoiner joiner = new StringJoiner(SEPARATOR, SEPARATOR, "");
        this.rawSegments.stream().map(Namespace::normalizeSegment).filter(Namespace::notEmpty).forEach(joiner::add);
        this.name = joiner.toString();
    }

    // Static factories

    public static Namespace root() {

        return of();
    }

    public static Namespace of(String... segments) {

        if (segments == null) {
            segments = new String[0];
        }
        return new Namespace(stream(segments).filter(Namespace::notEmpty).flatMap(Namespace::splitSegment).toList());
    }

    public static Namespace parse(String name) {

        String[] segments = name.split(SEPARATOR);
        if (segments.length == 1 && segments[0].isEmpty()) {
            return root();
        }
        return new Namespace(asList(segments));
    }

    static Stream<String> splitSegment(String segment) {

        return Stream.of(segment.split(SEPARATOR)).filter(Namespace::notEmpty);
    }

    static String normalizeSegment(String segment) {

        if (segment == null || segment.isBlank()) {
            return "";
        }
        String partiallyNormalized = NON_ASCII.matcher(normalize(segment.trim().toLowerCase(Locale.ROOT), Form.NFD)).replaceAll("");
        return LETTERS_AND_NUMBERS.matcher(partiallyNormalized).replaceAll("-");
    }

    private static boolean notEmpty(String segment) {

        return segment != null && !segment.isBlank();
    }

    // Operations

    public String name() {

        return name;
    }

    public List<String> segments() {

        return List.copyOf(rawSegments);
    }

    public Namespace child(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Child namespace must contain at least one new segment");
        }
        if (name.startsWith(SEPARATOR)) {
            return Namespace.parse(name);
        }
        List<String> childSegments = asList(name.split(SEPARATOR));
        if (childSegments.isEmpty()) {
            throw new IllegalArgumentException("Child namespace must contain at least one new segment");
        }
        List<String> allSegments = new ArrayList<>(this.rawSegments);
        allSegments.addAll(childSegments);
        return new Namespace(allSegments);
    }

    public List<String> resolveProperty(String... names) {

        String[] propertyNames = Objects.requireNonNullElse(names, new String[0]);
        if (propertyNames.length == 0) {
            throw new IllegalArgumentException("Property names cannot be empty");
        }
        List<String> property = new ArrayList<>(rawSegments);
        property.addAll(asList(propertyNames));
        return property;
    }

    // Object

    @Override
    public String toString() {

        return name();
    }

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Namespace namespace = (Namespace) o;
        return Objects.equals(name, namespace.name);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(name);
    }

    // CharSequence

    @Override
    public int length() {

        return name.length();
    }

    @Override
    public char charAt(int index) {

        return name.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {

        return name.subSequence(start, end);
    }

    // Comparable

    @Override
    public int compareTo(Namespace o) {

        if (o == null) {
            return 1;
        }
        return name.compareTo(o.name);
    }
}
