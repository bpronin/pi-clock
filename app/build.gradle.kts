import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone
import kotlin.apply

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

    testOptions.unitTests.isReturnDefaultValues = true

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

/* Custom tasks */

tasks.register("updateReleaseInfo") {
    val file = file("src/main/assets/release.properties")

    doFirst {
        Properties().apply {
            file.reader().use(::load)

            val n = getProperty("build_number").toInt() + 1
            setProperty("build_number", n.toString())

            SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").apply {
                timeZone = TimeZone.getTimeZone("UTC")
                setProperty("build_time", format(Date()))
            }
            file.writer().use { store(it, "Release properties") }

            println("New build number: $n")
        }
    }
}

tasks.register<Copy>("uploadDebug") {
    from(
        fileTree(layout.buildDirectory.dir("outputs/apk/debug")).files
    )
    into(localProperties.getProperty("upload_path"))
    include("**/*.apk")
}

tasks.register<Copy>("uploadRelease") {
    from(
        fileTree(layout.buildDirectory.dir("outputs/apk/release")).files
    )
    into(localProperties.getProperty("upload_path"))
    include("**/*.apk")
}

tasks.named("preBuild") {
    finalizedBy("updateReleaseInfo")
}

afterEvaluate {
    tasks.named("assembleDebug") {
        finalizedBy("uploadDebug")
    }
    tasks.named("assembleRelease") {
        finalizedBy("uploadRelease")
    }
}