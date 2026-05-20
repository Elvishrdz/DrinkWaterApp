plugins {
    id("com.android.library")
    id("drinkwater.android.library")
    id("drinkwater.android.compose")
}

android {
    namespace = "com.eahm.drinkwaterapp.ads"

    buildTypes {
        release {
            val bannerId = "ca-app-pub-9403893294190125/8776486293"
            buildConfigField("String", "ADS_BANNER_UNIT_ID", "\"$bannerId\"")
        }
        create("staging") {
            val bannerId = "ca-app-pub-3940256099942544/9214589741"
            buildConfigField("String", "ADS_BANNER_UNIT_ID", "\"$bannerId\"")
        }
        create("qa") {
            val bannerId = "ca-app-pub-3940256099942544/9214589741"
            buildConfigField("String", "ADS_BANNER_UNIT_ID", "\"$bannerId\"")
        }
        debug {
            val bannerId = "ca-app-pub-3940256099942544/9214589741"
            buildConfigField("String", "ADS_BANNER_UNIT_ID", "\"$bannerId\"")
        }
    }

    buildFeatures.buildConfig = true
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Admob
    implementation("com.google.android.gms:play-services-ads:25.2.0")

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
