plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.teambanjarahills.mydownlinemanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.teambanjarahills.mydownlinemanager"
        minSdk = 23
        targetSdk = 35
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "1.0.${System.getenv("GITHUB_RUN_NUMBER") ?: "1"}"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("MYDM_KEYSTORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = System.getenv("MYDM_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("MYDM_KEY_ALIAS")
                keyPassword = System.getenv("MYDM_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
