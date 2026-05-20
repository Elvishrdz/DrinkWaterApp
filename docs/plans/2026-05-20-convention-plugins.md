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
| `drinkwater.android.compose` | Configures Compose for both app and library modules |
| `drinkwater.android.buildtypes` | Defines the four build types for the `:compose` app module |
| `drinkwater.android.buildconfig` | Enables `buildFeatures.buildConfig` and defines all `buildConfigField` entries per variant |

---

## Project Constraints

- Gradle 9.4.1 / AGP 9.1.1 / Kotlin 2.2.20
- Root `build.gradle` is still **Groovy DSL**; `settings.gradle` is also Groovy.
- Module build files (`:compose`, `:ads`, `:analytics`) already use **Kotlin DSL**.
- No existing `build-logic` or `includeBuild` setup.
- Version catalog lives at `gradle/libs.versions.toml`.

---

## Step 1 — Scaffold `build-logic`

Create the composite build that will host all convention plugins.

### Files to create

```
build-logic/
  settings.gradle.kts          ← names the includeBuild, declares no extra repos
  build.gradle.kts             ← applies `kotlin-dsl`, declares dependencies on AGP + Kotlin gradle plugins
  src/main/kotlin/
    BuildVariant.kt            ← shared enum (see Step 2)
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
    compileOnly(libs.gradle)                      // AGP
    compileOnly(libs.kotlin.gradle.plugin)        // Kotlin Gradle Plugin
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

> **Note on version catalog access inside `build-logic`:** AGP 9 supports accessing the root project's version catalog from a composite build via `libs` in `build.gradle.kts`. We use this for the `compileOnly` deps above. The same catalog will NOT be auto-available inside the plugin source files — dependencies declared inside plugins must use string coordinates or be looked up explicitly. See Step 4.

### Wire into root `settings.gradle`

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

## Step 2 — `BuildVariant` enum

Shared enum visible to all plugins. Lets each plugin safely identify the active variant without magic strings.

```kotlin
// build-logic/src/main/kotlin/BuildVariant.kt

enum class BuildVariant(val buildTypeName: String) {
    DEBUG("debug"),
    STAGING("staging"),
    QA("qa"),
    RELEASE("release");

