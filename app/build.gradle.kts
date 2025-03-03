plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "com.osman.eczanemnerede"
    compileSdk = 35

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }
    packagingOptions {
        resources {
            excludes += setOf("META-INF/DEPENDENCIES")
        }
    }
    defaultConfig {
        applicationId = "com.osman.eczanemnerede"
        minSdk = 24
        targetSdk = 35
        versionCode = 31
        versionName = "2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation ("com.opencsv:opencsv:5.5.2")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
    implementation ("com.rmtheis:tess-two:9.1.0")
    implementation ("com.google.android.gms:play-services-ads:24.0.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation ("org.tensorflow:tensorflow-lite:2.7.0")

    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation ("org.jsoup:jsoup:1.15.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.compose.ui:ui-text-android:1.7.8")

    testImplementation("junit:junit:4.13.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}