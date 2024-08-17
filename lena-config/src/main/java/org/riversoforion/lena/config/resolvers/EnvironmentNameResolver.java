/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentNameResolver extends PrefixedNameResolver {

    private static final Pattern WORD_FINDER = Pattern.compile("(([A-Z]?[a-z0-9]+)|([A-Z]))");
    private static final String SEPARATOR = "_";

    public EnvironmentNameResolver(String prefix) {

        super(prefix);
    }

    @Override
    protected String separator() {

        return SEPARATOR;
    }

    protected String normalize(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(SEPARATOR);
        Matcher matcher = WORD_FINDER.matcher(raw);
        while (matcher.find()) {
            joiner.add(matcher.group(0)
                              .toUpperCase(Locale.ROOT));
        }
        return joiner.toString();
    }
}
