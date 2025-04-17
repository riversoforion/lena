/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("lena.examples")
}

dependencies {
    implementation(project(":examples:common"))
    implementation(project(":lena-config"))

    testImplementation(junitPlatform.junitJupiter)
    testImplementation(libs.testing.systemStubs)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}
