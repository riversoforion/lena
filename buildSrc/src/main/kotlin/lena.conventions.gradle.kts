/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    `java-library`
    `maven-publish`
    signing
    // semver
}

group = "com.riversoforion.lena"
version = "0.1.0"

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        withJavadocJar()
        withSourcesJar()
    }
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
}

tasks.named<Test>("test") {
    // -XX:+EnableDynamicAgentLoading : For ByteBuddy, used by Mockito/SystemStubs
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("lena") {
            from(components["java"])

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
    }

    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    sign(publishing.publications["lena"])
}
