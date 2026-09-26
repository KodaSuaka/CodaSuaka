plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.codasuaka"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.codasuaka"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"${project.findProperty("BASE_URL") ?: "https://codasuaka.my.id/"}\"")
    }

    buildTypes {
        debug {
            // Debug: no minification for faster builds
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isDebuggable = false
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit — networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Coroutines Android
    implementation(libs.kotlinx.coroutines.android)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // DataStore Preferences untuk token persistence
    implementation(libs.androidx.datastore.preferences)

    // Security Crypto untuk encrypted storage
    implementation(libs.androidx.security.crypto)

    // Koin - Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // OkHttp Logging Interceptor (untuk debug network)
    implementation(libs.okhttp.logging)
}