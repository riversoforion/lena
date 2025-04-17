/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleConfigurationSourceTest {

    @Test
    @DisplayName("getValue delegates to resolvers")
    void getValue_DelegatesToResolvers(@Mock NameResolver names, @Mock ValueResolver values) {

        Namespace root = Namespace.root();
        Name thisProp = Name.of("this.prop");
        when(names.resolveName(root, thisProp)).thenReturn("THIS_PROP");
        when(values.resolveValue("THIS_PROP")).thenReturn(Optional.of("some_value"));

        SimpleConfigurationSource source = new SimpleConfigurationSource(names, values);
        Optional<String> value = source.getValue(root, thisProp);

        assertThat(value).contains("some_value");
        verify(names).resolveName(root, thisProp);
        verify(values).resolveValue("THIS_PROP");
    }
}
