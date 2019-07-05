@file:Suppress("MemberVisibilityCanBePrivate")

const val KOTLIN_VERSION = "1.3.40"
const val ANDROIDX_NAVIGATION_VERSION = "2.0.0"
const val PLAYSERVICE_LICENSE_VERSION = "0.9.1"

object BuildDeps {
    const val ANDROID_GP = "com.android.tools.build:gradle:3.5.0-beta05"
    const val KOTLIN_GP = "org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION"
    const val KOTLIN_ANDROID_EXT = "org.jetbrains.kotlin:kotlin-android-extensions:$KOTLIN_VERSION"
    const val BEN_MANES = "com.github.ben-manes:gradle-versions-plugin:0.21.0"
    const val GMS = "com.google.gms:google-services:4.2.0"
    const val FABRIC_IO = "io.fabric.tools:gradle:1.28.1"
    const val SAFE_ARGS = "androidx.navigation:navigation-safe-args-gradle-plugin:2.0.0"

}

object Deps {
    // Android (https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-master-dev/buildSrc/src/main/kotlin/androidx/build/dependencies/Dependencies.kt)
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:1.0.2"
    const val ANDROIDX_ANNOTATIONS = "androidx.annotation:annotation:1.0.2"
    const val ANDROIDX_RECYCLERVIEW = "androidx.recyclerview:recyclerview:1.0.0"
    const val ANDROIDX_PREFERENCE = "androidx.preference:preference-ktx:1.0.0"
    const val ANDROIDX_PREFERENCE_LEGACY = "androidx.legacy:legacy-preference-v14:1.0.0"
    const val ANDROIDX_CARDVIEW = "androidx.cardview:cardview:1.0.0"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val ANDROIDX_CORE = "androidx.core:core-ktx:1.0.2"
    const val ANDROIDX_DYNAMIC_ANIMATION = "androidx.dynamicanimation:dynamicanimation:1.0.0"
    const val ANDROIDX_SUPPORT_LEGACY = "androidx.legacy:legacy-support-v13:1.0.0"
    const val ANDROID_FLEXBOX = "com.google.android:flexbox:1.1.0"

    const val ARCH_LIFECYCLE_EXT = "androidx.lifecycle:lifecycle-extensions:2.0.0"
    const val ARCH_LIFECYCLE_RUNTIME = "androidx.lifecycle:lifecycle-runtime:2.0.0"
    const val ARCH_LIFECYCLE_VIEWMODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.0.0"
    const val ARCH_LIFECYCLE_VIEWMODEL_SAVED_STATE =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:1.0.0-alpha01"
    const val ARCH_LIFECYCLE_COMMON = "androidx.lifecycle:lifecycle-common-java8:2.0.0"

    const val ARCH_NAVIGATION_FRAGMENT =
        "androidx.navigation:navigation-fragment-ktx:$ANDROIDX_NAVIGATION_VERSION"
    const val ARCH_NAVIGATION_UI =
        "androidx.navigation:navigation-ui-ktx:$ANDROIDX_NAVIGATION_VERSION"

    const val ARCH_WORK_RUNTIME = "androidx.work:work-runtime-ktx:2.0.1"
    const val WORKMANAGER_TOOLS = "org.dbtools:workmanager-tools:1.7.9"

    const val ARCH_PAGING_RUNTIME =
        "androidx.paging:paging-runtime:2.0.0"   // Don"t use 2.1.0, wait for 2.2.0

    const val ANDROID_MATERIAL = "com.google.android.material:material:1.1.0-alpha07"
    const val ANDROID_MULTIDEX = "androidx.multidex:multidex:2.0.1"
    const val ANDROID_MULTIDEX_INSTRUMENTATION = "androidx.multidex:multidex-instrumentation:2.0.0"

    const val PLAYSERVICE_VISION = "com.google.android.gms:play-services-vision:17.0.2"
    const val PLAYSERVICE_LICENSES =
        "com.google.android.gms:play-services-oss-licenses:$PLAYSERVICE_LICENSE_VERSION"
    const val PLAYSERVICE_CAST_FRAMEWORK =
        "com.google.android.gms:play-services-cast-framework:16.2.0"
    const val PLAYSERVICE_LOCATION = "com.google.android.gms:play-services-location:16.0.0"
    const val PLAYSERVICE_GCM = "com.google.android.gms:play-services-gcm:16.1.0"

