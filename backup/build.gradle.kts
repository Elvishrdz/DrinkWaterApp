import java.util.Properties

plugins {
    id("com.android.library")
    id("drinkwater.android.library")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val dropboxAppKey: String = localProps.getProperty("dropbox.app.key", "")

android {
    namespace = "com.eahm.drinkwaterapp.backup"

    buildFeatures { buildConfig = true }

    defaultConfig {
        buildConfigField("String", "DROPBOX_APP_KEY", "\"$dropboxAppKey\"")
        manifestPlaceholders["dropboxAppKey"] = dropboxAppKey
    }
}

dependencies {
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.play.services.auth)
    implementation(libs.dropbox.android)
    implementation(libs.dropbox.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)
    implementation(libs.koin.androidx.workmanager)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
