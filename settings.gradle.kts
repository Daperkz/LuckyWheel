// =============================================================================
// LuckyWheel - Java Minecraft Plugin
// Copyright (c) 2026 Daperkz
//
// Gradle project settings
// =============================================================================

import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
    }
}

rootProject.name = "LuckyWheel"
