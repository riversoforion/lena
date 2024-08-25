/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemStubsExtension.class)
class EnvironmentValueResolverTest {

    @SystemStub
    private final EnvironmentVariables envVars = new EnvironmentVariables();
    private final EnvironmentValueResolver resolver = new EnvironmentValueResolver();

    @DisplayName("resolveValue finds existing environment variables")
    @Test
    void resolveValue_Found() throws Exception {

        envVars.set("RIVER", "Lena")
               .and("COUNTRY", "RU")
               .execute(() -> {
                   assertThat(resolver.resolveValue("COUNTRY")).contains("RU");
                   assertThat(resolver.resolveValue("RIVER")).contains("Lena");
               });
    }

    @DisplayName("resolveValue is empty for non-existent environment variables")
    @Test
    void resolveValue_NotFound() throws Exception {

        envVars.execute(() -> {
            assertThat(resolver.resolveValue("RIVER")).isEmpty();
        });
    }
}
