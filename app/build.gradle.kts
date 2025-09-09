plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    // Se añade el plugin kotlin-ksp para el procesador de anotaciones de Room
    id("com.google.devtools.ksp")
    // Se añade el plugin kotlin-parcelize para la interfaz Parcelable
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.gym4house"
    compileSdk = 34

    // EL BLOQUE REPOSITORIES HA SIDO ELIMINADO DE AQUÍ.
    // AHORA SE GESTIONA GLOBALMENTE EN settings.gradle.kts

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
    // 1. Firebase (BOM: define todas las versiones de Firebase)
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))

    // 2. Dependencias de Android y Jetpack
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // 3. Dependencias de Firebase (SIN VERSIONES ESPECÍFICAS)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx) // Ya estaba aquí
    implementation("com.google.firebase:firebase-storage-ktx") // ¡SIN VERSIÓN!

    // Dependencias de Room añadidas
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Fragment, Coroutines, Lifecycle, etc.
    implementation ("androidx.fragment:fragment-ktx:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // Glide y MPAndroidChart
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}