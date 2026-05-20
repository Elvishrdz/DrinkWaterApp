plugins {
    `kotlin-dsl`
}

group = "com.eahm.drinkwaterapp.buildlogic"

dependencies {
    compileOnly(libs.gradle)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidCompose") {
            id = "drinkwater.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidBuildTypes") {
            id = "drinkwater.android.buildtypes"
            implementationClass = "AndroidBuildTypesConventionPlugin"
        }
        register("androidBuildConfig") {
            id = "drinkwater.android.buildconfig"
            implementationClass = "AndroidBuildConfigConventionPlugin"
        }
    }
}
