/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("lena.conventions")
    // semver
}

dependencies {
    testImplementation(junitPlatform.junitJupiter)
    testImplementation(assertJPlatform.assertjCore)
    testImplementation(mockitoPlatform.mockitoJunitJupiter)
    testImplementation(libs.testing.systemStubs)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}
