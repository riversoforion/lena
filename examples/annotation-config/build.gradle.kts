/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
plugins {
    id("lena.examples")
}

tasks.named<JavaExec>("exec") {
    mainClass = "com.riversoforion.lena.example.AnnotationConfigExample"
}

dependencies {
    implementation(project(":examples:common"))
    implementation(project(":lena-config"))
    annotationProcessor(project(":lena-config-annotation-processor"))

    testImplementation(junitPlatform.junitJupiter)
    testImplementation(libs.testing.systemStubs)
    testRuntimeOnly(junitPlatform.junitJupiterEngine)
}
