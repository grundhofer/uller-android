import java.io.ByteArrayOutputStream
import java.net.NetworkInterface

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.github.triplet.play")
}

val appVersionName = "1.0.3"
val appVersionCode = 6

val git_hash = "git rev-parse --short HEAD".runCommand()

// local url
//val host_url = "http://${getLocalIPv4().hostAddress}:8080/"
// production url
val host_url = "https://android-dm.dev.7p-group.com/"
val api_version = 1

val composeVersion = "1.0.0-rc02"
val okhttpVersion = "4.9.1"

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.amr_7p.devicemanagement"
        minSdk = 21
        targetSdk = 30
        versionName = appVersionName
        versionCode = appVersionCode


        buildConfigField("String", "GIT_HASH", "\"$git_hash\"")
        buildConfigField("String", "HOST_URL", "\"$host_url\"")
        buildConfigField("Integer", "API_VERSION", "$api_version")
        buildConfigField("String", "APP_VERSION", "\"$appVersionName ($appVersionCode) $git_hash\"")
    }
    signingConfigs {
        create("release") {
            storeFile = file("playstore-key.keystore")
            storePassword = "equai30oQue2piHU"
            keyAlias = "upload"
            keyPassword = "equai30oQue2piHU"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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

    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.activity:activity-compose:1.3.0-rc02")

    implementation("androidx.work:work-runtime-ktx:2.5.0")

    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
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
