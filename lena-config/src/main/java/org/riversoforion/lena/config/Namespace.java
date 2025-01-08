/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

/**
 * Normalized configuration namespace, including property names. A normalized namespace is as follows:
 * <ul>
 *     <li></li>
 * </ul>
 */
public class Namespace implements Comparable<Namespace>, CharSequence {

    public static final String SEPARATOR = "/";
    private static final Pattern SEGMENT_PATTERN = Pattern.compile("^[a-z0-9-]*$");

    private final List<String> segments;
    private final String name;

    private Namespace(List<String> segments) {

        this.segments = segments == null ? List.of() : List.copyOf(segments);
        StringJoiner joiner = new StringJoiner(SEPARATOR, SEPARATOR, "");
        this.segments.forEach(joiner::add);
        this.name = joiner.toString();

    }

    public static Namespace root() {

        return of();
    }

    public static Namespace of(String... segments) {

        if (segments == null) {
            segments = new String[0];
        }
        return new Namespace(stream(segments).filter(Objects::nonNull).peek(Namespace::validateSegment).toList());
    }

    public static Namespace parse(String name) {

        validate(name);
        String[] segments = name.substring(1).split(SEPARATOR);
        if (segments.length == 1 && segments[0].isEmpty()) {
            return root();
        }
        stream(segments).forEach(Namespace::validateSegment);
        return new Namespace(asList(segments));
    }

    static boolean isValid(String namespace) {

        return namespace != null && namespace.startsWith(SEPARATOR);
    }

    static void validate(String name) {

        if (!isValid(name)) {
            throw new IllegalArgumentException("Invalid namespace: " + name);
        }
    }

    static boolean isSegmentValid(String segment) {

        if (segment == null || segment.isEmpty()) {
            return false;
        }
        return SEGMENT_PATTERN.matcher(segment).matches();
    }

    static void validateSegment(String segment) {

        if (!isSegmentValid(segment)) {
            throw new IllegalArgumentException("Invalid namespace segment: " + segment);
        }
    }

    public String name() {

        return name;
    }

    public Namespace child(String namespace) {

        if (namespace == null) {
            throw new IllegalArgumentException("Child namespace cannot be null");
        }
        if (namespace.startsWith(SEPARATOR)) {
            return Namespace.parse(namespace);
        }
        List<String> childSegments = asList(namespace.split(SEPARATOR));
        if (childSegments.isEmpty()) {
            throw new IllegalArgumentException("Child namespace must contain at least one segment");
        }
        childSegments.forEach(Namespace::validateSegment);
        List<String> allSegments = new ArrayList<>(this.segments);
        allSegments.addAll(childSegments);
        return new Namespace(allSegments);
    }

    public List<String> resolveProperty(String propertyName) {

        validateSegment(propertyName);
        List<String> property = new ArrayList<>(segments);
        property.add(propertyName);
        return property;
    }

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
        return Objects.equals(segments, namespace.segments);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(segments);
    }

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

    @Override
    public int compareTo(Namespace o) {

        if (o == null) {
            return 1;
        }
        return name.compareTo(o.name);
    }
}
