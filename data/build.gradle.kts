plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.anuress.pokedex.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Koin (koin-android includes koin-core)
    implementation(libs.koin.android)

    // Jetpack Paging Runtime
    implementation(libs.androidx.paging.runtime.ktx)

    // Test dependencies (uncomment if you add unit tests here)
    // testImplementation(libs.junit)
}
