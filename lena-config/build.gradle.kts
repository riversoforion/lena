/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("lena.kmp-library")
}

kotlin {
    sourceSets {
        jvmTest.dependencies {
            implementation(libs.junit.jupiter)
            implementation(libs.assertj.core)
            implementation(libs.mockito.junit.jupiter)
            implementation(libs.systemStubs.jupiter)
            runtimeOnly(libs.junit.jupiter.engine)
        }
    }
}
