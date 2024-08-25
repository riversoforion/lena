/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemStubsExtension.class)
class SystemPropertiesValueResolverTest {

    @SystemStub
    private final SystemProperties sysProps = new SystemProperties();
    private final SystemPropertiesValueResolver resolver = new SystemPropertiesValueResolver();

    @DisplayName("resolveValue finds existing system properties")
    @Test
    void resolveValue_Found() throws Exception {

        sysProps.set("river.name", "Lena")
                .set("river.country", "RU")
                .execute(() -> {
                    assertThat(resolver.resolveValue("river.name")).contains("Lena");
                    assertThat(resolver.resolveValue("river.country")).contains("RU");
                });
    }

    @DisplayName("resolveValue is empty for non-existent system properties")
    @Test
    void resolveValue_NotFound() throws Exception {

        sysProps.execute(() -> {
            assertThat(resolver.resolveValue("river.name")).isEmpty();
        });
    }
}
