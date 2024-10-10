plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.companyvihva.vihvawatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.companyvihva.vihvawatch"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Versão da biblioteca BOM do Compose e outras dependências essenciais
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Android Wear Compose
    implementation("androidx.wear.compose:compose-material:1.1.2")
    implementation("androidx.wear.compose:compose-foundation:1.1.2")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.7.2")

    // Core SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // AppCompat e Material
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Constraint Layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Play Services Wearable
    implementation("com.google.android.gms:play-services-wearable:18.2.0")

    // Dependências de Teste
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Firebase BOM para garantir versões consistentes das bibliotecas
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Samsung Health (HALF)
    implementation("com.samsung.android.sdk.healthdata:healthdata:2.5.0")
    implementation("com.samsung.android:shealth:1.0.0")

    // Health Services Client
    implementation("androidx.health:health-services-client:1.0.0-rc02")
}
