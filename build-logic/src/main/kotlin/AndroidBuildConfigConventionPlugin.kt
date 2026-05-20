import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val props = localProperties

            val revenuecatDev = props.getProperty("revenuecat.api.key.dev", "")
            val revenuecatProd = System.getenv("REVENUECAT_API_KEY")
                ?: props.getProperty("revenuecat.api.key.prod", "")

            extensions.configure<ApplicationExtension> {
                buildFeatures.buildConfig = true

                buildTypes {
                    BuildVariant.entries.forEach { variant ->
                        @Suppress("UNUSED_VARIABLE")
                        val handled = when (variant) {
                            BuildVariant.DEBUG -> {
                                getByName(variant.buildTypeName) {
                                    buildConfigField("boolean", "IS_DEV", "true")
                                    buildConfigField("boolean", "IS_STAGING", "false")
                                    buildConfigField("boolean", "IS_QA", "false")
                                    buildConfigField("boolean", "LOGS_ENABLED", "true")
                                    buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                    buildConfigField("boolean", "FEATURE_STREAKS", "false")
                                    buildConfigField(
                                        "String",
                                        "REVENUECAT_API_KEY",
                                        "\"$revenuecatDev\""
                                    )
                                }
                            }

                            BuildVariant.STAGING -> {
                                getByName(variant.buildTypeName) {
                                    buildConfigField("boolean", "IS_DEV", "false")
                                    buildConfigField("boolean", "IS_STAGING", "true")
                                    buildConfigField("boolean", "IS_QA", "false")
                                    buildConfigField("boolean", "LOGS_ENABLED", "true")
                                    buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                    buildConfigField("boolean", "FEATURE_STREAKS", "false")
                                    buildConfigField(
                                        "String",
                                        "REVENUECAT_API_KEY",
                                        "\"$revenuecatDev\""
                                    )
                                }
                            }

                            BuildVariant.QA -> {
                                getByName(variant.buildTypeName) {
                                    buildConfigField("boolean", "IS_DEV", "false")
                                    buildConfigField("boolean", "IS_STAGING", "false")
                                    buildConfigField("boolean", "IS_QA", "true")
                                    buildConfigField("boolean", "LOGS_ENABLED", "true")
                                    buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                    buildConfigField("boolean", "FEATURE_STREAKS", "false")
                                    buildConfigField(
                                        "String",
                                        "REVENUECAT_API_KEY",
                                        "\"$revenuecatDev\""
                                    )
                                }
                            }

                            BuildVariant.RELEASE -> {
                                getByName(variant.buildTypeName) {
                                    buildConfigField("boolean", "IS_DEV", "false")
                                    buildConfigField("boolean", "IS_STAGING", "false")
                                    buildConfigField("boolean", "IS_QA", "false")
                                    buildConfigField("boolean", "LOGS_ENABLED", "false")
                                    buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                    buildConfigField("boolean", "FEATURE_STREAKS", "false")
                                    buildConfigField(
                                        "String",
                                        "REVENUECAT_API_KEY",
                                        "\"$revenuecatProd\""
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