    const val FIREBASE_AUTH = "com.google.firebase:firebase-auth:17.0.0"
    const val FIREBASE_CORE = "com.google.firebase:firebase-core:16.0.9"
    const val FIREBASE_CONFIG = "com.google.firebase:firebase-config:17.0.0"
    const val FIREBASE_FIRESTORE = "com.google.firebase:firebase-firestore:19.0.2"
    const val FIREBASE_MESSAGING = "com.google.firebase:firebase-messaging:18.0.0"
    const val FIREBASE_PERF = "com.google.firebase:firebase-perf:16.2.3"
    const val FIREBASE_STORAGE = "com.google.firebase:firebase-storage:17.0.0"

    // Code
    private const val COROUTINES_VERSION = "1.2.1"
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$COROUTINES_VERSION"
    private const val DAGGER_VERSION = "2.23.1"
    const val DAGGER = "com.google.dagger:dagger:$DAGGER_VERSION"
    const val DAGGER_COMPILER = "com.google.dagger:dagger-compiler:$DAGGER_VERSION"
    const val KOIN = "org.koin:koin-androidx-viewmodel:2.0.0"
    const val EASY_PERMISSIONS = "pub.devrel:easypermissions:3.0.0"
    const val EXTRAS_DELEGATES = "me.eugeniomarletti:android-extras-delegates:1.0.5"
    @Deprecated("Use KOTLIN_SERIALIZATION")
    const val GSON = "com.google.code.gson:gson:2.8.5"
    @Deprecated("Use KOTLIN_RETROFIT_CONVERTER")
    const val GSON_RETROFIT = "com.squareup.retrofit2:converter-gson:2.5.0"
    const val JSOUP = "org.jsoup:jsoup:1.12.1"
    const val KOTLIN_RETROFIT_CONVERTER =
        "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.4.0"
    const val KOTLIN_STD_LIB = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KOTLIN_VERSION"
    const val KOTLIN_SERIALIZATION = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.0"
    const val DEBUG_DB = "com.amitshekhar.android:debug-db:1.0.6"
    const val PRETTY_TIME = "org.ocpsoft.prettytime:prettytime:4.0.2.Final"
    const val STETHO = "com.facebook.stetho:stetho:1.5.1"
    const val STETHO_OKHTTP = "com.facebook.stetho:stetho-okhttp3:1.5.1"
    const val THREETEN_ABP = "com.jakewharton.threetenabp:threetenabp:1.2.1"
    const val TIMBER = "com.jakewharton.timber:timber:4.7.1"
    const val VIEWMODEL_INJECT = "com.vikingsen.inject:viewmodel-inject:0.1.1"
    const val VIEWMODEL_INJECT_PROCESSOR = "com.vikingsen.inject:viewmodel-inject-processor:0.1.1"
    const val XZ = "org.tukaani:xz:1.8"

    // Analytics
    const val CRASHLYTICS = "com.crashlytics.sdk.android:crashlytics:2.10.1"
    const val LOCALYTICS = "com.localytics.android:library:5.5.0"

    // UI
    const val APP_INTRO = "com.github.paolorotolo:appintro:v5.1.0"
    const val BOTTOM_NAVIGATION = "com.ashokvarma.android:bottom-navigation-bar:2.1.0"
    private const val GLIDE_VERSION = "4.9.0"
    const val GLIDE = "com.github.bumptech.glide:glide:$GLIDE_VERSION"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:$GLIDE_VERSION"
    const val GLIDE_OKHTTP = "com.github.bumptech.glide:okhttp3-integration:$GLIDE_VERSION"
    private const val MATERIAL_DIALOGS_VERSION = "2.8.1"
    const val MATERIAL_DIALOGS_COLOR =
        "com.afollestad.material-dialogs:color:$MATERIAL_DIALOGS_VERSION"
    const val MATERIAL_DIALOGS_CORE =
        "com.afollestad.material-dialogs:core:$MATERIAL_DIALOGS_VERSION"
    const val MATERIAL_DIALOGS_DATETIME =
        "com.afollestad.material-dialogs:datetime:$MATERIAL_DIALOGS_VERSION"
    const val MATERIAL_DIALOGS_INPUT =
        "com.afollestad.material-dialogs:input:$MATERIAL_DIALOGS_VERSION"
    const val MATERIAL_DIALOGS_LIFECYCLE =
        "com.afollestad.material-dialogs:lifecycle:$MATERIAL_DIALOGS_VERSION"
    const val MATERIAL_SPINNER = "com.github.ganfra:material-spinner:2.0.0"
    const val RECYCLERVIEW_FASTSCROLL = "com.simplecityapps:recyclerview-fastscroll:2.0.0"
    const val MATERIAL_PROGRESSBAR = "me.zhanghai.android.materialprogressbar:library:1.6.1"
    const val PHOTO_VIEW = "com.github.chrisbanes:PhotoView:2.3.0"
    const val SLIDING_UP_PANEL = "com.sothree.slidinguppanel:library:3.4.0"

