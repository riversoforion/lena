/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private final Name name;
    private final String fullName;

    private Namespace(List<String> segments) {

        this.name = Name.forNamespace(segments);
        this.fullName = Name.SEPARATOR + name;
    }

    // Static factories

    public static Namespace root() {

        return of();
    }

    public static Namespace of(String... segments) {

        if (segments == null) {
            segments = new String[0];
        }
        return new Namespace(stream(segments).filter(Namespace::notEmpty).flatMap(Name::splitSegment).toList());
    }

    public static Namespace parse(String name) {

        String[] segments = name.split(Name.SEPARATOR);
        if (segments.length == 1 && segments[0].isEmpty()) {
            return root();
        }
        return new Namespace(asList(segments));
    }

    private static boolean notEmpty(String segment) {

        return segment != null && !segment.isBlank();
    }

    // Operations

    public String name() {

        return fullName;
    }

    public List<String> segments() {

        return name.segments();
    }

    public Namespace child(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Child namespace must contain at least one new segment");
        }
        if (name.startsWith(Name.SEPARATOR)) {
            return Namespace.parse(name);
        }
        List<String> childSegments = asList(name.split(Name.SEPARATOR));
        if (childSegments.isEmpty()) {
            throw new IllegalArgumentException("Child namespace must contain at least one new segment");
        }
        List<String> allSegments = new ArrayList<>(this.name.segments());
        allSegments.addAll(childSegments);
        return new Namespace(allSegments);
    }

    public List<String> resolveProperty(Name propName) {

        if (propName == null) {
            throw new IllegalArgumentException("Property name required");
        }
        List<String> property = new ArrayList<>(this.name.segments());
        property.addAll(propName.segments());
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
    public int compareTo(Namespace o) {

        if (o == null) {
            return 1;
        }
        return fullName.compareTo(o.fullName);
    }
}
