
pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("com.gtnewhorizons.gtnhsettingsconvention") version("2.0.26")
    id("cn.elytra.gradle.conventions.settings") version "1.2.0-beta-1-g5e4b0b1.dirty"
}

elytra {
    versionCatalogs {
        create("gtnh290") {
            version = "2.9.0-beta-1"
        }
    }
}
