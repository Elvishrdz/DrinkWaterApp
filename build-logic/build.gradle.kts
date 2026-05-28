plugins {
    `kotlin-dsl`
}

group = "com.eahm.drinkwaterapp.buildlogic"

dependencies {
    compileOnly(libs.gradle)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
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
        register("androidApplication") {
            id = "drinkwater.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidRoom") {
            id = "drinkwater.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidLibrary") {
            id = "drinkwater.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("detekt") {
            id = "drinkwater.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
