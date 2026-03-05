plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.normie69K.v_guard"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.normie69K.v_guard"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 1. Core Android & Compose
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.runtime.ktx.v270)
    implementation(libs.androidx.activity.compose.v182)
    implementation(platform(libs.androidx.compose.bom.v20240200))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // 2. Compose Navigation (Crucial for our AppNavigation.kt)
    implementation(libs.androidx.navigation.compose)

    // 3. Firebase (Using the BoM so we don't need to specify versions for each)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)       // For Login/Register
    implementation(libs.firebase.database)   // For Realtime ESP32 Data

    // 4. Google Maps & Location
    implementation(libs.maps.compose)          // Maps for Compose
    implementation(libs.play.services.maps)    // Base Maps SDK
    implementation(libs.play.services.location) // To get phone's location

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}