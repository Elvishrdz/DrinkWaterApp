plugins {
    id("com.android.library")
    id("drinkwater.android.library")
    id("drinkwater.detekt")
}

android {
    namespace = "com.eahm.drinkwaterapp.age"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.play.age.signals)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(kotlin("test-junit"))
    testImplementation(libs.kotlinx.coroutines.test)
}
