/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("java-library")
    // semver
}

apply(from = rootProject.file("buildSrc/shared.gradle.kts"))
// modules
// propagate version

dependencies {
    testImplementation(junitPlatform.junitJupiter)
    testImplementation(assertJLibs.assertjCore)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
