package com.eahm.drinkwaterapp.buildlogic.extension

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
internal val JAVA_VERSION = JavaVersion.VERSION_11

/**
 * Applies compileSdk — the only property shared at the CommonExtension level in AGP 9.x.
 * compileOptions is not available on CommonExtension in this version; each plugin sets it
 * directly using the shared JAVA_VERSION constant.
 */
internal fun CommonExtension.applyAndroidCommonConfig(project: Project) {
    compileSdk = project.androidCompileSdk
}
