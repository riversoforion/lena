/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PassThroughNameResolverTest {

    private final PassThroughNameResolver resolver = new PassThroughNameResolver();

    @DisplayName("resolveName returns input unchanged")
    @ParameterizedTest(name = "{0} -> {0}")
    @ValueSource(strings = { "test1", "another", "it's all good" })
    @NullAndEmptySource
    void resolveName_ReturnsInput(String input) {

        assertThat(resolver.resolveName(input)).isEqualTo(input);
    }
}
