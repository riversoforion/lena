/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    `maven-publish`
}

group = "com.riversoforion.lena"
version = "0.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":lena-config"))
    compileOnly(libs.ksp.symbol.processing.api)
}

java {
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
