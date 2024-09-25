/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
plugins {
    id("lena.examples")
}

tasks.named<JavaExec>("exec") {
    mainClass = "com.riversoforion.lena.example.SimpleConfigExample"
}

dependencies {
    implementation(project(":lena-config"))

    testImplementation(junitPlatform.junitJupiter)
    testImplementation(libs.testing.systemStubs)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}
