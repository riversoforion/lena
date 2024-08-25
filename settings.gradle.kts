/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

import dev.aga.gradle.versioncatalogs.Generator.generate
import dev.aga.gradle.versioncatalogs.GeneratorConfig

plugins {
    id("dev.aga.gradle.version-catalog-generator") version("2.0.0-beta.1")
}

rootProject.name = "lena"
include("lena-config", "lena-config-annotation-processor")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        // Testing
        generate("junitPlatform") {
            from(toml("junit-bom"))
            aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
        }
        generate("assertJPlatform") {
            from(toml("assertj-bom"))
            aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
        }
        generate("mockitoPlatform") {
            from(toml("mockito-bom"))
            aliasPrefixGenerator = GeneratorConfig.NO_PREFIX
        }
    }
}
