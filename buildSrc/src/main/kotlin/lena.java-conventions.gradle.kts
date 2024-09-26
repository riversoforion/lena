/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    java
}

group = "com.riversoforion.lena"
version = "0.1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    // -XX:+EnableDynamicAgentLoading : For ByteBuddy, used by Mockito/SystemStubs
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    useJUnitPlatform()
}
