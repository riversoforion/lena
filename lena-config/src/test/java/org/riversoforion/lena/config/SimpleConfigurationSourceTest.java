/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
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

    @DisplayName("getValue delegates to resolvers")
    @Test
    void getValue_DelegatesToResolvers(@Mock NameResolver names, @Mock ValueResolver values) {

        when(names.resolveName("this.prop")).thenReturn("THIS_PROP");
        when(values.resolveValue("THIS_PROP")).thenReturn(Optional.of("some_value"));

        SimpleConfigurationSource source = new SimpleConfigurationSource(names, values);
        Optional<String> value = source.getValue("this.prop");

        assertThat(value).contains("some_value");
        verify(names).resolveName("this.prop");
        verify(values).resolveValue("THIS_PROP");
    }
}
