/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
plugins {
    id("lena.examples")
}

application {
    mainClass = "com.riversoforion.lena.example.NestedConfigExample"
}

dependencies {
    implementation(project(":examples:common"))
    implementation(project(":lena-config"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.systemStubs.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
