import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidRoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("androidx.room")

            applyRoomSchemaDirectory()
            applyDependencies()
        }
    }
}

private fun Project.applyRoomSchemaDirectory() {
    extensions.configure<RoomExtension> {
        schemaDirectory("$projectDir/schemas")
    }
}

@Suppress("NewApi")
private fun Project.applyDependencies() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        add("implementation", libs.findLibrary("androidx.room.runtime").get())
        add("ksp", libs.findLibrary("androidx.room.compiler").get())
        add("annotationProcessor", libs.findLibrary("androidx.room.compiler").get())
        add("implementation", libs.findLibrary("androidx.room.ktx").get())
        add("implementation", libs.findLibrary("androidx.room.paging").get())
        add("testImplementation", libs.findLibrary("androidx.room.testing").get())
    }
}
