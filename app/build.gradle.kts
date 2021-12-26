import java.io.ByteArrayOutputStream
import java.net.NetworkInterface

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.github.triplet.play")
    id("com.google.firebase.appdistribution")
}

val appVersionName = "1.0.3"
val appVersionCode = 6

val git_hash = "git rev-parse --short HEAD".runCommand()

val host_url = "http://${getLocalIPv4().hostAddress}:8081/"
//val host_url = "http://10.0.2.2:8080/"

val api_version = 1

val composeVersion = "1.0.5"
val okhttpVersion = "4.9.1"

android {
    compileSdk = 31
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "eu.sebaro.uller"
        minSdk = 23
        targetSdk = 31
        versionName = appVersionName
        versionCode = appVersionCode


        buildConfigField("String", "GIT_HASH", "\"$git_hash\"")
        buildConfigField("String", "HOST_URL", "\"$host_url\"")
        buildConfigField("Integer", "API_VERSION", "$api_version")
        buildConfigField("String", "APP_VERSION", "\"$appVersionName ($appVersionCode) $git_hash\"")
    }
    signingConfigs {
        create("release") {
            storeFile = file("upload-keystore.jks")
            storePassword = ""
            keyAlias = "upload"
            keyPassword = ""
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            release {
                firebaseAppDistribution {
                    appId = "1:540879684846:android:d5d8949a582939434e1fff"
                    artifactType = "AAB"
                    testers = "your@exampleemail.com, cerseimartell.772371@email.com"
                }
            }
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            firebaseAppDistribution {
                appId = "1:540879684846:android:d5d8949a582939434e1fff"
                artifactType = "AAB"
                testers = "your@exampleemail.com, cerseimartell.772371@email.com"
            }

        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
}

play {
    serviceAccountCredentials.set(file("Google Play Console Developer-a2d2f2b53d46.json"))
    track.set("internal")
    defaultToAppBundles.set(true)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation(platform ("com.google.firebase:firebase-bom:29.0.2"))
    implementation ("com.google.firebase:firebase-messaging-ktx")
    implementation ("com.google.firebase:firebase-analytics-ktx")
    // Firebase Cloud Messaging (Java)
    implementation ("com.google.firebase:firebase-messaging")
    implementation ("com.google.firebase:firebase-installations-ktx:17.0.0")
    implementation ("com.google.firebase:firebase-analytics")

    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.foundation:foundation-layout:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.activity:activity-compose:1.4.0")

    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0-RC")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")

    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.test.uiautomator:uiautomator:2.2.0")

    implementation("androidx.core:core-splashscreen:1.0.0-alpha02")

    implementation("androidx.navigation:navigation-compose:2.4.0-beta02")

    testImplementation("io.appium:java-client:7.6.0")
    // Required -- JUnit 4 framework
    testImplementation("junit:junit:4.13.2")
    // Optional -- Robolectric environment
    testImplementation("androidx.test:core:1.4.0")
    // Optional -- Mockito framework
    testImplementation("org.mockito:mockito-core:2.8.9")
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

fun getLocalIPv4(): java.net.Inet4Address =
    NetworkInterface.getNetworkInterfaces().toList()
        .filter { it.isUp && it.isLoopback.not() && it.isVirtual.not() }
        .flatMap { it.inetAddresses.toList() }
        .filterIsInstance<java.net.Inet4Address>()
        .first { it.isLoopbackAddress.not() }

