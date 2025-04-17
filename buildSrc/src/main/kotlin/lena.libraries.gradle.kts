/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

plugins {
    id("lena.java-conventions")
    `java-library`
    `maven-publish`
    signing
    // semver
}

java {
    toolchain {
        withJavadocJar()
        withSourcesJar()
    }
}

tasks.javadoc {
    // Disable the warnings for missing documentation, for now. As we approach maturity, revisit.
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:-missing", true)
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
