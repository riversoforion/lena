/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */

plugins {
    // Resolves/downloads JDK toolchains (e.g. JDK 21) on demand.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "lena"
include(
    "lena-config",
    "lena-config-api",
    "lena-config-ksp",
    "examples:common",
    "examples:nested-config-java",
    "examples:simple-config-java",
    "examples:nested-config-kotlin",
    "examples:simple-config-kotlin",
)

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}
