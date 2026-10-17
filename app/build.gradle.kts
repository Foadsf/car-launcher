plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.foadsf.carlauncher"
    // AndroidX core 1.19 / Compose 1.12 require compiling against API 37. This does not
    // change runtime behaviour: targetSdk below is what the phone (API 35) acts on.
    compileSdk {
        version = release(37) { minorApiLevel = 0 }
    }
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "io.github.foadsf.carlauncher"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    // Optional shared debug key: if `debug.keystore` exists next to settings.gradle.kts (it is
    // gitignored), every machine that has it signs debug builds identically, so they can update
    // an installed copy. Without it, AGP's per-machine ~/.android/debug.keystore is used.
    val sharedDebugKey = rootProject.file("debug.keystore")
    if (sharedDebugKey.exists()) {
        signingConfigs {
            getByName("debug") {
                storeFile = sharedDebugKey
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // No Google-encrypted dependency blob in the APK.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
}
