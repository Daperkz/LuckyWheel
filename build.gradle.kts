// =============================================================================
// LuckyWheel - Java Minecraft Plugin
// Copyright (c) 2026 Daperkz
//
// Gradle build configuration
// =============================================================================

import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.api.tasks.bundling.Jar

plugins {
    java
}

group = "com.daperkz"
version = "1.2.3"

val javaVersion = providers.gradleProperty("javaVersion")
    .map(String::toInt)
    .orElse(25)
    .get()

val defaultPaperVersions = listOf(
    "1.20.6" to "1.20.6-R0.1-SNAPSHOT",
    "1.21" to "1.21-R0.1-SNAPSHOT",
    "1.21.1" to "1.21.1-R0.1-SNAPSHOT",
    "1.21.4" to "1.21.4-R0.1-SNAPSHOT",
    "26.1" to "26.1.2.build.74-stable",
    "26.2" to "26.2.build.124-stable",
    "26.3" to "26.3.build.8-alpha"
)

val paperVersions = providers.gradleProperty("paperVersions")
    .map { value ->
        value.split(",").map { entry ->
            val parts = entry.trim().split(":", limit = 2)
            val minecraftVersion = parts.first()
            minecraftVersion to (parts.getOrNull(1) ?: "$minecraftVersion-R0.1-SNAPSHOT")
        }
    }
    .orElse(defaultPaperVersions)
    .get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaVersion
}

val mainSourceSet = sourceSets.named("main").get()
val javaToolchains = extensions.getByType<JavaToolchainService>()
val versionJarTasks = paperVersions.map { (minecraftVersion, paperApiVersion) ->
    val taskSuffix = minecraftVersion.replace(Regex("[^A-Za-z0-9]"), "_")
    val configurationName = "paperApi$taskSuffix"
    val apiConfiguration = configurations.create(configurationName)
    dependencies.add(apiConfiguration.name, "io.papermc.paper:paper-api:$paperApiVersion")
    dependencies.add(apiConfiguration.name, "org.jetbrains:annotations:26.0.2")
    val targetJavaVersion = if (minecraftVersion.startsWith("26.")) 25 else 21

    val compileTask = tasks.register<JavaCompile>("compileJava$taskSuffix") {
        description = "Compile against Paper API $paperApiVersion for Minecraft $minecraftVersion."
        source = mainSourceSet.java
        classpath = apiConfiguration
        destinationDirectory.set(layout.buildDirectory.dir("classes/java/$taskSuffix"))
        javaCompiler.set(javaToolchains.compilerFor(java.toolchain))
        options.encoding = "UTF-8"
        options.release = targetJavaVersion
    }

    val jarTask = tasks.register<Jar>("jar$taskSuffix") {
        description = "Package the Minecraft $minecraftVersion plugin JAR."
        dependsOn(compileTask)
        archiveBaseName.set("LuckyWheel-$minecraftVersion")
        archiveVersion.set(project.version.toString())
        from(compileTask)
        from(mainSourceSet.resources) {
            expand("project" to mapOf("version" to project.version))
        }
    }

    jarTask
}

tasks.register("compileAllVersions") {
    group = "build"
    description = "Compile the plugin against every configured Paper API version."
    dependsOn(versionJarTasks.map { it.name.replace("jar", "compileJava") })
}

tasks.register("buildAllVersions") {
    group = "build"
    description = "Build one plugin JAR for every configured Minecraft version."
    dependsOn(versionJarTasks)
}

tasks.named("build") {
    dependsOn("buildAllVersions")
}

tasks.named<Jar>("jar") {
    enabled = false
}
