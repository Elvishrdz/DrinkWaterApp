# Guía de Buenas Prácticas de Ingeniería — DrinkWaterApp

Referencia para mantener la calidad, consistencia y mantenibilidad del código a largo plazo.
Cada herramienta incluye su estado actual, por qué importa y los pasos exactos para implementarla.

---

## Índice

| # | Herramienta                                                            | Estado                         | Prioridad |
|---|------------------------------------------------------------------------|--------------------------------|-----------|
| 1 | [Renovate — Actualización automática de dependencias](#1-renovate)     | ✅ Implementado                 | —         |
| 2 | [EditorConfig — Estilo de código consistente](#2-editorconfig)         | ❌ Pendiente                    | Alta      |
| 3 | [Detekt — Análisis estático de Kotlin](#3-detekt)                      | ⚠️ Parcial (solo en TOML)      | Alta      |
| 4 | [Android Lint — Reglas de calidad Android](#4-android-lint)            | ❌ Pendiente                    | Alta      |
| 5 | [CI Quality Gate — Verificación automática en PRs](#5-ci-quality-gate) | ❌ Pendiente                    | Alta      |
| 6 | [Makefile — Targets de calidad](#6-makefile-targets-de-calidad)        | ⚠️ Parcial (falta integración) | Media     |
| 7 | [Convenciones de módulos y dependencias](#7-convenciones-de-módulos)   | ✅ Base existe                  | Media     |
| 8 | [Convenciones de commits y ramas](#8-convenciones-de-commits-y-ramas)  | ❌ Pendiente                    | Media     |

---

## 1. Renovate

**Estado:** ✅ Implementado en `renovate.json`

### Qué es

Renovate Bot es un servicio que abre Pull Requests automáticos cuando hay nuevas versiones de
dependencias. Detecta cambios en `libs.versions.toml`, `build.gradle.kts` y los workflows de GitHub
Actions.

### Configuración actual

| Setting             | Valor                  | Razón                             |
|---------------------|------------------------|-----------------------------------|
| `schedule`          | Lunes antes de las 6am | PRs listos al inicio de la semana |
| `prConcurrentLimit` | 5                      | Evita flood de PRs abiertos       |
| `prHourlyLimit`     | 2                      | Respeta rate limits de GitHub     |
| `labels`            | `dependencies`         | Fácil de filtrar en el backlog    |

**Grupos de dependencias configurados** (se actualizan en un solo PR):

- **Kotlin & KSP** — deben mantenerse compatibles entre sí
- **Android Gradle Plugin** — requiere actualización coordinada con Gradle wrapper
- **Room** — las tres referencias (`room`, `roomRuntime`, `roomGradlePlugin`) deben coincidir
- **AndroidX Lifecycle** — todas las artifacts de lifecycle van juntas
- **AndroidX DataStore** — artifacts de DataStore van juntas
- **Koin** — todas las artifacts de Koin van juntas
- **Firebase & Google Services** — BOM + Crashlytics gradle
- **GitHub Actions** — todos los `uses:` de los workflows

### Cómo revisar un PR de Renovate

1. Verificar que el changelog no mencione breaking changes.
2. Correr `make test` localmente si la dependencia es crítica (Room, Kotlin, AGP).
3. Hacer merge solo si CI pasa.

### Extender la configuración

Para agrupar nuevas dependencias, editar `renovate.json`:

```json
{
  "description": "Mi nueva librería",
  "matchPackagePatterns": [
    "^com.example.lib"
  ],
  "groupName": "Mi Librería"
}
```

---

## 2. EditorConfig

**Estado:** ❌ Pendiente

### Qué es

`.editorconfig` es un estándar soportado por Android Studio, IntelliJ, VS Code y la mayoría de
editores. Define reglas de formato (indentación, charset, fin de línea) que aplican automáticamente
al abrir o guardar archivos, independientemente de las configuraciones personales de cada
desarrollador.

### Por qué importa

Sin EditorConfig cada desarrollador puede tener configuraciones distintas, generando diffs de
whitespace en PRs, mezcla de tabs/espacios o encoding inconsistente — ruido que dificulta la
revisión de código.

### Implementación

Crear el archivo en la raíz del proyecto:

**Archivo:** `.editorconfig`

```ini
root = true

# Todos los archivos
[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

# Kotlin y KTS
[*.{kt,kts}]
indent_style = space
indent_size = 4
max_line_length = 120

# XML (layouts, manifests, resources)
[*.xml]
indent_style = space
indent_size = 4
max_line_length = off

# Gradle Groovy (root build.gradle)
[*.gradle]
indent_style = space
indent_size = 4

# JSON (renovate, google-services, etc.)
[*.json]
indent_style = space
indent_size = 2

# YAML (GitHub Actions)
[*.{yml,yaml}]
indent_style = space
indent_size = 2

# Markdown
[*.md]
trim_trailing_whitespace = false
max_line_length = off

# Makefile requiere tabs obligatoriamente
[Makefile]
indent_style = tab
```

### Verificar en Android Studio

`Settings → Editor → Code Style` → el ícono de engranaje → `Enable EditorConfig support` (ya
debería estar activo por defecto desde AS Flamingo).

---

## 3. Detekt

**Estado:** ⚠️ Parcial — la versión `1.23.8` y las dependencias están declaradas en
`libs.versions.toml`, pero Detekt **no está aplicado a ningún módulo ni tiene archivo de
configuración**.

### Qué es

Detekt es el analizador estático estándar para Kotlin. Detecta code smells, complejidad excesiva,
problemas de nombrado, imports no usados, funciones demasiado largas y más de 400 reglas
categorizadas. Incluye además el plugin `detekt-formatting` que aplica las reglas de `ktlint` como
formatter.

### Implementación paso a paso

#### Paso 1 — Crear el archivo de configuración

Crear `config/detekt/detekt.yml` en la raíz del proyecto. Este archivo activa/desactiva reglas
individualmente:

```yaml
# config/detekt/detekt.yml
build:
  maxIssues: 0          # El build falla si hay cualquier issue
  excludeCorrectable: false

config:
  validation: true
  warningsAsErrors: false

# ── Estilo ────────────────────────────────────────────────────────────────────
style:
  active: true
  MagicNumber:
    active: true
    ignoreNumbers: [ '-1', '0', '1', '2' ]
    ignoreConstantDeclaration: true
    ignoreCompanionObjectPropertyDeclaration: true
    ignoreAnnotation: true
    ignoreNamedArgument: true
    ignoreEnums: true
  WildcardImport:
    active: true
    excludeImports:
      - 'androidx.compose.ui.Modifier'
      - 'kotlinx.coroutines.*'
  MaxLineLength:
    active: true
    maxLineLength: 120
    excludePackageStatements: true
    excludeImportStatements: true
    excludeCommentStatements: false
  UnusedPrivateMember:
    active: true
  ForbiddenComment:
    active: true
    values: [ 'STOPSHIP', 'FIXME' ]
    allowedPatterns: 'TODO'

# ── Complejidad ───────────────────────────────────────────────────────────────
complexity:
  active: true
  CyclomaticComplexMethod:
    active: true
    threshold: 15
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    functionThreshold: 8
    constructorThreshold: 8
    ignoreDefaultParameters: true
    ignoreDataClasses: true
    ignoreAnnotated: [ 'Composable' ]
  TooManyFunctions:
    active: true
    thresholdInFiles: 30
    thresholdInClasses: 20
    thresholdInInterfaces: 15
    thresholdInObjects: 20
    thresholdInEnums: 10
    ignoreDeprecated: true
    ignorePrivate: false
    ignoreOverridden: true

# ── Nombrado ──────────────────────────────────────────────────────────────────
naming:
  active: true
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'
    excludes: [ '**/test/**', '**/androidTest/**' ]
    ignoreAnnotated: [ 'Composable' ]
  VariableNaming:
    active: true
    variablePattern: '[a-z][A-Za-z0-9]*'
  ClassNaming:
    active: true
    classPattern: '[A-Z][a-zA-Z0-9]*'

# ── Posibles bugs ─────────────────────────────────────────────────────────────
potential-bugs:
  active: true
  EqualsAlwaysReturnsTrueOrFalse:
    active: true
  UnreachableCode:
    active: true
  UnnecessaryNotNullOperator:
    active: true

# ── Rendimiento ───────────────────────────────────────────────────────────────
performance:
  active: true
  ArrayPrimitive:
    active: true
  ForEachOnRange:
    active: true

# ── Coroutines ────────────────────────────────────────────────────────────────
coroutines:
  active: true
  GlobalCoroutineUsage:
    active: true
  RedundantSuspendModifier:
    active: true
  SuspendFunWithFlowReturnType:
    active: true

# ── Formatting (ktlint via detekt-formatting) ─────────────────────────────────
formatting:
  active: true
  android: true
  autoCorrect: true
  Indentation:
    active: true
    indentSize: 4
  MaximumLineLength:
    active: true
    maxLineLength: 120
  NoWildcardImports:
    active: true
  TrailingCommaOnCallSite:
    active: true
    useTrailingCommaOnCallSite: true
  TrailingCommaOnDeclarationSite:
    active: true
    useTrailingCommaOnDeclarationSite: true
```

#### Paso 2 — Crear el convention plugin de Detekt

Crear `build-logic/src/main/kotlin/AndroidDetektConventionPlugin.kt`:

```kotlin
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class AndroidDetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val extension = extensions.getByType<DetektExtension>()
            extension.apply {
                config.setFrom(rootProject.files("config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                allRules = false
                autoCorrect = true
                parallel = true
            }

            tasks.withType<Detekt>().configureEach {
                jvmTarget = "11"
                reports {
                    html.required.set(true)
                    xml.required.set(false)
                    txt.required.set(false)
                    sarif.required.set(false)
                }
                exclude("**/build/**")
                exclude("**/generated/**")
            }

            tasks.withType<DetektCreateBaselineTask>().configureEach {
                jvmTarget = "11"
                exclude("**/build/**")
                exclude("**/generated/**")
            }

            dependencies {
                "detektPlugins"(libs.findLibrary("detekt-formatting").get())
            }
        }
    }
}
```

#### Paso 3 — Registrar el plugin en build-logic

Agregar en `build-logic/build.gradle.kts` dentro del bloque `gradlePlugin { plugins { ... } }`:

```kotlin
register("androidDetekt") {
    id = "drinkwater.android.detekt"
    implementationClass = "AndroidDetektConventionPlugin"
}
```

Y agregar la dependencia en el bloque `dependencies`:

```kotlin
compileOnly(libs.detekt.gradle.plugin)
```

#### Paso 4 — Aplicar en cada módulo

En `compose/build.gradle.kts`, `ads/build.gradle.kts`, `analytics/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.drinkwater.android.detekt)
    // ... otros plugins existentes
}
```

#### Paso 5 — Generar baseline (primera vez)

La primera vez se ejecuta en modo baseline para no bloquear el proyecto con issues legacy:

```bash
./gradlew detektBaseline
```

Esto crea `config/detekt/baseline.xml`. Commitearlo. A partir de ese momento solo los issues
**nuevos** bloquean el build.

#### Paso 6 — Correr Detekt

```bash
./gradlew detekt              # analiza todo el proyecto
./gradlew :compose:detekt     # solo el módulo compose
```

Los reportes HTML se generan en `build/reports/detekt/detekt.html` de cada módulo.

### Política de supresión

Cuando una regla genera un falso positivo, suprimir con anotación (no con comentario):

```kotlin
@Suppress("MagicNumber")
val animationDuration = 300
```

**Nunca** usar `@file:Suppress` globalmente. Si hay muchos falsos positivos en una regla,
desactivarla en `detekt.yml` con documentación de por qué.

---

## 4. Android Lint

**Estado:** ❌ Pendiente

### Qué es

El Lint de Android es el análisis estático oficial de Google. Detecta problemas específicos de
Android: uso incorrecto de APIs, recursos no utilizados, problemas de accesibilidad, issues de
internacionalización, errores de seguridad y más.

### Implementación

#### Paso 1 — Crear `lint.xml`

Crear `config/lint/lint.xml` en la raíz:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- Tratar warnings como errores en CI -->
    <issue id="all" severity="warning" />

    <!-- Seguridad: siempre error -->
    <issue id="AllowBackup" severity="error" />
    <issue id="ExportedReceiver" severity="error" />
    <issue id="HardcodedDebugMode" severity="error" />
    <issue id="SetTextI18n" severity="error" />
    <issue id="UseSparseArrays" severity="ignore" />

    <!-- Accesibilidad -->
    <issue id="ContentDescription" severity="warning" />
    <issue id="ClickableViewAccessibility" severity="warning" />

    <!-- Recursos no usados (útil al refactorizar) -->
    <issue id="UnusedResources" severity="warning" />

    <!-- Obsoletos -->
    <issue id="Deprecated" severity="warning" />
    <issue id="NewApi" severity="error" />

    <!-- Desactivar reglas que generan ruido en proyectos Compose -->
    <issue id="VectorPath" severity="ignore" />
    <issue id="UnusedAttribute" severity="ignore" />
</lint>
```

#### Paso 2 — Integrar en el convention plugin de library/application

Agregar en `AndroidApplicationConventionPlugin.kt` y `AndroidLibraryConventionPlugin.kt`:

```kotlin
extensions.configure<ApplicationExtension> {
    // ... configuración existente
    lint {
        lintConfig = rootProject.file("config/lint/lint.xml")
        abortOnError = true
        checkReleaseBuilds = true
        htmlReport = true
        htmlOutput = project.file("build/reports/lint/lint.html")
    }
}
```

#### Paso 3 — Correr Lint

```bash
./gradlew lint                   # todos los módulos
./gradlew :compose:lintDebug     # solo compose, variante debug
```

---

## 5. CI Quality Gate

**Estado:** ❌ Pendiente — los workflows actuales (`release.yml`, `listing.yml`) solo cubren
release y listing. **No hay verificación automática de calidad en Pull Requests.**

### Qué es

Un workflow de GitHub Actions que se ejecuta en cada Pull Request y bloquea el merge si el código
no pasa las verificaciones de calidad: Detekt, Lint y tests unitarios.

### Implementación

Crear `.github/workflows/quality.yml`:

```yaml
name: Quality Gate

on:
  pull_request:
    branches:
      - development
      - main
  push:
    branches:
      - development

concurrency:
  group: quality-${{ github.ref }}
  cancel-in-progress: true

jobs:
  quality:
    name: Static Analysis & Tests
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Setup JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Create local.properties
        run: |
          echo "REVENUECAT_API_KEY=dummy_key_for_ci" >> local.properties
          echo "MIXPANEL_TOKEN=dummy_token_for_ci" >> local.properties

      - name: Run Detekt
        run: ./gradlew detekt --continue

      - name: Run Lint
        run: ./gradlew lint --continue

      - name: Run Unit Tests
        run: ./gradlew test --continue

      - name: Upload Detekt Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: detekt-report
          path: '**/build/reports/detekt/**'
          retention-days: 7

      - name: Upload Lint Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: '**/build/reports/lint/**'
          retention-days: 7

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/build/test-results/**'
          retention-days: 7
```

### Configurar branch protection

En GitHub → Settings → Branches → Branch protection rules para `development`:

- [x] Require status checks to pass before merging
    - Status check: `Static Analysis & Tests`
- [x] Require branches to be up to date before merging
- [x] Restrict who can push to matching branches (opcional)

---

## 6. Makefile targets de calidad

**Estado:** ⚠️ Parcial — el Makefile tiene targets de build y ADB pero **no tiene targets para
correr las verificaciones de calidad**.

### Targets a agregar en `Makefile`

```makefile
# ── Quality ───────────────────────────────────────────────────────────────────
.PHONY: check detekt lint test test-all

## Corre todas las verificaciones de calidad (detekt + lint + tests)
check: detekt lint test

## Análisis estático con Detekt en todos los módulos
detekt:
	$(GRADLEW) detekt

## Android Lint en todos los módulos
lint:
	$(GRADLEW) lint

## Unit tests del módulo principal (:compose)
test:
	$(GRADLEW) :$(MODULE):testDebugUnitTest

## Unit tests de todos los módulos
test-all:
	$(GRADLEW) test

## Genera baseline de Detekt (solo la primera vez o tras refactor grande)
detekt-baseline:
	$(GRADLEW) detektBaseline
```

Agregar también en el target `help`:

```makefile
@echo ""
@echo "  Quality:"
@echo "    check              Corre detekt + lint + tests"
@echo "    detekt             Análisis estático Kotlin (Detekt)"
@echo "    lint               Android Lint"
@echo "    test               Unit tests del módulo compose"
@echo "    test-all           Unit tests de todos los módulos"
@echo "    detekt-baseline    Genera baseline de Detekt"
```

### Flujo recomendado antes de push

```bash
make check   # Verifica todo antes de hacer push
```

---

## 7. Convenciones de módulos

**Estado:** ✅ Base existe (convention plugins en `build-logic`)

### Estructura actual

```
:compose       → app principal (Compose)
:ads           → módulo de AdMob
:analytics     → módulo de Mixpanel
:app           → módulo legacy (en proceso de deprecación)
build-logic/   → convention plugins
```

### Reglas de dependencias

| Módulo       | Puede depender de    | No puede depender de     |
|--------------|----------------------|--------------------------|
| `:compose`   | `:ads`, `:analytics` | —                        |
| `:ads`       | —                    | `:compose`, `:analytics` |
| `:analytics` | —                    | `:compose`, `:ads`       |

**Principio:** Los módulos de feature/utility (`:ads`, `:analytics`) nunca deben depender del
módulo de app. La dirección de dependencia es siempre hacia abajo.

### Convention plugins disponibles

| Plugin                           | Uso                                        |
|----------------------------------|--------------------------------------------|
| `drinkwater.android.application` | Módulo de app (`:compose`)                 |
| `drinkwater.android.library`     | Módulos de librería (`:ads`, `:analytics`) |
| `drinkwater.android.compose`     | Módulos que usan Compose                   |
| `drinkwater.android.buildtypes`  | Módulos con múltiples variantes            |
| `drinkwater.android.buildconfig` | Módulos con BuildConfig fields             |
| `drinkwater.android.room`        | Módulos con base de datos Room             |
| `drinkwater.android.detekt`      | **Pendiente de implementar** (ver §3)      |

### Agregar un nuevo módulo

1. Crear el directorio del módulo.
2. Agregar en `settings.gradle`: `include(":nombre-modulo")`.
3. Crear `nombre-modulo/build.gradle.kts` usando convention plugins existentes.
4. No repetir configuración que ya existe en los plugins.

---

## 8. Convenciones de commits y ramas

**Estado:** ❌ Pendiente (no hay enforcing automatizado)

### Formato de commits (Conventional Commits)

Usar el estándar [Conventional Commits](https://www.conventionalcommits.org/):

```
<tipo>(<alcance>): <descripción corta>

[cuerpo opcional]

[footer opcional]
```

**Tipos permitidos:**

| Tipo       | Cuándo usarlo                                 |
|------------|-----------------------------------------------|
| `feat`     | Nueva funcionalidad visible para el usuario   |
| `fix`      | Corrección de un bug                          |
| `refactor` | Cambio de código sin cambio de comportamiento |
| `test`     | Agregar o modificar tests                     |
| `chore`    | Tareas de mantenimiento (deps, CI, build)     |
| `docs`     | Solo documentación                            |
| `perf`     | Mejora de rendimiento                         |
| `revert`   | Revertir un commit anterior                   |

**Ejemplos:**

```
feat(reminder): add snooze option to water reminder notification
fix(alarm): prevent duplicate alarms on device restart
chore(deps): bump Kotlin to 2.2.20
refactor(compose): extract DrinkButton into reusable component
```

### Convención de ramas

```
feature/<descripcion-corta>    → nueva funcionalidad
fix/<descripcion-corta>        → corrección de bug
chore/<descripcion-corta>      → mantenimiento
release/<version>              → preparación de release
```

**Rama principal de desarrollo:** `development`

### Flujo de trabajo

```
feature/mi-feature  ──→  development  ──→  (release)
fix/mi-bug          ──→  development
chore/update-deps   ──→  development
```

Los PRs de Renovate van directamente a `development` y solo se mergean si CI pasa.

---

## Checklist de implementación

Orden recomendado para implementar lo que está pendiente:

- [ ] **Crear `.editorconfig`** — §2 — sin dependencias, impacto inmediato
- [ ] **Crear `config/detekt/detekt.yml`** — §3, Paso 1
- [ ] **Crear `AndroidDetektConventionPlugin.kt`** — §3, Paso 2
- [ ] **Registrar plugin en `build-logic`** — §3, Paso 3
- [ ] **Aplicar plugin en módulos** — §3, Paso 4
- [ ] **Generar baseline** (`./gradlew detektBaseline`) — §3, Paso 5
- [ ] **Crear `config/lint/lint.xml`** — §4, Paso 1
- [ ] **Integrar lint en convention plugins** — §4, Paso 2
- [ ] **Crear `.github/workflows/quality.yml`** — §5
- [ ] **Configurar branch protection en GitHub** — §5 (final)
- [ ] **Agregar targets de calidad en Makefile** — §6

---

## Referencias

- [Detekt docs](https://detekt.dev/)
- [Detekt rules catalog](https://detekt.dev/docs/rules/comments)
- [Renovate docs](https://docs.renovatebot.com/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Android Lint checks](https://developer.android.com/studio/write/lint)
- [GitHub Actions – android setup](https://github.com/actions/setup-java)
