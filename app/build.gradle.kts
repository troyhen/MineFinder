plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.application")
    id("kotlin-android-extensions") // synthetics
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.troy.mine"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SCHEMA_PATH", "\"schema\"")   //used to test database migrations

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schema")
                argument("room.incremental", "true")
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.4.0-beta02")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.0-beta02")
    implementation("androidx.room:room-runtime:2.4.0-rc01")
    kapt("androidx.room:room-compiler:2.4.0-rc01")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("io.insert-koin:koin-android:3.1.4")
//    implementation("io.insert-koin:koin-androidx-viewmodel:3.1.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:2.4.0-rc01")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
