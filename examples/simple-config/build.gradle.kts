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
    testImplementation(junitPlatform.junitJupiter)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}
