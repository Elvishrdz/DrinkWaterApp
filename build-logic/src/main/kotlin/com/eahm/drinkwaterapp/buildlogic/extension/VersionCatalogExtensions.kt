@file:Suppress("NewApi")

package com.eahm.drinkwaterapp.buildlogic.extension

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val Project.androidCompileSdk: Int
    get() = libs.findVersion("androidCompileSdk").get().requiredVersion.toInt()

internal val Project.androidMinSdk: Int
    get() = libs.findVersion("androidMinSdk").get().requiredVersion.toInt()

internal val Project.androidTargetSdk: Int
    get() = libs.findVersion("androidTargetSdk").get().requiredVersion.toInt()

internal val Project.androidVersionCode: Int
    get() = libs.findVersion("androidVersionCode").get().requiredVersion.toInt()

internal val Project.androidVersionName: String
    get() = libs.findVersion("androidVersionName").get().requiredVersion
