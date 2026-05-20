package com.eahm.drinkwaterapp.buildlogic.extension

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 24
internal const val TARGET_SDK = 37
internal const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
internal val JAVA_VERSION = JavaVersion.VERSION_11

/**
 * Applies the config that is identical across every Android module in this project:
 * compileSdk and compileOptions (source/target compatibility).
 *
 * Called from AndroidApplicationConventionPlugin and AndroidLibraryConventionPlugin.
 * Each plugin then handles its own defaultConfig using the shared constants above.
 */
/**
 * Applies compileSdk — the only property shared at the CommonExtension level in AGP 9.x.
 * compileOptions is not available on CommonExtension in this version; each plugin sets it
 * directly using the shared com.eahm.drinkwaterapp.buildlogic.extension.JAVA_VERSION constant.
 */
internal fun CommonExtension.applyAndroidCommonConfig() {
    compileSdk = COMPILE_SDK
}
