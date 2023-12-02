plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    aaptOptions.noCompress("eylul.pdf")
    aaptOptions.cruncherEnabled = false
    aaptOptions.useNewCruncher = false
    namespace = "com.osman.eczanemnerede"
    compileSdk = 33
    packagingOptions {
        exclude ("META-INF/DEPENDENCIES") // Exclude this specific path
        // You can add more exclusions or merges as needed
    }
    defaultConfig {
        applicationId = "com.osman.eczanemnerede"
        minSdk = 24
        targetSdk = 33
        versionCode = 12
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

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation ("com.opencsv:opencsv:5.5.2")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
    implementation ("com.rmtheis:tess-two:9.1.0")
    implementation ("com.google.android.gms:play-services-ads:22.5.0")

    implementation ("org.tensorflow:tensorflow-lite:2.7.0")

    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.compose.material3:material3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.0-alpha03")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    testImplementation("junit:junit:4.13.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}