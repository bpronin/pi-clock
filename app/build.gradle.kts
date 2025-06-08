import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties").let {
        if (it.exists()) it else rootProject.file(".github/workflows/local.properties")
    }

    file.reader().use(::load)
}

val keyStore = rootProject.file("keystore.p12").apply {
    if (!exists()) throw InvalidUserDataException("Required file $this")
}

android {
    namespace = "com.bopr.piclock"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bopr.piclock"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        base.archivesName = "piclock-$versionName"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            keyAlias = "sample"
            storeFile = keyStore
            storePassword = localProperties.getProperty("store_password")
            keyPassword = localProperties.getProperty("key_password")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            isShrinkResources = false
            isMinifyEnabled = false
            isDefault = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true
        }
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
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}