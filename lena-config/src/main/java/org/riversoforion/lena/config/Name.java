/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.text.Normalizer.Form;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.text.Normalizer.normalize;
import static java.util.Arrays.asList;

public class Name implements CharSequence, Comparable<Name> {

    public static final String SEPARATOR = "/";
    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern NON_LETTERS_OR_NUMBERS = Pattern.compile("[^a-z0-9]+");

    private final List<String> rawSegments;
    private final String fullName;

    private Name(List<String> segments, boolean allowEmpty) {

        this.rawSegments = segments == null ? List.of() : segments.stream().filter(Name::notEmpty).flatMap(Name::splitSegment).toList();
        if (rawSegments.isEmpty() && !allowEmpty) {
            throw new IllegalArgumentException("Name must contain at least one segment");
        }
        StringJoiner joiner = new StringJoiner(SEPARATOR);
        this.rawSegments.stream().map(Name::normalizeSegment).filter(Name::notEmpty).forEach(joiner::add);
        this.fullName = joiner.toString();
    }

    static Stream<String> splitSegment(String segment) {

        return Stream.of(segment.split(SEPARATOR)).filter(Name::notEmpty);
    }

    static String normalizeSegment(String segment) {

        if (segment == null || segment.isBlank()) {
            return "";
        }
        String partiallyNormalized = NON_ASCII.matcher(normalize(segment.trim().toLowerCase(Locale.ROOT), Form.NFD)).replaceAll("");
        return NON_LETTERS_OR_NUMBERS.matcher(partiallyNormalized).replaceAll("-");
    }

    private static boolean notEmpty(String segment) {

        return segment != null && !segment.isBlank();
    }

    // Static factories

    public static Name of(String... segments) {

        if (segments == null || segments.length == 0) {
            throw new IllegalArgumentException("Name must contain at least one segment");
        }
        return new Name(asList(segments), false);
    }

    public static Name of(List<String> segments) {

        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("Name must contain at least one segment");
        }
        return new Name(segments, false);
    }

    static Name forNamespace(List<String> segments) {

        return new Name(segments, true);
    }

    // Operations

    public List<String> segments() {

        return List.copyOf(rawSegments);
    }

    // Object

    @Override
    public String toString() {

        return fullName;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Name namespace = (Name) o;
        return Objects.equals(fullName, namespace.fullName);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(fullName);
    }

    // CharSequence

    @Override
    public int length() {

        return fullName.length();
    }

    @Override
    public char charAt(int index) {

        return fullName.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {

        return fullName.subSequence(start, end);
    }

    // Comparable

    @Override
    public int compareTo(Name o) {

        if (o == null) {
            return 1;
        }
        return fullName.compareTo(o.fullName);
    }
}
