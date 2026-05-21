import com.android.build.api.dsl.LibraryBuildType

plugins {
    id("com.android.library")
    id("drinkwater.android.library")
    id("drinkwater.android.compose")
}

android {
    namespace = "com.eahm.drinkwaterapp.ads"

    buildTypes {
        release {
            applyAdConfigurations()
        }

        create("staging") {
            applyAdTestConfiguration()
        }

        create("qa") {
            applyAdTestConfiguration()
        }

        debug {
            applyAdTestConfiguration()
        }
    }

    buildFeatures.buildConfig = true
}

private fun LibraryBuildType.applyAdConfigurations() {
    manifestPlaceholders["app_id"] = "ca-app-pub-9403893294190125~4239664850"

    val bannerBelowMainScreen = "ca-app-pub-9403893294190125/8776486293"
    val bannerHistoryScreenLog = "ca-app-pub-9403893294190125/8614957381"
    val bannerStatsScreenComponent = "ca-app-pub-9403893294190125/6751211378"
    val interstitialHistoryScreenEditLog = "ca-app-pub-9403893294190125/2975617897"

    buildConfigField("String", "BANNER_BELOW_MAIN_SCREEN", "\"$bannerBelowMainScreen\"")
    buildConfigField("String", "BANNER_HISTORY_SCREEN_LOGS", "\"$bannerHistoryScreenLog\"")
    buildConfigField("String", "BANNER_STATS_SCREEN_COMPONENT", "\"$bannerStatsScreenComponent\"")
    buildConfigField(
        "String",
        "INTERSTITIAL_HISTORY_SCREEN_EDIT",
        "\"$interstitialHistoryScreenEditLog\""
    )
}

private fun LibraryBuildType.applyAdTestConfiguration() {
    manifestPlaceholders["app_id"] = "ca-app-pub-3940256099942544~3347511713"

    val bannerTestId = "ca-app-pub-3940256099942544/9214589741"
    val interstitialTestId = "ca-app-pub-3940256099942544/1033173712"
    buildConfigField("String", "BANNER_BELOW_MAIN_SCREEN", "\"$bannerTestId\"")
    buildConfigField("String", "BANNER_HISTORY_SCREEN_LOGS", "\"$bannerTestId\"")
    buildConfigField("String", "BANNER_STATS_SCREEN_COMPONENT", "\"$bannerTestId\"")
    buildConfigField("String", "INTERSTITIAL_HISTORY_SCREEN_EDIT", "\"$interstitialTestId\"")
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Admob
    implementation(libs.play.services.ads)

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
