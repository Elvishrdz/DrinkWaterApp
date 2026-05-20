# Convention Plugins — Implementation Plan

**Date:** 2026-05-20
**Branch:** development
**Scope:** Create `build-logic` with three convention plugins. Do NOT apply them to any module yet.

---

## Context

The project currently has no `build-logic` folder. All Gradle configuration lives inline inside each module's `build.gradle.kts`. The goal is to extract repeating and related config into standalone convention plugins so they can be applied à la carte later.

Three plugins are in scope for this plan:

| Plugin ID | Purpose |
|---|---|
| `drinkwater.android.compose` | Configures Compose compiler, feature flag, and all Compose dependencies |
| `drinkwater.android.buildtypes` | Defines the four build types for the `:compose` app module |
| `drinkwater.android.buildconfig` | Enables `buildFeatures.buildConfig` and defines all `buildConfigField` entries per variant |

---

## Project Constraints

- Gradle 9.4.1 / AGP 9.1.1 / Kotlin 2.2.20
- Root `build.gradle` and `settings.gradle` are **Groovy DSL** — they stay as-is. `pluginManagement` in a Groovy `settings.gradle` supports `includeBuild`, so no migration is needed for this plan.
- Module build files (`:compose`, `:ads`, `:analytics`) already use **Kotlin DSL**.
- No existing `build-logic` or `includeBuild` setup.
- Version catalog lives at `gradle/libs.versions.toml`.
- `local.properties` stays at the project root. The `LocalProperties` helper inside `build-logic` reads it via `rootProject.file("local.properties")`. The file is never moved.

---

## Step 1 — Scaffold `build-logic`

Create the composite build that will host all convention plugins.

### Files to create

```
build-logic/
  settings.gradle.kts
  build.gradle.kts
  src/main/kotlin/
    BuildVariant.kt
    LocalProperties.kt
    AndroidComposeConventionPlugin.kt
    AndroidBuildTypesConventionPlugin.kt
    AndroidBuildConfigConventionPlugin.kt
```

### `build-logic/settings.gradle.kts`

```kotlin
rootProject.name = "build-logic"
```

### `build-logic/build.gradle.kts`

```kotlin
plugins {
    `kotlin-dsl`
}

group = "com.eahm.drinkwaterapp.buildlogic"

dependencies {
    compileOnly(libs.gradle)                        // AGP
    compileOnly(libs.kotlin.gradle.plugin)          // Kotlin Gradle Plugin
    compileOnly(libs.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidCompose") {
            id = "drinkwater.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidBuildTypes") {
            id = "drinkwater.android.buildtypes"
            implementationClass = "AndroidBuildTypesConventionPlugin"
        }
        register("androidBuildConfig") {
            id = "drinkwater.android.buildconfig"
            implementationClass = "AndroidBuildConfigConventionPlugin"
        }
    }
}
```

> **Version catalog access:** In `build-logic/build.gradle.kts`, `libs` refers to the root project's version catalog automatically (Gradle 9 composite build support). Inside plugin source files (`.kt`), the catalog is NOT auto-available — it must be accessed via `VersionCatalogsExtension`. See the Compose plugin step for the pattern.

### Wire into root `settings.gradle` (Groovy)

Add at the top of the existing `settings.gradle`:

```groovy
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

---

## Step 2 — Version Catalog Updates (`gradle/libs.versions.toml`)

The Compose plugin will add Compose dependencies programmatically. Since plugin source files access the catalog via `VersionCatalogsExtension`, all Compose libraries need to be in the catalog first.

### Versions to add

```toml
[versions]
composeBom = "2025.12.00"
activityCompose = "1.13.0"
```

### Libraries to add

```toml
[libraries]
# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }

# Compose — no version needed, managed by BOM
compose-ui                              = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics                     = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling-preview              = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-tooling                      = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-test-manifest                = { group = "androidx.compose.ui", name = "ui-test-manifest" }
compose-ui-test-junit4                  = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-text-google-fonts            = { group = "androidx.compose.ui", name = "ui-text-google-fonts" }
compose-material3                       = { group = "androidx.compose.material3", name = "material3" }
compose-material3-adaptive-nav-suite    = { group = "androidx.compose.material3", name = "material3-adaptive-navigation-suite" }
compose-material3-adaptive              = { group = "androidx.compose.material3.adaptive", name = "adaptive" }
compose-material3-adaptive-layout       = { group = "androidx.compose.material3.adaptive", name = "adaptive-layout" }
compose-material3-adaptive-navigation   = { group = "androidx.compose.material3.adaptive", name = "adaptive-navigation" }

