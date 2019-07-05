plugins {
    id("com.android.application")
    id("com.github.ben-manes.versions") // ./gradlew dependencyUpdates -Drevision=release
    id("kotlin-android")
    id("kotlin-android-extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.troy.mine"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SCHEMA_PATH", "\"schema\"")   //used to test database migrations

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.schemaLocation" to "$projectDir/schema")    // used by Room to produce schema files
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(Deps.KOTLIN_STD_LIB)
    implementation(Deps.COROUTINES)

    implementation(Deps.ANDROIDX_APPCOMPAT)
    implementation(Deps.ANDROIDX_CORE)
    implementation(Deps.ANDROIDX_SUPPORT_LEGACY)
    implementation(Deps.ARCH_NAVIGATION_FRAGMENT)
    implementation(Deps.ARCH_NAVIGATION_UI)
    implementation(Deps.ARCH_ROOM_RUNTIME)
    kapt(Deps.ARCH_ROOM_COMPILER)
    implementation(Deps.TIMBER)
    implementation(Deps.KOIN)

    testImplementation(Deps.TEST_JUNIT)
    testImplementation(Deps.TEST_ARCH_ROOM_TESTING)
    androidTestImplementation(Deps.TEST_ANDROIDX_RUNNER)
    androidTestImplementation(Deps.TEST_ESPRESSO_CORE)
}
