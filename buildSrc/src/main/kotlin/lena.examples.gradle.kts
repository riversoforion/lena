/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

group = "com.riversoforion.lena.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

// Prefixes owned by the JVM/OS/Gradle itself. Forwarding these to the example's JavaExec (e.g.
// `java.home`, which points at the Gradle daemon's own JDK) corrupts the child JVM — notably
// java.text.Normalizer's ICU data lookup, which reads java.home to find its bundled resources.
private val standardJvmPropertyPrefixes = listOf(
    "java.", "sun.", "jdk.", "os.", "user.", "file.", "path.", "line.",
    "native.", "stdout.", "stderr.", "awt.", "http.", "https.", "ftp.",
    "socksProxy", "socksNonProxyHosts", "gopherProxySet",
)

// `application` registers `run`; examples are documented (README, CLAUDE.md) as using `exec`, and
// forwarding user-supplied `-Dfoo=bar` / `-Pfoo=bar` values through to the example JVM is the
// whole point of running these by hand.
tasks.register<JavaExec>("exec") {
    group = "application"
    description = "Runs the example, forwarding user-supplied -D/-P properties from the Gradle invocation."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(project.provider { application.mainClass.get() })
    val forwardedSystemProperties = System.getProperties().entries
        .filter { (k, _) -> standardJvmPropertyPrefixes.none { prefix -> k.toString().startsWith(prefix) } }
        .associate { (k, v) -> k.toString() to v.toString() }
    systemProperties = forwardedSystemProperties + gradle.startParameter.projectProperties
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

dependencies {
    // Gradle's test task requires the platform launcher on the runtime classpath explicitly.
    // (buildSrc precompiled script plugins don't see the root `libs` catalog, hence the literal
    // coordinates here rather than `libs.junit.platform.launcher`.)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.0")
}
