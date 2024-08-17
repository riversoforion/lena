/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.riversoforion.lena.config.NameResolver;

import java.util.Optional;
import java.util.StringJoiner;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
abstract class PrefixedNameResolver implements NameResolver {

    protected final Optional<String> prefix;

    protected PrefixedNameResolver(String prefix) {

        this.prefix = Optional.ofNullable(normalize(prefix));
    }

    @Override
    public String resolveName(String name) {

        StringJoiner result = new StringJoiner(separator());
        prefix.ifPresent(result::add);
        return result.add(Optional.ofNullable(normalize(name))
                                  .filter(s -> !s.isEmpty())
                                  .orElseThrow(() -> new IllegalArgumentException(name)))
                     .toString();
    }

    protected abstract String separator();

    protected abstract String normalize(String raw);
}
