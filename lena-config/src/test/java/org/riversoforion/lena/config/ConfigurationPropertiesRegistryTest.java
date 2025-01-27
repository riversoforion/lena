/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConfigurationPropertiesRegistryTest {

    @Mock
    private ConfigurationSource configurationSource;
    private final ConfigurationPropertiesRegistry registry = ConfigurationPropertiesRegistry.instance();

    @Test
    @DisplayName("registration and fetching")
    void registry() {

        Namespace root = Namespace.root();
        Namespace a = Namespace.of("parent");
        Namespace b = Namespace.of("another-parent");
        Namespace aa = a.child("child");
        Namespace bb = b.child("child");
        List<Namespace> namespaces = List.of(root, a, b, aa, bb);
        namespaces.forEach(ns -> new TestConfigurationProperties(configurationSource, ns));

        assertThat(namespaces).allMatch(ns -> {
            ConfigurationProperties props = registry.get(ns);
            return props.namespace().equals(ns);
        });
    }

    private static class TestConfigurationProperties extends ConfigurationProperties {

        protected TestConfigurationProperties(ConfigurationSource source, Namespace namespace) {

            super(source, namespace);
        }
    }
}