# Activity Compose
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
```

> TOML key `compose-bom` becomes `compose.bom` when accessed via `libs.findLibrary("compose.bom")` in plugin source.

---

## Step 3 — `BuildVariant` enum

Single source of truth for the four variants. All plugins iterate over `BuildVariant.entries` and use an **exhaustive `when` expression** — assigning the result to a `val` forces the Kotlin compiler to error if a new entry is added but not handled. This is the static analysis guarantee.

```kotlin
// build-logic/src/main/kotlin/BuildVariant.kt

enum class BuildVariant(val buildTypeName: String) {
    DEBUG("debug"),
    STAGING("staging"),
    QA("qa"),
    RELEASE("release");
}
```

### Exhaustive `when` pattern used in all plugins

```kotlin
BuildVariant.entries.forEach { variant ->
    val handled: Unit = when (variant) {
        BuildVariant.DEBUG   -> { /* configure debug */   }
        BuildVariant.STAGING -> { /* configure staging */ }
        BuildVariant.QA      -> { /* configure qa */      }
        BuildVariant.RELEASE -> { /* configure release */ }
        // Adding a new BuildVariant entry without a branch here → compile error
    }
}
```

The `val handled: Unit = when (variant) { ... }` assignment turns the `when` into an expression, which Kotlin requires to be exhaustive. No annotation or helper extension needed.

---

## Step 4 — `LocalProperties` helper

Shared `Project` extension read by both `AndroidBuildTypesConventionPlugin` and `AndroidBuildConfigConventionPlugin`. Reads `local.properties` from the project root (its current location).

```kotlin
// build-logic/src/main/kotlin/LocalProperties.kt

import org.gradle.api.Project
import java.util.Properties

val Project.localProperties: Properties
    get() = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) load(file.inputStream())
    }
