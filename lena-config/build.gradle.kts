/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("lena.kmp-library")
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lena-config-api"))
        }
        jvmTest.dependencies {
            implementation(libs.junit.jupiter)
            implementation(libs.assertj.core)
            implementation(libs.mockito.junit.jupiter)
            implementation(libs.systemStubs.jupiter)
            runtimeOnly(libs.junit.jupiter.engine)
            runtimeOnly(libs.junit.platform.launcher)
        }
    }
}

dependencies {
    add("kspJvm", project(":lena-config-ksp"))
    add("kspJvmTest", project(":lena-config-ksp"))
}
