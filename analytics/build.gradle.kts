import java.util.Properties

plugins {
    id("com.android.library")

}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val mixpanelTokenDev: String = localProps.getProperty("mixpanel.token.dev", "")
val mixpanelTokenProd: String = System.getenv("MIXPANEL_TOKEN")
    ?: localProps.getProperty("mixpanel.token.prod", "")

android {
    namespace = "com.eahm.drinkwaterapp.analytics"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildTypes {
        release {
            buildConfigField("String", "MIXPANEL_TOKEN", "\"$mixpanelTokenProd\"")
        }
        debug {
            buildConfigField("String", "MIXPANEL_TOKEN", "\"$mixpanelTokenDev\"")
        }
        create("staging") {
            buildConfigField("String", "MIXPANEL_TOKEN", "\"$mixpanelTokenDev\"")
        }
    }

    buildFeatures.buildConfig = true
}

dependencies {
    // Mixpanel
    implementation("com.mixpanel.android:mixpanel-android:8.7.0")

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
}
