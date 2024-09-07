/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */

rootProject.name="lena-conventions"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
