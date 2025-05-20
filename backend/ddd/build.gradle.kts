plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "net.discdd.k9.backend.ddd"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.discdd.k9.backend.ddd"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    androidResources {
        namespaced = true
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
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.moshi)
    implementation(libs.client.adapter)
    // implementation(libs.androidx.lifecycle.process)
    ksp(libs.moshi.kotlin.codegen)

    api(projects.backend.api)
    api(projects.mail.common)

    testImplementation(projects.mail.testing)
}
