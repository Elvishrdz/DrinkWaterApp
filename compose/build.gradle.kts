import java.util.Properties

plugins {
    id("com.android.application")
    id("com.android.built-in-kotlin")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"

    // Room
    id("com.google.devtools.ksp")
    id("androidx.room")
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val revenuecatApiKeyDev: String = localProps.getProperty("revenuecat.api.key.dev", "")
val revenuecatApiKeyProd: String = System.getenv("REVENUECAT_API_KEY")
    ?: localProps.getProperty("revenuecat.api.key.prod", "")

android {
    namespace = "com.eahm.drinkwaterapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.eahm.drinkwaterapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 19
        versionName = "2.0.0-alpha02"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {

            storeFile = System.getenv("KEYSTORE_PATH")
                .takeIf { !it.isNullOrBlank() }
                ?.let { file(it) }
                ?: rootProject.file("keystore/drink_water_app_keystore.jks")

            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: localProps.getProperty("keystore.password", "")

            keyAlias = System.getenv("KEY_ALIAS")
                ?: localProps.getProperty("key.alias", "")

            keyPassword = System.getenv("KEY_PASSWORD")
                ?: localProps.getProperty("key.password", "")
        }
    }

    buildTypes {
        // Local development — all features on, debug tools visible
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("boolean", "IS_DEV", "true")
            buildConfigField("boolean", "IS_STAGING", "false")
            buildConfigField("boolean", "LOGS_ENABLED", "true")
            buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
            buildConfigField("boolean", "FEATURE_STREAKS", "false")

            buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenuecatApiKeyDev\"")
        }

        // QA / Internal Testing track — beta features on, no dev tools
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("boolean", "IS_DEV", "false")
            buildConfigField("boolean", "IS_STAGING", "true")
            buildConfigField("boolean", "LOGS_ENABLED", "true")
            buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
            buildConfigField("boolean", "FEATURE_STREAKS", "false")

            buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenuecatApiKeyDev\"")
        }

        // Closed testing, open testing and production track
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEV", "false")
            buildConfigField("boolean", "IS_STAGING", "false")
            buildConfigField("boolean", "LOGS_ENABLED", "false")
            buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "false")
            buildConfigField("boolean", "FEATURE_STREAKS", "false")

            buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenuecatApiKeyProd\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures.compose = true
    buildFeatures.buildConfig = true
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":ads"))
    implementation(project(":analytics"))

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")

    // compose
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
    implementation("androidx.compose.material3.adaptive:adaptive")
    implementation("androidx.compose.material3.adaptive:adaptive-layout")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core.splashscreen)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)
    implementation(libs.koin.androidx.workmanager)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.koin.android.test)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.viewmodel.navigation)

    // DataStore
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    testImplementation(libs.androidx.room.testing)

    // Kotlin DateTime
    implementation(libs.kotlinx.datetime)

    // Kotlin Collections
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.5.0-beta01")

    implementation(libs.picktime.compose)

    // RevenueCat
    implementation("com.revenuecat.purchases:purchases:10.6.0")

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
}
