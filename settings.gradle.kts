/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */

import dev.aga.gradle.versioncatalogs.Generator.generate
import dev.aga.gradle.versioncatalogs.GeneratorConfig

plugins {
    id("dev.aga.gradle.version-catalog-generator") version ("2.0.0-beta.2")
}

rootProject.name = "lena"
include(
    "lena-config",
    "lena-config-annotation-processor",
    "examples:common",
    "examples:simple-config",
    "examples:nested-config",
    "examples:annotation-config",
)

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage") repositories {
        mavenCentral()
    }
    versionCatalogs { // Testing
        generate("junitPlatform") {
            from(toml("testing-junit-bom"))
            aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
        }
        generate("assertJPlatform") {
            from(toml("testing-assertj-bom"))
            aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
        }
        generate("mockitoPlatform") {
            from(toml("testing-mockito-bom"))
            aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
        }
    }
}

include("examples:annotation-config")
