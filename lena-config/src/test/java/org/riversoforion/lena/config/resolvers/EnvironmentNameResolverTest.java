/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.riversoforion.lena.config.Namespace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EnvironmentNameResolverTest {

    @ParameterizedTest(name = "{0}+{1} -> {2}")
    @DisplayName("resolveName with valid scenarios")
    @CsvSource(textBlock = """
                           '', env_var, ENV_VAR
                           '', envVar, ENVVAR
                           '', foo.bar, FOO_BAR
                           the.namespace, the name, THE_NAMESPACE_THE_NAME
                           Namespace with space, name%with-weird+Characters!, NAMESPACE_WITH_SPACE_NAME_WITH_WEIRD_CHARACTERS
                           withNumbers6, IContain2Numbers31AndAQ, WITHNUMBERS6_ICONTAIN2NUMBERS31ANDAQ
                           my-ns, _some-name, MY_NS_SOME_NAME
                           my-ns, @some-name, MY_NS_SOME_NAME
                           PerfectlyValidNamespace, ValidName, PERFECTLYVALIDNAMESPACE_VALIDNAME
                           """)
    void resolveName_Valid(String namespace, String name, String expected) {

        Namespace ns = Namespace.parse(namespace);
        EnvironmentNameResolver resolver = new EnvironmentNameResolver();

        String actual = resolver.resolveName(ns, name);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}+{1}")
    @DisplayName("resolveName with invalid scenarios")
    @CsvSource(textBlock = """
                           /,
                           /, ''
                           /, -./*
                           /my/namespace,
                           /my/namespace, ''
                           /my/namespace, ()$%
                           PerfectlyValidNamespace, @*\\%
                           """)
    void resolveName_Invalid(String namespace, String name) {

        Namespace ns = Namespace.parse(namespace);
        EnvironmentNameResolver resolver = new EnvironmentNameResolver();

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> resolver.resolveName(ns, name));
    }

    @Test
    @DisplayName("resolveName with additional names")
    void resolveName_AdditionalNames() {

        Namespace ns = Namespace.parse("my-prefix");
        EnvironmentNameResolver resolver = new EnvironmentNameResolver();

        String result = resolver.resolveName(ns, "my-name", "_Another Name ", "4th+Name_");
        assertThat(result).isEqualTo("MY_PREFIX_MY_NAME_ANOTHER_NAME_4TH_NAME");
    }
}
