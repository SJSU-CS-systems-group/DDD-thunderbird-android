plugins {
    id(ThunderbirdPlugins.Library.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "net.discdd.k9.plugin.ddd"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.moshi)
    implementation(libs.client.adapter)
    //implementation(libs.androidx.lifecycle.process)
    ksp(libs.moshi.kotlin.codegen)

    api(projects.backend.api)
    api(projects.mail.common)

    testImplementation(projects.mail.testing)
}
