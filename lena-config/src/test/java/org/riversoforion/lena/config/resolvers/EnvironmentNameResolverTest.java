/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EnvironmentNameResolverTest {

    @DisplayName("resolveName with valid scenarios")
    @ParameterizedTest(name = "{0}+{1} -> {2}")
    @CsvSource(textBlock = """
                           , env_var, ENV_VAR
                           , envVar, ENV_VAR
                           '', foo.bar, FOO_BAR
                           the.prefix, the name, THE_PREFIX_THE_NAME
                           prefix with space, name%with-weird+Characters!, PREFIX_WITH_SPACE_NAME_WITH_WEIRD_CHARACTERS
                           withNumbers6, IContain2Numbers31AndAQ, WITH_NUMBERS6_I_CONTAIN2_NUMBERS31_AND_A_Q
                           """)
    void resolveName_Valid(String prefix, String name, String expected) {

        EnvironmentNameResolver resolver = new EnvironmentNameResolver(prefix);

        String actual = resolver.resolveName(name);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("resolveName with invalid scenarios")
    @ParameterizedTest(name = "{0}+{1}")
    @CsvSource(textBlock = """
                           ,
                           ,''
                           ,-./*
                           my.prefix,
                           my.prefix,''
                           my.prefix,()$%
                           """)
    void resolveName_Invalid(String prefix, String name) {

        EnvironmentNameResolver resolver = new EnvironmentNameResolver(prefix);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> resolver.resolveName(name));
    }
}
