plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    // Se añade el plugin kotlin-kapt para el procesador de anotaciones de Room
    kotlin("kapt")
}

android {
    namespace = "com.example.gym4house"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gym4house"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true // Añadir si quieres usar View Binding
    }
}

dependencies {
    // Firebase (BOM para gestionar versiones)
    implementation(platform("com.google.firebase:firebase-bom:32.8.1")) // O la versión que tengas
    // Asegúrate que la versión del BOM sea la que necesitas, a veces con Version Catalogs se maneja diferente.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Dependencias de Room añadidas
    val room_version = "2.6.1" // Asegúrate de que esta sea la versión que deseas
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Para usar Coroutines con Room

    // Fragment (si no lo tienes ya para Kotlin o Java)
    implementation ("androidx.fragment:fragment-ktx:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0") // Para lifecycleScope
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}