/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("lena.java-conventions")
    application
}

tasks.register<JavaExec>("exec") {
    group = "run examples"
    classpath = sourceSets.main.get().runtimeClasspath

    jvmArgs("-XX:+EnableDynamicAgentLoading")
}
