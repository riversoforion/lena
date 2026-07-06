/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    `maven-publish`
    signing
}

group = "com.riversoforion.lena"
version = "0.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    // Enforce explicit visibility & return types on the public API of a library.
    explicitApi()

    // Suppress beta warning for expect/actual on class-like declarations (objects, interfaces,
    // etc.). This feature is stable enough for use; the warning is noise.
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // Compile with JDK 21, but emit bytecode runnable on JVM 17+ (the minimum target).
    jvmToolchain(21)
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // Desktop-tier native targets (Kotlin Tier 1 + Tier 2; macosX64 deprecated).
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// The JVM target's test task: run JUnit 5 (Jupiter) and allow dynamic agent loading for
// Mockito/SystemStubs (ByteBuddy).
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

// The Kotlin Multiplatform plugin registers a publication per target automatically once
// `maven-publish` is applied. We just decorate the POMs and publish to a local repo for now.
publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            url = "https://github.com/riversoforion/lena"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "macdaddyaz"
                    name = "Eric McIntyre"
                    email = "mac@riversoforion.com"
                }
            }
            scm {
                connection = "scm:git:git://github.com/riversoforion/lena.git"
                developerConnection = "scm:git:ssh://github.com/riversoforion/lena.git"
                url = "https://github.com/riversoforion/lena"
            }
        }
    }
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    // Only required when actually publishing with signing keys present; keeps `build` green locally.
    isRequired = false
    sign(publishing.publications)
}
