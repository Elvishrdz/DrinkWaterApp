import com.android.build.api.dsl.ApplicationExtension
import com.eahm.drinkwaterapp.buildlogic.domain.BuildVariant
import com.eahm.drinkwaterapp.buildlogic.extension.localProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidBuildTypesConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {
                applySigningConfigs(project = this@with)
                applyBuildTypes()
            }
        }
    }

}

private fun ApplicationExtension.applySigningConfigs(project: Project) {
    val properties = project.localProperties

    signingConfigs {
        create("release") {
            storeFile = System.getenv("KEYSTORE_PATH")
                .takeIf { !it.isNullOrBlank() }
                ?.let { project.file(it) }
                ?: project.rootProject.file("keystore/drink_water_app_keystore.jks")

            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: properties.getProperty("keystore.password", "")

            keyAlias = System.getenv("KEY_ALIAS")
                ?: properties.getProperty("key.alias", "")

            keyPassword = System.getenv("KEY_PASSWORD")
                ?: properties.getProperty("key.password", "")
        }
    }
}

private fun ApplicationExtension.applyBuildTypes() {
    buildTypes {
        BuildVariant.entries.forEach { variant ->
            when (variant) {
                BuildVariant.DEBUG -> {
                    getByName(variant.buildTypeName) {
                        applicationIdSuffix = ".dev"
                        versionNameSuffix = "-dev"
                    }
                }

                BuildVariant.STAGING -> {
                    create(variant.buildTypeName) {
                        initWith(getByName(BuildVariant.RELEASE.buildTypeName))
                        matchingFallbacks += listOf(BuildVariant.RELEASE.buildTypeName)
                        applicationIdSuffix = ".staging"
                        versionNameSuffix = "-staging"
                    }
                }

                BuildVariant.QA -> {
                    create(variant.buildTypeName) {
                        initWith(getByName(BuildVariant.DEBUG.buildTypeName))
                        matchingFallbacks += listOf(BuildVariant.DEBUG.buildTypeName)
                        signingConfig = signingConfigs.getByName(BuildVariant.DEBUG.buildTypeName)
                        applicationIdSuffix = ".qa"
                        versionNameSuffix = "-qa"
                    }
                }

                BuildVariant.RELEASE -> {
                    getByName(variant.buildTypeName) {
                        signingConfig = signingConfigs.getByName("release")
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                        ndk {
                            debugSymbolLevel = "SYMBOL_TABLE"
                        }
                    }
                }
            }
        }
    }
}
