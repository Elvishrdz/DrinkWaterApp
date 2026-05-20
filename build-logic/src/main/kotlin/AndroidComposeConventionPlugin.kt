import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val isApp = pluginManager.hasPlugin("com.android.application")
            val isLibrary = pluginManager.hasPlugin("com.android.library")

            when {
                isApp -> applyAppConfiguration()
                isLibrary -> applyLibraryConfiguration()
                else -> error("AndroidComposeConventionPlugin requires either 'com.android.application' or 'com.android.library' to be applied first.")
            }
        }
    }
}

private fun Project.applyAppConfiguration() {
    extensions.configure<ApplicationExtension> {
        buildFeatures.compose = true
    }

    applyDependencies()
}

private fun Project.applyLibraryConfiguration() {
    extensions.configure<LibraryExtension> {
        buildFeatures.compose = true
    }

    applyDependencies()
}

@Suppress("NewApi")
private fun Project.applyDependencies() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        val bom = platform(libs.findLibrary("compose.bom").get())
        add("implementation", bom)
        add("androidTestImplementation", bom)

        add("implementation", libs.findLibrary("androidx.activity.compose").get())
        add("implementation", libs.findLibrary("compose.ui").get())
        add("implementation", libs.findLibrary("compose.ui.graphics").get())
        add("implementation", libs.findLibrary("compose.ui.tooling.preview").get())
        add("implementation", libs.findLibrary("compose.ui.text.google.fonts").get())
        add("implementation", libs.findLibrary("compose.material3").get())
        add("implementation", libs.findLibrary("compose.material3.adaptive.nav.suite").get())
        add("implementation", libs.findLibrary("compose.material3.adaptive").get())
        add("implementation", libs.findLibrary("compose.material3.adaptive.layout").get())
        add("implementation", libs.findLibrary("compose.material3.adaptive.navigation").get())
        add("implementation", libs.findLibrary("compose.ui.tooling").get())
        add("implementation", libs.findLibrary("compose.ui.test.manifest").get())
        add("androidTestImplementation", libs.findLibrary("compose.ui.test.junit4").get())
    }
}
