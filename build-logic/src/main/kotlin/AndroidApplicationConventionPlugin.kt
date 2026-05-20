import com.android.build.api.dsl.ApplicationExtension
import com.eahm.drinkwaterapp.buildlogic.extension.JAVA_VERSION
import com.eahm.drinkwaterapp.buildlogic.extension.TEST_INSTRUMENTATION_RUNNER
import com.eahm.drinkwaterapp.buildlogic.extension.androidMinSdk
import com.eahm.drinkwaterapp.buildlogic.extension.androidTargetSdk
import com.eahm.drinkwaterapp.buildlogic.extension.androidVersionCode
import com.eahm.drinkwaterapp.buildlogic.extension.androidVersionName
import com.eahm.drinkwaterapp.buildlogic.extension.applyAndroidCommonConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {
                applyAndroidCommonConfig(target)
                applyDefaultConfig(target)
                applyCompileOptions()
            }
        }
    }
}

private fun ApplicationExtension.applyDefaultConfig(project: Project) {
    defaultConfig {
        minSdk = project.androidMinSdk
        targetSdk = project.androidTargetSdk
        versionCode = project.androidVersionCode
        versionName = project.androidVersionName
        testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
    }
}

private fun ApplicationExtension.applyCompileOptions() {
    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
}