```

---

## Step 5 — `AndroidComposeConventionPlugin`

Applies the Kotlin Compose compiler plugin, sets `buildFeatures.compose = true`, and adds all Compose dependencies. Each module type (app vs library) has its own explicit dependency block so it is always clear what a module receives when applying this plugin.

**App module** gets: BOM + all UI libs + `activity-compose` (needed for `setContent`, `ComponentActivity`).
**Library module** gets: BOM + all UI libs + `activity-compose` (feature modules need it for `rememberLauncherForActivityResult`, `LocalActivity`, etc.). The set is intentionally the same as app — if a future library module genuinely does not need a subset, that dep can be moved to the module's own `build.gradle.kts` instead of removing it from the plugin.

```kotlin
// build-logic/src/main/kotlin/AndroidComposeConventionPlugin.kt

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

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            val isApp     = pluginManager.hasPlugin("com.android.application")
            val isLibrary = pluginManager.hasPlugin("com.android.library")

            when {
                isApp -> {
                    extensions.configure<ApplicationExtension> {
                        buildFeatures.compose = true
                    }
                    dependencies {
                        val bom = platform(libs.findLibrary("compose.bom").get())
                        add("implementation", bom)
                        add("androidTestImplementation", bom)

                        // Activity host — required for setContent / ComponentActivity
                        add("implementation", libs.findLibrary("androidx.activity.compose").get())

                        // Core UI
                        add("implementation", libs.findLibrary("compose.ui").get())
                        add("implementation", libs.findLibrary("compose.ui.graphics").get())
                        add("implementation", libs.findLibrary("compose.ui.tooling.preview").get())
                        add("implementation", libs.findLibrary("compose.ui.text.google.fonts").get())

                        // Material3
                        add("implementation", libs.findLibrary("compose.material3").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive.nav.suite").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive.layout").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive.navigation").get())

                        // Tooling / testing
                        add("implementation", libs.findLibrary("compose.ui.tooling").get())
                        add("implementation", libs.findLibrary("compose.ui.test.manifest").get())
                        add("androidTestImplementation", libs.findLibrary("compose.ui.test.junit4").get())
                    }
                }

                isLibrary -> {
                    extensions.configure<LibraryExtension> {
                        buildFeatures.compose = true
                    }
                    dependencies {
                        val bom = platform(libs.findLibrary("compose.bom").get())
                        add("implementation", bom)
                        add("androidTestImplementation", bom)

                        // Activity host — feature library modules need this for
                        // rememberLauncherForActivityResult, LocalActivity, etc.
                        add("implementation", libs.findLibrary("androidx.activity.compose").get())

                        // Core UI
                        add("implementation", libs.findLibrary("compose.ui").get())
                        add("implementation", libs.findLibrary("compose.ui.graphics").get())
                        add("implementation", libs.findLibrary("compose.ui.tooling.preview").get())
                        add("implementation", libs.findLibrary("compose.ui.text.google.fonts").get())

                        // Material3
                        add("implementation", libs.findLibrary("compose.material3").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive.nav.suite").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive.layout").get())
                        add("implementation", libs.findLibrary("compose.material3.adaptive.navigation").get())

                        // Tooling / testing
                        add("implementation", libs.findLibrary("compose.ui.tooling").get())
                        add("implementation", libs.findLibrary("compose.ui.test.manifest").get())
                        add("androidTestImplementation", libs.findLibrary("compose.ui.test.junit4").get())
                    }
                }

                else -> error(
                    "AndroidComposeConventionPlugin requires either " +
                    "'com.android.application' or 'com.android.library' to be applied first."
                )
            }
        }
    }
}
```

> The catalog key lookup uses the TOML alias with `-` replaced by `.`. For example `compose-ui-graphics` → `"compose.ui.graphics"`. The exact strings depend on the aliases defined in Step 2.

---

## Step 6 — `AndroidBuildTypesConventionPlugin`

Defines all four build types by iterating `BuildVariant.entries` with an exhaustive `when`. The enum is the single source of truth for which variants exist — adding a fifth entry without handling it here is a **compile error**.

This plugin handles: `applicationIdSuffix`, `versionNameSuffix`, `initWith`, `matchingFallbacks`, signing config, `isMinifyEnabled`, and `proguardFiles`. No `buildConfigField` here.

```kotlin
// build-logic/src/main/kotlin/AndroidBuildTypesConventionPlugin.kt

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildTypesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val props = localProperties

            extensions.configure<ApplicationExtension> {

                signingConfigs {
                    create("release") {
                        storeFile = System.getenv("KEYSTORE_PATH")
                            .takeIf { !it.isNullOrBlank() }
                            ?.let { file(it) }
                            ?: rootProject.file("keystore/drink_water_app_keystore.jks")

                        storePassword = System.getenv("KEYSTORE_PASSWORD")
                            ?: props.getProperty("keystore.password", "")

                        keyAlias = System.getenv("KEY_ALIAS")
                            ?: props.getProperty("key.alias", "")

                        keyPassword = System.getenv("KEY_PASSWORD")
                            ?: props.getProperty("key.password", "")
                    }
                }

                buildTypes {
                    BuildVariant.entries.forEach { variant ->
                        val handled: Unit = when (variant) {

                            BuildVariant.DEBUG -> {
                                getByName(variant.buildTypeName) {
                                    applicationIdSuffix = ".dev"
                                    versionNameSuffix = "-dev"
                                }
                            }

                            BuildVariant.STAGING -> {
                                create(variant.buildTypeName) {
                                    initWith(getByName(BuildVariant.RELEASE.buildTypeName))
                                    matchingFallbacks += listOf(BuildVariant.RELEASE.buildTypeName)
                                    applicationIdSuffix = ".staging"
                                    versionNameSuffix = "-staging"
                                }
                            }

                            BuildVariant.QA -> {
                                create(variant.buildTypeName) {
                                    initWith(getByName(BuildVariant.DEBUG.buildTypeName))
                                    matchingFallbacks += listOf(BuildVariant.DEBUG.buildTypeName)
                                    signingConfig = signingConfigs.getByName(BuildVariant.DEBUG.buildTypeName)
                                    applicationIdSuffix = ".qa"
                                    versionNameSuffix = "-qa"
                                }
                            }

                            BuildVariant.RELEASE -> {
                                getByName(variant.buildTypeName) {
                                    signingConfig = signingConfigs.getByName("release")
                                    isMinifyEnabled = true
                                    proguardFiles(
                                        getDefaultProguardFile("proguard-android-optimize.txt"),
                                        "proguard-rules.pro"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

> `DEBUG` and `RELEASE` use `getByName()` because AGP creates them automatically. `STAGING` and `QA` use `create()` because they are custom build types.

---

## Step 7 — `AndroidBuildConfigConventionPlugin`

Enables `buildFeatures.buildConfig = true` and defines all `buildConfigField` entries. Uses the same exhaustive `when` over `BuildVariant.entries` — if a new variant is added to the enum, this file must be updated or the build fails.

```kotlin
// build-logic/src/main/kotlin/AndroidBuildConfigConventionPlugin.kt

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val props = localProperties

            val revenuecatDev  = props.getProperty("revenuecat.api.key.dev", "")
            val revenuecatProd = System.getenv("REVENUECAT_API_KEY")
                ?: props.getProperty("revenuecat.api.key.prod", "")

            extensions.configure<ApplicationExtension> {
                buildFeatures.buildConfig = true

                buildTypes {
                    BuildVariant.entries.forEach { variant ->
                        val handled: Unit = when (variant) {

                            BuildVariant.DEBUG -> getByName(variant.buildTypeName) {
                                buildConfigField("boolean", "IS_DEV",    "true")
                                buildConfigField("boolean", "IS_STAGING","false")
                                buildConfigField("boolean", "IS_QA",     "false")
                                buildConfigField("boolean", "LOGS_ENABLED",            "true")
                                buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                buildConfigField("boolean", "FEATURE_STREAKS",         "false")
                                buildConfigField("String",  "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
                            }

                            BuildVariant.STAGING -> getByName(variant.buildTypeName) {
                                buildConfigField("boolean", "IS_DEV",    "false")
                                buildConfigField("boolean", "IS_STAGING","true")
                                buildConfigField("boolean", "IS_QA",     "false")
                                buildConfigField("boolean", "LOGS_ENABLED",            "true")
                                buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                buildConfigField("boolean", "FEATURE_STREAKS",         "false")
                                buildConfigField("String",  "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
                            }

                            BuildVariant.QA -> getByName(variant.buildTypeName) {
                                buildConfigField("boolean", "IS_DEV",    "false")
                                buildConfigField("boolean", "IS_STAGING","false")
                                buildConfigField("boolean", "IS_QA",     "true")
                                buildConfigField("boolean", "LOGS_ENABLED",            "true")
                                buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                buildConfigField("boolean", "FEATURE_STREAKS",         "false")
                                buildConfigField("String",  "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
                            }

                            BuildVariant.RELEASE -> getByName(variant.buildTypeName) {
                                buildConfigField("boolean", "IS_DEV",    "false")
                                buildConfigField("boolean", "IS_STAGING","false")
                                buildConfigField("boolean", "IS_QA",     "false")
                                buildConfigField("boolean", "LOGS_ENABLED",            "false")
                                buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                                buildConfigField("boolean", "FEATURE_STREAKS",         "false")
                                buildConfigField("String",  "REVENUECAT_API_KEY", "\"$revenuecatProd\"")
                            }
                        }
                    }
                }
            }
        }
    }
}
```

> `STAGING` and `QA` use `getByName()` here (not `create()`) because `AndroidBuildTypesConventionPlugin` must be applied first and has already created them. See plugin application order below.

---

## Plugin application order (future reference)

When the plugins are eventually applied to `:compose`, the required order is:

```kotlin
plugins {
    id("com.android.application")
    id("com.android.built-in-kotlin")
    id("drinkwater.android.buildtypes")    // creates staging + qa first
    id("drinkwater.android.buildconfig")   // then configures fields on all four
    id("drinkwater.android.compose")       // compiler plugin + deps
    // ... other plugins (ksp, room, firebase, serialization)
}
```

For a library module (`:ads`, `:analytics`, future feature modules) that needs Compose:

```kotlin
plugins {
    id("com.android.library")
    id("drinkwater.android.compose")
}
```

---

## Decisions resolved

| # | Decision | Resolution |
|---|---|---|
| 1 | `BuildVariant` scope | Enum lives in `build-logic`, compile-time only, not on app runtime classpath. |
| 2 | Secrets location | `local.properties` stays at project root. Plugin reads via `rootProject.file("local.properties")`. |
| 3 | `LocalProperties` helper location | Shared `Project` extension in `build-logic/src/main/kotlin/LocalProperties.kt`. |
| 4 | Feature flags defaults | `FEATURE_REQUEST_FEATURE = true` on all variants. `FEATURE_STREAKS = false` on all variants. Update in one place when they change. |
| 5 | Groovy root build files | No migration needed. `pluginManagement { includeBuild("build-logic") }` works in Groovy `settings.gradle`. Migrate only if a future step specifically requires Kotlin DSL. |
| 6 | Compose deps scope | Compose plugin owns all Compose dependencies. Modules applying it do not need to re-declare them. |
