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
    id("com.gtnewhorizons.gtnhsettingsconvention") version ("2.0.26")
    id("io.github.DancingSnow0517.gtnh-catalog.settings") version "1.0.1"
}

`gtnh-catalog` {
    create("gtnh290") {
        version = "2.9.0-beta-2"
    }
}
