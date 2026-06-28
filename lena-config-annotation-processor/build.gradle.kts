/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("lena.libraries")
    // semver
}

dependencies {
    implementation(libs.aptk)
    implementation(libs.aptkProcessor)
    implementation(project(":lena-config"))
    annotationProcessor(libs.aptkProcessor)

    testImplementation(junitPlatform.junitJupiter)
    testImplementation(assertJPlatform.assertjCore)
    testImplementation(mockitoPlatform.mockitoJunitJupiter)
    testImplementation(libs.testing.systemStubs)
    testImplementation(libs.testing.cute)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}
