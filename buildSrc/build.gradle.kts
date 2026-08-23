/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
    mavenCentral()
}

dependencies {
    // Lets the precompiled convention plugins apply `org.jetbrains.kotlin.multiplatform`
    implementation(libs.kotlin.gradlePlugin)
    // Lets the precompiled convention plugins apply Detekt
    implementation(libs.detekt.gradlePlugin)
}
