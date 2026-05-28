import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class DetektConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                parallel = true
                config.setFrom(rootProject.files("detekt.yml"))
                // Per-module baseline so modules don't overwrite each other.
                baseline = file("detekt-baseline.xml")
            }

            @Suppress("NewApi")
            dependencies.add("detektPlugins", libs.findLibrary("detekt-formatting").get())
        }
    }
}
