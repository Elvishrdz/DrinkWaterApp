plugins {
    id("com.android.application")
    id("com.android.built-in-kotlin")
    id("drinkwater.android.application")
    id("drinkwater.android.buildtypes")
    id("drinkwater.android.buildconfig")
    id("drinkwater.android.compose")
    id("drinkwater.android.room")
    id("drinkwater.detekt")

    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.eahm.drinkwaterapp"

    defaultConfig {
        applicationId = "com.eahm.drinkwaterapp"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "true")
        }
        getByName("staging") {
            buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "false")
        }
        getByName("qa") {
            buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "false")
        }
        release {
            buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "false")
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":ads"))
    implementation(project(":age"))
    implementation(project(":analytics"))
    implementation(project(":backup"))

    // Ktor — needed for BackupModule to wire GoogleDriveBackupProvider's HttpClient
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core.splashscreen)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

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

    // Firebase Crashlytics
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-crashlytics")
}
