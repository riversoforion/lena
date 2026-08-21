/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // Native targets
    macosArm64 {
        binaries.executable {
            baseName = "simple-config-kotlin"
            entryPoint = "com.riversoforion.lena.example.main"
        }
    }
    linuxX64 {
        binaries.executable {
            baseName = "simple-config-kotlin"
            entryPoint = "com.riversoforion.lena.example.main"
        }
    }
    linuxArm64 {
        binaries.executable {
            baseName = "simple-config-kotlin"
            entryPoint = "com.riversoforion.lena.example.main"
        }
    }
    mingwX64 {
        binaries.executable {
            baseName = "simple-config-kotlin"
            entryPoint = "com.riversoforion.lena.example.main"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lena-config"))
                implementation(project(":lena-config-api"))
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":lena-config-ksp"))
    add("kspJvm", project(":lena-config-ksp"))
    add("kspMacosArm64", project(":lena-config-ksp"))
    add("kspLinuxX64", project(":lena-config-ksp"))
    add("kspLinuxArm64", project(":lena-config-ksp"))
    add("kspMingwX64", project(":lena-config-ksp"))
}

// Add a convenience task to run the JVM version
tasks.register<JavaExec>("runJvm") {
    group = "application"
    mainClass.set("com.riversoforion.lena.example.SimpleConfigExampleKt")
    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    classpath = jvmTarget.compilations.getByName("main").output.allOutputs + configurations.getByName("jvmRuntimeClasspath")
}
