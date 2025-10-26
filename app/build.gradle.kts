import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Function to load properties from local.properties
fun getLocalProperty(key: String, projectRootDir: File): String {
    val properties = Properties()
    val localPropertiesFile = File(projectRootDir, "local.properties")
    if (localPropertiesFile.isFile) {
        properties.load(localPropertiesFile.inputStream())
        return properties.getProperty(key) ?: ""
    } else {
        println("Warning: local.properties file not found in project root. Mixpanel token will not be set.")
    }
    return "" // Return empty string if not found or file doesn't exist
}

android {
    namespace = "com.anuress.pokedex"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anuress.pokedex"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read Mixpanel token from local.properties and add to BuildConfig
        val mixpanelProjectToken = getLocalProperty("mixpanel.projectToken", rootProject.rootDir)
        buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"$mixpanelProjectToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // You might want to ensure the token is also available for release builds if needed
             val mixpanelProjectToken = getLocalProperty("mixpanel.projectToken", rootProject.rootDir)
             buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"$mixpanelProjectToken\"")
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            // BuildConfig fields are inherited from defaultConfig unless overridden
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
        buildConfig = true // Ensure buildConfig is enabled
    }
}

dependencies {
    implementation(project(":data")) // Added dependency on the :data module

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette.ktx)

    // Jetpack Navigation
    implementation(libs.androidx.navigation.compose) // Added Navigation Compose

    // Koin Dependencies (needed for UI layer and Application class)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Jetpack Paging Compose (for UI integration)
    implementation(libs.androidx.paging.compose)

    // Mixpanel
    implementation(libs.mixpanel)
    implementation(libs.mixpanel.session.replay) // Added Mixpanel Session Replay

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
