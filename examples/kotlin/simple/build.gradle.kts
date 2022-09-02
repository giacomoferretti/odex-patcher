plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.giacomoferretti.odexpatcher.example.simple"
    compileSdk = 32

    defaultConfig {
        applicationId = "com.giacomoferretti.odexpatcher.example.simple"
        minSdk = 19
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("normal") {
            dimension = "version"

            applicationIdSuffix = ".normal"
            resValue("string", "app_name", "NORMAL")
            buildConfigField("String", "EXAMPLE_TEXT", "\"Hello, this is the NORMAL app.\"")
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
            buildConfigField("String", "EXAMPLE_TEXT", "\"Hello, this is the PATCHED app.\"")
            buildConfigField("float", "EXAMPLE_TEXT_SIZE", "16")
            buildConfigField("int", "EXAMPLE_TEXT_COLOR", "android.graphics.Color.BLACK")
            buildConfigField("int", "EXAMPLE_TEXT_STYLE", "android.graphics.Typeface.BOLD")
            buildConfigField("String", "EXAMPLE_SQUARE_COLOR", "\"#44c95b\"")
            buildConfigField("float", "EXAMPLE_SQUARE_SIZE", "100")
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