    companion object {
        fun from(name: String): BuildVariant =
            entries.firstOrNull { it.buildTypeName == name }
                ?: error("Unknown build variant: '$name'. Expected one of: ${entries.map { it.buildTypeName }}")
    }
}
```

This enum lives in `build-logic` source, so it is compiled into the plugins and not exported to the app's runtime classpath.

---

## Step 3 — `AndroidComposeConventionPlugin`

Configures Compose compiler plugin + `buildFeatures.compose = true`. Works for both an `applicationExtension` (app module) and a `libraryExtension` (library module) by checking which extension is present.

```kotlin
// AndroidComposeConventionPlugin.kt

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply the Kotlin Compose compiler plugin (required for Compose 1.5+/K2)
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // Configure whichever Android extension is present
            val isApp = pluginManager.hasPlugin("com.android.application")
            val isLibrary = pluginManager.hasPlugin("com.android.library")

            when {
                isApp -> extensions.configure<ApplicationExtension> {
                    buildFeatures.compose = true
                }
                isLibrary -> extensions.configure<LibraryExtension> {
                    buildFeatures.compose = true
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

**Design notes:**
- The plugin does not add any Compose BOM or dependencies. Dependency declarations stay in each module's `build.gradle.kts` — the plugin only handles *compiler/feature flag* concerns.
- If a future module uses `com.android.library`, this same plugin applies without changes.

User updates: agrega las dependencias de compose aqui, incluyendo el bom de compose. aqui todo lo necesario para correr compose. 
---

## Step 4 — `AndroidBuildTypesConventionPlugin`

Defines the four build types (`debug`, `staging`, `qa`, `release`) for the **app** module. Includes signing config setup, `applicationIdSuffix`, `versionNameSuffix`, `isMinifyEnabled`, and `proguardFiles`.

This plugin does **not** add `buildConfigField` entries — that belongs to the BuildConfig plugin.

```kotlin
// AndroidBuildTypesConventionPlugin.kt

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildTypesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {

                signingConfigs {
                    create("release") {
                        storeFile = System.getenv("KEYSTORE_PATH")
                            .takeIf { !it.isNullOrBlank() }
                            ?.let { file(it) }
                            ?: rootProject.file("keystore/drink_water_app_keystore.jks")

                        storePassword = System.getenv("KEYSTORE_PASSWORD")
                            ?: localProperties["keystore.password"] as? String ?: ""

                        keyAlias = System.getenv("KEY_ALIAS")
                            ?: localProperties["key.alias"] as? String ?: ""

                        keyPassword = System.getenv("KEY_PASSWORD")
                            ?: localProperties["key.password"] as? String ?: ""
                    }
                }

                buildTypes {
                    debug {
                        applicationIdSuffix = ".dev"
                        versionNameSuffix = "-dev"
                    }

                    create("staging") {
                        initWith(getByName("release"))
                        matchingFallbacks += listOf("release")
                        applicationIdSuffix = ".staging"
                        versionNameSuffix = "-staging"
                    }

                    create("qa") {
                        initWith(getByName("debug"))
                        matchingFallbacks += listOf("debug")
                        signingConfig = signingConfigs.getByName("debug")
                        applicationIdSuffix = ".qa"
                        versionNameSuffix = "-qa"
                    }

                    release {
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
```

**`localProperties` helper:** The plugin needs to read `local.properties` at configuration time. Rather than duplicating the `Properties().apply { load(...) }` pattern in every plugin, we can define a small extension at the top of `build-logic/src/main/kotlin/`:

```kotlin
// LocalProperties.kt  (shared helper inside build-logic)
import org.gradle.api.Project
import java.util.Properties

val Project.localProperties: Properties
    get() = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) load(file.inputStream())
    }
```

user update: hay que usar en build variant enum para crear cada build variant. me imagino hacer loop y crear cada variante. la idea es restringir la cantidad de variantes con analisis de codigo statico para evitar que otro convention plugin cree una variante que otros plugins no soporten. 


---

## Step 5 — `AndroidBuildConfigConventionPlugin`

Enables `buildFeatures.buildConfig = true` and adds all `buildConfigField` entries per variant. This plugin also reads secrets from `local.properties` / environment variables.

```kotlin
// AndroidBuildConfigConventionPlugin.kt

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildConfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val props = localProperties

            val revenuecatDev = props.getProperty("revenuecat.api.key.dev", "")
            val revenuecatProd = System.getenv("REVENUECAT_API_KEY")
                ?: props.getProperty("revenuecat.api.key.prod", "")

            extensions.configure<ApplicationExtension> {
                buildFeatures.buildConfig = true

                buildTypes {
                    debug {
                        buildConfigField("boolean", "IS_DEV",    "true")
                        buildConfigField("boolean", "IS_STAGING","false")
                        buildConfigField("boolean", "IS_QA",     "false")
                        buildConfigField("boolean", "LOGS_ENABLED",            "true")
                        buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                        buildConfigField("boolean", "FEATURE_STREAKS",         "false")
                        buildConfigField("String",  "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
                    }

                    // staging / qa / release are referenced by name because they are
                    // created by AndroidBuildTypesConventionPlugin which must be applied first.
                    getByName(BuildVariant.STAGING.buildTypeName) {
                        buildConfigField("boolean", "IS_DEV",    "false")
                        buildConfigField("boolean", "IS_STAGING","true")
                        buildConfigField("boolean", "IS_QA",     "false")
                        buildConfigField("boolean", "LOGS_ENABLED",            "true")
                        buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                        buildConfigField("boolean", "FEATURE_STREAKS",         "false")
                        buildConfigField("String",  "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
                    }

                    getByName(BuildVariant.QA.buildTypeName) {
                        buildConfigField("boolean", "IS_DEV",    "false")
                        buildConfigField("boolean", "IS_STAGING","false")
                        buildConfigField("boolean", "IS_QA",     "true")
                        buildConfigField("boolean", "LOGS_ENABLED",            "true")
                        buildConfigField("boolean", "FEATURE_REQUEST_FEATURE", "true")
                        buildConfigField("boolean", "FEATURE_STREAKS",         "false")
                        buildConfigField("String",  "REVENUECAT_API_KEY", "\"$revenuecatDev\"")
                    }

                    release {
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
```

**Important ordering constraint:** `AndroidBuildConfigConventionPlugin` calls `getByName("staging")` and `getByName("qa")`, which means those build types must already exist when this plugin is applied. Therefore in `compose/build.gradle.kts`, the application order must be:

```
drinkwater.android.buildtypes   ← creates staging + qa
drinkwater.android.buildconfig  ← configures fields on them
```

This is safe and explicit. Both plugins only touch build type configuration — they do not conflict.


user update: usando en enum podremos restringir la creacion de variantes y explicitamente definir aqui la configuracion necesaria si otro enum se agrega en el futuro. 

---

## Plugin application order (future reference)

When the plugins are eventually applied to `:compose`, the recommended order is:

```kotlin
plugins {
    id("com.android.application")
    id("com.android.built-in-kotlin")
    id("drinkwater.android.buildtypes")    // must be before buildconfig
    id("drinkwater.android.buildconfig")
    id("drinkwater.android.compose")
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

## Open Questions / Decisions to confirm

1. **`BuildVariant` scope:** The enum is defined in `build-logic` source. It is available to all plugin files but not to the app's production code. If the app itself ever needs to branch on build variant at runtime, it already has `BuildConfig.IS_DEV` etc. — the enum is build-time only. This is intentional.

update: el enum estara solo disponible para los plugins no hay problema.

2. **`signingConfigs` inside plugin vs inline:** Moving signing config into the plugin means the plugin file will be the single source of truth for keystore resolution logic. The `compose/build.gradle.kts` will no longer need the `localProps` boilerplate for signing. Confirm this is acceptable.

update: todavia quiero mantener las api keys y secrets en local.properties, tendriamos que configurar para apuntar al archivo de forma correcta. no quiero moverlo tampoco.

3. **`local.properties` helper placement:** The `LocalProperties.kt` helper could go directly in one of the plugin files or as a shared `Project` extension. Shared extension is cleaner and avoids duplication across the three plugins.

update: prefiero que se quede donde esta el archivo original local.properties, el helper puede estar ubicado donde mejor este. pero el local.properties lo quiero siempre donde esta ahora.

4. **Feature flags in BuildConfig plugin:** Currently `FEATURE_STREAKS` and `FEATURE_REQUEST_FEATURE` are hardcoded per variant. If they vary per release cycle, having them in one plugin file makes them easy to update. Confirm the flags defined above match the intended defaults.

5. **Groovy root build files:** The root `build.gradle` and `settings.gradle` are still Groovy. Migrating them to Kotlin DSL is not in scope for this plan but is a natural next step.

update: si podemos trabajar con groovy y luego migrar el codigo que vamos a generar con este plan, entonces no hay problema. de lo contrario vamos a migrar si se necesita algo especifico de kotlin dsl.