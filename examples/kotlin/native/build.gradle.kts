plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.giacomoferretti.odexpatcher.example.nativelib"
    compileSdk = 32

    defaultConfig {
        applicationId = "com.giacomoferretti.odexpatcher.example.nativelib"
        minSdk = 19
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    flavorDimensions += listOf("version", "abi")
    productFlavors {
        create("normal") {
            dimension = "version"

            applicationIdSuffix = ".normal"
            resValue("string", "app_name", "NORMAL")
            buildConfigField("String", "EXAMPLE_TEXT", "\"NORMAL using ABI \"")
            buildConfigField("float", "EXAMPLE_TEXT_SIZE", "16")
            buildConfigField("int", "EXAMPLE_TEXT_COLOR", "android.graphics.Color.BLACK")
            buildConfigField("int", "EXAMPLE_TEXT_STYLE", "android.graphics.Typeface.NORMAL")
            buildConfigField("String", "EXAMPLE_SQUARE_COLOR", "\"#2f86eb\"")
            buildConfigField("float", "EXAMPLE_SQUARE_SIZE", "100")
        }

        create("patched") {
            dimension = "version"

            applicationIdSuffix = ".patched"
            resValue("string", "app_name", "PATCHED")
            buildConfigField("String", "EXAMPLE_TEXT", "\"PATCHED using ABI \"")
            buildConfigField("float", "EXAMPLE_TEXT_SIZE", "16")
            buildConfigField("int", "EXAMPLE_TEXT_COLOR", "android.graphics.Color.BLACK")
            buildConfigField("int", "EXAMPLE_TEXT_STYLE", "android.graphics.Typeface.BOLD")
            buildConfigField("String", "EXAMPLE_SQUARE_COLOR", "\"#44c95b\"")
            buildConfigField("float", "EXAMPLE_SQUARE_SIZE", "100")
        }

        create("arm") {
            dimension = "abi"
            versionNameSuffix = "-arm"
            ndk {
                abiFilters += "armeabi-v7a"
            }
        }

        create("arm64") {
            dimension = "abi"
            versionNameSuffix = "-arm64"
            ndk {
                abiFilters += "arm64-v8a"
            }
        }

        create("x86") {
            dimension = "abi"
            versionNameSuffix = "-x86"
            ndk {
                abiFilters += "x86"
            }
        }

        create("x86_64") {
            dimension = "abi"
            versionNameSuffix = "-x86_64"
            ndk {
                abiFilters += "x86_64"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.18.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    // No dependencies :)
}
