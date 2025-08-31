// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Estas líneas son correctas para aplicar plugins de forma declarativa.
    // 'apply false' significa que el plugin se define aquí, pero se aplica en los módulos.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false


    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.devtools.ksp") version "2.2.20-RC-2.0.2" apply false // Versión de Kotlin Symbol Processing para Room (Kotlin 1.9.0)
    // Si tu versión de Kotlin es diferente, ajusta la de KSP.
    // Por ejemplo, para Kotlin 1.8.0, sería id("com.google.devtools.ksp") version "1.8.0-1.0.9" apply false
    // Puedes ver las versiones aquí: https://github.com/google/ksp/releases
}

