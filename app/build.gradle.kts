import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    //id("org.jetbrains.kotlin.android") version "1.9.22"  apply false
}

android {
    namespace = "com.example.sumppump3"
    compileSdk = 34


    buildFeatures {
        compose = true
        dataBinding = true

    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
    defaultConfig {
        applicationId = "com.example.sumppumpbeta3"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.02")

    implementation(composeBom)
    androidTestImplementation(composeBom)


    // Choose one of the following:
    // Material Design 3
    //implementation ("androidx.core:core:2.2.0") //this breaks EVERYTHING
    implementation("androidx.compose.material3:material3")
    implementation ("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.0.0")
}