/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.riversoforion.lena.config.NameResolver;
import org.riversoforion.lena.config.Namespace;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class EnvironmentNameResolver implements NameResolver {

    private static final Pattern SANITIZER = Pattern.compile("[^a-zA-Z0-9]+");
    private static final Pattern WORD_FINDER = Pattern.compile("\\S+");
    private static final String SEPARATOR = "_";

    @Override
    public String resolveName(Namespace namespace, String name) {

        String sanitized = sanitize(name);
        NameResolver.validateNotEmpty(sanitized);
        List<String> parts = namespace.resolveProperty(sanitized);
        List<String> normalized = parts.stream().map(this::sanitize).flatMap(this::split).map(this::normalize).toList();
        return String.join(SEPARATOR, normalized);
    }

    protected String sanitize(String part) {

        return SANITIZER.matcher(Objects.requireNonNullElse(part, "")).replaceAll(" ").trim();
    }

    protected Stream<String> split(String part) {

        NameResolver.validateNotEmpty(part);
        return WORD_FINDER.matcher(part)
                          .results()
                          .map(MatchResult::group)
                          .peek(s -> {
                              if (s.isBlank()) {
                                  throw new IllegalArgumentException("Invalid environment variable name " + part);
                              }
                          });
    }

    private String normalize(String part) {

        return part.toUpperCase(Locale.ROOT);
    }
}
