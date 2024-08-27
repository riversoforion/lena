/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrioritizedConfigurationSourceTest {

    @DisplayName("getValue from a single source")
    @Test
    void getValue_SingleSource(@Mock ConfigurationSource source) {

        when(source.getValue(any())).thenReturn(Optional.empty());
        when(source.getValue(startsWith("existing"))).thenAnswer(invocation -> {
            String name = invocation.getArgument(0, String.class);
            return Optional.of(name + " value");
        });

        PrioritizedConfigurationSource prioritized = new PrioritizedConfigurationSource(List.of(source));

        assertThat(prioritized.getValue("existing num")).isPresent()
                                                        .contains("existing num value");
        assertThat(prioritized.getValue("existing str")).isPresent()
                                                        .contains("existing str value");
        assertThat(prioritized.getValue("missing num")).isEmpty();
    }

    @DisplayName("getValue from multiple sources")
    @Test
    void getValue_MultipleSources(@Mock ConfigurationSource first, @Mock ConfigurationSource second, @Mock ConfigurationSource third) {

        // By default, value is "missing"
        when(first.getValue(any())).thenReturn(Optional.empty());
        when(second.getValue(any())).thenReturn(Optional.empty());
        when(third.getValue(any())).thenReturn(Optional.empty());
        // Test scenarios
        when(first.getValue("first num")).thenReturn(Optional.of("first value"));
        when(second.getValue("second bool")).thenReturn(Optional.of("second value"));
        when(third.getValue("third string")).thenReturn(Optional.of("third value"));

        PrioritizedConfigurationSource prioritized = new PrioritizedConfigurationSource(List.of(first, second, third));

        assertThat(prioritized.getValue("first num")).isPresent()
                                                     .contains("first value");
        assertThat(prioritized.getValue("second bool")).isPresent()
                                                       .contains("second value");
        assertThat(prioritized.getValue("third string")).isPresent()
                                                        .contains("third value");
        assertThat(prioritized.getValue("fourth num")).isEmpty();

        verify(first, times(4)).getValue(anyString());
        verify(second, times(3)).getValue(anyString());
        verify(third, times(2)).getValue(anyString());
    }
}
