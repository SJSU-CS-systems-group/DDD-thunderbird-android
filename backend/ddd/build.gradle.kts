plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}
//
android {
    namespace = "net.discdd.k9"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.discdd.k9"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(projects.backend.api)
    api(projects.mail.common)
    api(projects.mail.protocols.pop3)
    api(projects.mail.protocols.smtp)

    testImplementation(projects.mail.testing)
}
