/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
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

    @Test
    @DisplayName("getValue from a single source")
    void getValue_SingleSource(@Mock ConfigurationSource source) {

        Namespace ns = Namespace.of("prefix");
        when(source.getValue(eq(ns), any())).thenReturn(Optional.empty());
        when(source.getValue(eq(ns), startsWith("existing"))).thenAnswer(invocation -> {
            String name = invocation.getArgument(1, String.class);
            return Optional.of(name + " value");
        });

        PrioritizedConfigurationSource prioritized = new PrioritizedConfigurationSource(List.of(source));

        assertThat(prioritized.getValue(ns, "existing num")).isPresent()
                                                            .contains("existing num value");
        assertThat(prioritized.getValue(ns, "existing str")).isPresent()
                                                            .contains("existing str value");
        assertThat(prioritized.getValue(ns, "missing num")).isEmpty();
    }

    @Test
    @DisplayName("getValue from multiple sources")
    void getValue_MultipleSources(@Mock ConfigurationSource first, @Mock ConfigurationSource second, @Mock ConfigurationSource third) {

        Namespace ns = Namespace.of("this", "that");
        // By default, value is "missing"
        when(first.getValue(eq(ns), any())).thenReturn(Optional.empty());
        when(second.getValue(eq(ns), any())).thenReturn(Optional.empty());
        when(third.getValue(eq(ns), any())).thenReturn(Optional.empty());
        // Test scenarios
        when(first.getValue(ns, "first num")).thenReturn(Optional.of("first value"));
        when(second.getValue(ns, "second bool")).thenReturn(Optional.of("second value"));
        when(third.getValue(ns, "third string")).thenReturn(Optional.of("third value"));

        PrioritizedConfigurationSource prioritized = new PrioritizedConfigurationSource(List.of(first, second, third));

        assertThat(prioritized.getValue(ns, "first num")).isPresent()
                                                         .contains("first value");
        assertThat(prioritized.getValue(ns, "second bool")).isPresent()
                                                           .contains("second value");
        assertThat(prioritized.getValue(ns, "third string")).isPresent()
                                                            .contains("third value");
        assertThat(prioritized.getValue(ns, "fourth num")).isEmpty();

        verify(first, times(4)).getValue(eq(ns), anyString());
        verify(second, times(3)).getValue(eq(ns), anyString());
        verify(third, times(2)).getValue(eq(ns), anyString());
    }
}
