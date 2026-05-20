import com.android.build.api.dsl.ApplicationExtension
import com.eahm.drinkwaterapp.buildlogic.domain.BuildVariant
import com.eahm.drinkwaterapp.buildlogic.extension.localProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidBuildConfigConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {
                applyBuildTypes(project = this@with)

                buildFeatures.buildConfig = true
            }
        }
    }
}

private fun ApplicationExtension.applyBuildTypes(project: Project) {
    val properties = project.localProperties

    val revenuecatDev = properties.getProperty("revenuecat.api.key.dev", "")
    val revenuecatProd = System.getenv("REVENUECAT_API_KEY")
        ?: properties.getProperty("revenuecat.api.key.prod", "")

    buildTypes {
        BuildVariant.entries.forEach { variant ->
            when (variant) {
                BuildVariant.DEBUG -> {
                    getByName(variant.buildTypeName) {
                        buildConfigField("boolean", "IS_DEV", "true")
                        buildConfigField("boolean", "IS_STAGING", "false")
                        buildConfigField("boolean", "IS_QA", "false")
                        buildConfigField("boolean", "LOGS_ENABLED", "true")
                        buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                        buildConfigField("boolean", "FEATURE_STREAKS", "false")
                        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
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
                        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
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
                        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
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
                        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenuecatProd\"")
                    }
                }
            }
        }
    }
}
