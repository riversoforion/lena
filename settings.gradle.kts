/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */

import dev.aga.gradle.versioncatalogs.Generator.generate
import dev.aga.gradle.versioncatalogs.GeneratorConfig

plugins {
    id("dev.aga.gradle.version-catalog-generator") version ("3.2.1")
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
            fromToml("testing-junit-bom")
            using {
                aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
            }
        }
        generate("assertJPlatform") {
            fromToml("testing-assertj-bom")
            using {
                aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
            }
        }
        generate("mockitoPlatform") {
            fromToml("testing-mockito-bom")
            using {
                aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
            }
        }
    }
}
