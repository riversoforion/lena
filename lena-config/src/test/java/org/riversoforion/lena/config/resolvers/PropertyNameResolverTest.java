/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.riversoforion.lena.config.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PropertyNameResolverTest {

    @ParameterizedTest(name = "{0}+{1} -> {2}")
    @CsvSource(textBlock = """
                           /, myProp, myProp
                           /, my.otherProp, my.otherProp
                           /, prop-name, prop-name
                           /my-prefix, myProp, my-prefix.myProp
                           /my/prefix, other-prop, my.prefix.other-prop
                           /my/prefix, other.prop, my.prefix.other.prop
                           /, name+with#funky?Characters!, name+with#funky?Characters!
                           /My Prefix, %some$Other@PROP, My Prefix.%some$Other@PROP
                           /Another/Prefix, .valid, Another.Prefix..valid
                           /, .how.about.this?, .how.about.this?
                           /, ., .
                           /It $eem$, property..names.are...Very\tFlexible, It $eem$.property..names.are...Very\tFlexible
                           /, #Not.a.Comment, #Not.a.Comment
                           """)
    @DisplayName("resolveName with valid scenarios")
    void resolveName_Valid(String namespace, String name, String expected) throws IOException {

        Properties testProps = new Properties();
        testProps.load(testProperties());
        Namespace ns = Namespace.parse(namespace);
        PropertyNameResolver resolver = new PropertyNameResolver();

        String result = resolver.resolveName(ns, name);

        assertThat(result).isEqualTo(expected);
        // Make sure that property names we think are valid can actually be used to look up a property
        assertThat(testProps).containsKey(expected);
    }

    @ParameterizedTest(name = "{0}+{1}")
    @CsvSource(textBlock = """
                           /,
                           /, ''
                           /,
                           /, '  '
                           /my-prefix,
                           /my-prefix, ''
                           /my-prefix,
                           /my-prefix, ''
                           """)
    @DisplayName("resolveName with invalid scenarios")
    void resolveName_Invalid(String namespace, String name) {

        Namespace ns = Namespace.parse(namespace);
        PropertyNameResolver resolver = new PropertyNameResolver();

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> resolver.resolveName(ns, name));
    }

    @Test
    @DisplayName("resolveName with additional names")
    void resolveName_AdditionalNames() {

        Namespace ns = Namespace.parse("my-prefix");
        PropertyNameResolver resolver = new PropertyNameResolver();

        String result = resolver.resolveName(ns, "my-name", "Another Name", "4th+Name");
        assertThat(result).isEqualTo("my-prefix.my-name.Another Name.4th+Name");
    }

    private InputStream testProperties() {

        return this.getClass().getResourceAsStream("/properties/prop-name-resolver.properties");
    }
}
