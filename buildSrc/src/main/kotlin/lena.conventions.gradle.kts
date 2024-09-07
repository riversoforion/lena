/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    `java-library`
    // semver
}

group = "org.riversoforion.lena"
version = "0.1.0"

repositories {
    mavenCentral()
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
