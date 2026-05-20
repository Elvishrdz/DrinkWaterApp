import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildTypesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val props = localProperties

            extensions.configure<ApplicationExtension> {
                signingConfigs {
                    create("release") {
                        storeFile = System.getenv("KEYSTORE_PATH")
                            .takeIf { !it.isNullOrBlank() }
                            ?.let { file(it) }
                            ?: rootProject.file("keystore/drink_water_app_keystore.jks")

                        storePassword = System.getenv("KEYSTORE_PASSWORD")
                            ?: props.getProperty("keystore.password", "")

                        keyAlias = System.getenv("KEY_ALIAS")
                            ?: props.getProperty("key.alias", "")

                        keyPassword = System.getenv("KEY_PASSWORD")
                            ?: props.getProperty("key.password", "")
                    }
                }

                buildTypes {
                    BuildVariant.entries.forEach { variant ->
                        @Suppress("UNUSED_VARIABLE")
                        val handled = when (variant) {
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
                                    signingConfig =
                                        signingConfigs.getByName(BuildVariant.DEBUG.buildTypeName)
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