    // Database
    private const val ROOM_VERSION = "2.1.0"
    const val ARCH_ROOM_RUNTIME = "androidx.room:room-runtime:$ROOM_VERSION"
    const val ARCH_ROOM_KTX = "androidx.room:room-ktx:$ROOM_VERSION"
    const val ARCH_ROOM_COMPILER = "androidx.room:room-compiler:$ROOM_VERSION"
    private const val DBTOOLS_VERSION = "4.9.1"
    const val DBTOOLS_ROOM = "org.dbtools:dbtools-room:$DBTOOLS_VERSION"
    const val DBTOOLS_ROOM_SQLITE = "org.dbtools:dbtools-room-sqliteorg:$DBTOOLS_VERSION"
    const val LDS_SQLITE_ANDROID = "org.lds.sqlite:lds-sqlite-android:3.28.0"

    // Network
    private const val OKHTTP_VERSION = "3.14.2"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:$OKHTTP_VERSION"
    const val OKHTTP_LOGGING_INTERCEPTOR =
        "com.squareup.okhttp3:logging-interceptor:$OKHTTP_VERSION"

    // Debug
    private const val DEV_TOOLS_VERSION = "2.4.0"
    const val DEV_TOOLS = "org.lds.mobile:ldsmobile-devtools:$DEV_TOOLS_VERSION"
    const val DEV_TOOLS_NOOP = "org.lds.mobile:ldsmobile-devtools-no-op:$DEV_TOOLS_VERSION"
    private const val SQL_SCOUT_VERSION = "4.1"
    const val SQL_SCOUT = "com.idescout.sql:sqlscout-server:$SQL_SCOUT_VERSION"
    const val SQL_SCOUT_NOOP = "com.idescout.sql:sqlscout-server-noop:$SQL_SCOUT_VERSION"

    // Test - Integration
    private const val ESPRESSO_VERSION = "3.2.0"
    const val TEST_ESPRESSO_CORE = "androidx.test.espresso:espresso-core:$ESPRESSO_VERSION"
    const val TEST_ESPRESSO_CONTRIB = "androidx.test.espresso:espresso-contrib:$ESPRESSO_VERSION"
    const val TEST_ANDROIDX_CORE = "androidx.test:core:1.2.0"
    const val TEST_ANDROIDX_RUNNER = "androidx.test:runner:1.2.0"
    const val TEST_ANDROIDX_RULES = "androidx.test:rules:1.2.0"
    const val TEST_ANDROIDX_JUNIT = "androidx.test.ext:junit:1.1.1"
    const val TEST_ANDROIDX_ARCH_CORE = "androidx.arch.core:core-testing:2.0.0"

    const val TEST_ARCH_ROOM_TESTING = "androidx.room:room-testing:$ROOM_VERSION"
    const val TEST_DATA_PROVIDER = "com.tngtech.java:junit-dataprovider:1.13.1"
    const val TEST_DBTOOLS_ROOM_JDBC = "org.dbtools:dbtools-room-jdbc:$DBTOOLS_VERSION"
    private const val JUNIT_VERSION = "4.12"    //"5.4.1"
    // Test - JUnit
    const val TEST_JUNIT =
        "junit:junit:$JUNIT_VERSION"   //"org.junit.jupiter:junit-jupiter:$JUNIT_VERSION"
    const val TEST_JUNIT_ENGINE =
        "org.junit.vintage:junit-vintage-engine:5.4.1"    //"org.junit.jupiter:junit-jupiter-engine:$JUNIT_VERSION"
    const val TEST_OKHTTP_MOCKWEBSERVER = "com.squareup.okhttp3:mockwebserver:$OKHTTP_VERSION"
    const val TEST_KOTLIN_COROUTINES =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:$COROUTINES_VERSION"
    private const val MOCKITO_VERSION = "2.28.2"
    const val TEST_MOCKITO_CORE = "org.mockito:mockito-core:$MOCKITO_VERSION"
    const val TEST_MOCKITO_JUNIT = "org.mockito:mockito-junit-jupiter:$MOCKITO_VERSION"
    const val TEST_MOCKITO_KOTLIN = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0"
    const val TEST_ROBOLECTRIC = "org.robolectric:robolectric:4.3"
    const val TEST_SCREENGRAB = "tools.fastlane:screengrab:1.2.0"
    const val TEST_THREETENBP = "org.threeten:threetenbp:1.4.0"
    const val TEST_XERIAL_SQLITE = "org.xerial:sqlite-jdbc:3.27.2.1"
}