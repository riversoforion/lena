/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
plugins {
    id("lena.examples")
    alias(libs.plugins.ksp)
}

application {
    mainClass = "com.riversoforion.lena.example.SimpleConfigExample"
}

dependencies {
    add("ksp", project(":lena-config-ksp"))
    implementation(project(":examples:common"))
    implementation(project(":lena-config"))
    implementation(project(":lena-config-api"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.systemStubs.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
