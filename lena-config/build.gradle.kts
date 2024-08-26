/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("java-library")
    // semver
}

apply(from = rootProject.file("buildSrc/shared.gradle.kts"))

dependencies {
    testImplementation(junitPlatform.junitJupiter)
    testImplementation(assertJPlatform.assertjCore)
    testImplementation(mockitoPlatform.mockitoJunitJupiter)
    testImplementation(libs.testing.systemStubs)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}

java {
    toolchain {
        version = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    // -XX:+EnableDynamicAgentLoading : For ByteBuddy, used by Mockito/SystemStubs
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    useJUnitPlatform()
}
