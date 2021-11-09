/*
 * Copyright 2020-2021 Giacomo Ferretti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.application")
    id("com.google.android.gms.oss-licenses-plugin")
    kotlin("android")
}

android {
    compileSdk = Versions.COMPILE_SDK
    buildToolsVersion = Versions.BUILD_TOOLS

    defaultConfig {
        applicationId = "me.hexile.odexpatcher"
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        versionName = Versions.versionName
        versionCode = Versions.versionCode
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/*.version"
            excludes += "**/kotlin/**"
            excludes += "**/*.txt"
            excludes += "**/*.xml"
            excludes += "**/*.properties"

            // https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes += "DebugProbesKt.bin"
        }
    }

    buildFeatures {
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Main
    implementation("${Libs.KOTLIN_STDLIB}:${Versions.KOTLIN}")
    implementation("${Libs.CORE_KTX}:1.7.0")
    implementation("${Libs.APPCOMPAT}:1.3.1")
    implementation("${Libs.MATERIAL}:1.4.0")
    implementation("${Libs.CONSTRAINT_LAYOUT}:2.1.1")
    implementation("${Libs.NAVIGATION_FRAGMENT_KTX}:2.3.5")
    implementation("${Libs.NAVIGATION_UI_KTX}:2.3.5")
    implementation("${Libs.DATA_STORE_PREFERENCES}:1.0.0")
    implementation("${Libs.SWIPE_REFRESH_LAYOUT}:1.1.0")
    implementation("${Libs.PLAY_SERVICES_OSS_LICENSES}:17.0.0")
    implementation("${Libs.MULTIDEX}:2.0.1")
    implementation("${Libs.NAVIGATION_FRAGMENT_KTX}:2.3.5")
    implementation("${Libs.NAVIGATION_UI_KTX}:2.3.5")
    implementation("${Libs.LIFECYCLE_RUNTIME}:2.4.0")
    //implementation("${Libs.LIFECYCLE_VIEWMODEL}:2.4.0")
    //implementation("${Libs.LIFECYCLE_LIVEDATA}:2.4.0")
    //implementation("${Libs.LIFECYCLE_VIEWMODEL_SAVEDSTATE}:2.4.0")
    //implementation("${Libs.SPLASH_SCREEN}:1.0.0-alpha02")

    // libsu
    implementation("${Libs.LIBSU}:3.1.2")

    // Test
    testImplementation("${Libs.JUNIT}:4.13.2")
    androidTestImplementation("${Libs.EXT_JUNIT}:1.1.3")
    androidTestImplementation("${Libs.ESPRESSO_CORE}:3.4.0")

    //debugImplementation("${Libs.LEAKCANARY}:2.7")
}