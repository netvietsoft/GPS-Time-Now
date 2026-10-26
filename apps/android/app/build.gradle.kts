plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.nextai.camera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nextai.camera"
        minSdk = 26
        targetSdk = 35
        versionCode = 100028100
        versionName = "10.0.281"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-fvisibility=hidden", "-ffunction-sections", "-fdata-sections")
                arguments += listOf("-DANDROID_STL=c++_shared")
            }
        }

        buildConfigField("String", "BACKEND_EMULATOR_URL", "\"http://10.0.2.2:4580/nextai/v1/\"")
        buildConfigField("String", "BACKEND_LAN_URL", "\"http://192.168.1.100:4580/nextai/v1/\"")
        buildConfigField("String", "BACKEND_LOCAL_URL", "\"http://localhost:4580/nextai/v1/\"")
        buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ""
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core & Lifecycle
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose UI & Material 3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // CameraX (Live Viewfinder, Video Capture, Image Analysis)
    val cameraXVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-video:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")

    // Room Database (42 Tables)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Networking (Retrofit2 + OkHttp3 + Gson)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Coroutines & WorkManager (Background S3/Teamspace Sync)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Google Mobile Ads (Official Test Keys Only)
    implementation("com.google.android.gms:play-services-ads:23.5.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Lottie Compose & Coil Image Loading
    implementation("com.airbnb.android:lottie-compose:6.6.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Document Export (Apache POI Excel .xlsx & PDFBox)
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
