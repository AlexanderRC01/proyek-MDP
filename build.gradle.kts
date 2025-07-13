// Root-level build.gradle.kts (atau build.gradle jika pakai Groovy DSL)

plugins {
    alias(libs.plugins.android.application) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.7" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    kotlin("android") version "2.1.0" apply false
}

