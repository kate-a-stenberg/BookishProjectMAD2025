import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    buildFeatures {viewBinding=true}
    namespace = "dev.kateastenberg.bookishproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.kateastenberg.bookishproject"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("api.key", "")}\"")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.room.runtime)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    annotationProcessor(libs.room.runtime)
    implementation ("com.google.firebase:firebase-database:20.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("androidx.transition:transition:1.6.0")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("io.reactivex.rxjava3:rxjava:3.1.11")
    implementation("io.reactivex.rxjava3:rxandroid:3.1.11")

}