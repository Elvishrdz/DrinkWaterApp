import com.android.build.api.dsl.ApplicationExtension
import com.eahm.drinkwaterapp.buildlogic.extension.JAVA_VERSION
import com.eahm.drinkwaterapp.buildlogic.extension.MIN_SDK
import com.eahm.drinkwaterapp.buildlogic.extension.TARGET_SDK
import com.eahm.drinkwaterapp.buildlogic.extension.TEST_INSTRUMENTATION_RUNNER
import com.eahm.drinkwaterapp.buildlogic.extension.applyAndroidCommonConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {
                applyAndroidCommonConfig()
                applyDefaultConfig()
                applyCompileOptions()
            }
        }
    }
}

private fun ApplicationExtension.applyDefaultConfig() {
    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
    }
}

private fun ApplicationExtension.applyCompileOptions() {
    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
}
