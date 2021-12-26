// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.github.triplet.play") version "3.5.0-agp7.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath(kotlin("serialization", version = "1.5.31"))
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-appdistribution-gradle:2.2.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

fun isNonStable(version: String): Boolean {
    val unStableKeyword = listOf("alpha", "beta", "rc", "cr", "m", "preview").any { version.contains(it, ignoreCase = true) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = unStableKeyword.not() || regex.matches(version)
    return isStable.not()
}

tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java).configure {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}