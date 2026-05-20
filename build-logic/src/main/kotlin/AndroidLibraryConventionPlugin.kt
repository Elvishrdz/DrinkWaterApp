import com.android.build.api.dsl.LibraryExtension
import com.eahm.drinkwaterapp.buildlogic.extension.JAVA_VERSION
import com.eahm.drinkwaterapp.buildlogic.extension.MIN_SDK
import com.eahm.drinkwaterapp.buildlogic.extension.TEST_INSTRUMENTATION_RUNNER
import com.eahm.drinkwaterapp.buildlogic.extension.applyAndroidCommonConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.configure<LibraryExtension> {
                applyAndroidCommonConfig()

                applyDefaultConfig()
                applyCompileOptions()
            }
        }
    }
}

private fun LibraryExtension.applyDefaultConfig() {
    defaultConfig {
        minSdk = MIN_SDK
        testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
        consumerProguardFiles("consumer-rules.pro")
    }
}

private fun LibraryExtension.applyCompileOptions() {
    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
}
