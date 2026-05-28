# Detekt — Guía de Análisis Estático

Guía de referencia para el análisis estático del proyecto con Detekt.
Cubre qué es, cómo está integrado, los comandos disponibles, el manejo del baseline y cómo
extender la configuración a nuevos módulos.

---

## 1. ¿Qué es Detekt?

[Detekt](https://detekt.dev) es una herramienta de análisis estático para Kotlin. Analiza el
código **sin ejecutarlo** y detecta:

- Violaciones de estilo y convenciones de nomenclatura
- Código complejo o difícil de mantener (métodos largos, alta complejidad ciclomática)
- Bugs potenciales (variables que podrían ser `val`, uso de `!!`, colecciones mutables innecesarias)
- Problemas de corrutinas (`GlobalScope`, dispatchers no inyectados)
- Formateo inconsistente (espaciado, imports no usados, líneas largas) vía **detekt-formatting**

El objetivo es que el equipo comparta un estándar de calidad sin depender de revisiones manuales
para detalles mecánicos.

---

## 2. Arquitectura en el proyecto

Detekt se integra mediante un **convention plugin** (`drinkwater.detekt`) definido en
`build-logic/`. Esto sigue el mismo patrón que el resto de los convention plugins del proyecto.

```
build-logic/
└── src/main/kotlin/
    └── DetektConventionPlugin.kt   ← aplica el plugin + configuración central

config/detekt/
└── detekt.yml                      ← reglas activas para todos los módulos

:compose/detekt-baseline.xml        ┐
:ads/detekt-baseline.xml            ├── deuda técnica existente (ignorada)
:analytics/detekt-baseline.xml      ┘
```

### ¿Qué hace el convention plugin?

Cuando un módulo declara `id("drinkwater.detekt")`, automáticamente:

1. Aplica el plugin `io.gitlab.arturbosch.detekt`
2. Apunta la configuración a `config/detekt/detekt.yml`
3. Carga el `detekt-baseline.xml` local del módulo para ignorar deuda técnica existente
4. Agrega `detekt-formatting` (ktlint) como dependencia de análisis
5. Habilita ejecución en paralelo

### Módulos con Detekt activo

| Módulo       | Archivo de baseline             |
|--------------|---------------------------------|
| `:compose`   | `compose/detekt-baseline.xml`   |
| `:ads`       | `ads/detekt-baseline.xml`       |
| `:analytics` | `analytics/detekt-baseline.xml` |

> El módulo `:app` es legacy (Java/Kotlin antiguo) y no tiene Detekt aplicado.

---

## 3. Configuración inicial (una sola vez)

Después de clonar el repositorio, instala el pre-commit hook:

```bash
make install-hooks
```

Esto crea un **symlink** desde `.git/hooks/pre-commit` al script
`scripts/git-hooks/pre-commit`. A partir de ese momento, Detekt corre automáticamente antes de
cada `git commit`.

> **Nota:** El symlink vive solo en tu máquina local. Cada integrante del equipo debe ejecutar
> `make install-hooks` una vez después de clonar.

---

## 4. Comandos disponibles

### Análisis completo

```bash
make detekt
# equivale a: ./gradlew detekt
```

Corre Detekt en todos los módulos que tienen `id("drinkwater.detekt")`. Si hay violaciones
**fuera del baseline**, el build falla y lista los problemas.

### Auto-corrección de formateo

```bash
make detekt-fix
# equivale a: ./gradlew detekt --auto-correct
```

Corrige automáticamente las violaciones de formateo que ktlint puede resolver: indentación,
espaciado, imports no usados, semicolons, etc. Las violaciones de lógica o complejidad no se
auto-corrigen y deben resolverse manualmente.

Ejecutar `make detekt-fix` antes de commitear es una buena práctica cuando se trabaja en archivos
con mucho formateo pendiente.

### Regenerar baseline

```bash
make detekt-baseline
# equivale a: ./gradlew detektBaseline
```

Escanea todos los módulos y actualiza cada `detekt-baseline.xml` con las violaciones actuales.
Las violaciones registradas en el baseline **no bloquean** futuros commits.

Cuándo usarlo:

- Cuando el equipo acepta incorporar deuda técnica existente de un módulo nuevo
- Cuando se migra un archivo legacy que tiene muchas violaciones intencionalmente ignoradas
- **No** usarlo para tapar violaciones en código nuevo

> Siempre hacer commit del baseline actualizado junto con el código que lo justifica.

---

## 5. El pre-commit hook

El hook en `scripts/git-hooks/pre-commit` se ejecuta automáticamente al hacer `git commit`.

**Flujo:**

```
git commit
    └── pre-commit hook
            ├── ¿hay archivos .kt/.kts staged? → no → skip (exit 0)
            └── sí → ./gradlew detekt
                        ├── BUILD SUCCESSFUL → commit continúa
                        └── BUILD FAILED     → commit bloqueado, se muestran las violaciones
```

### Saltar Detekt en un commit

En casos de emergencia (hotfix urgente, commit de trabajo en progreso, etc.):

```bash
SKIP_DETEKT=1 git commit -m "mensaje"
```

> Úsalo con criterio. El objetivo del hook es atrapar problemas antes de que lleguen al
> historial, no bloquear el trabajo legítimo.

---

## 6. Entender el output de Detekt

Cuando Detekt encuentra violaciones, el output se ve así:

```
/path/to/MyFile.kt:42:15: Magic number '42' [MagicNumber]
/path/to/MyFile.kt:88:1: Function 'doEverything' has 80 lines, threshold is 60 [LongMethod]
```

El formato es: `archivo:línea:columna: descripción [ReglaDetekt]`

### Tipos de violaciones comunes

| Regla                     | Qué detecta                                            |
|---------------------------|--------------------------------------------------------|
| `MagicNumber`             | Números literales sin constante nombrada               |
| `LongMethod`              | Funciones con más de 60 líneas                         |
| `CyclomaticComplexMethod` | Funciones con demasiados caminos de ejecución          |
| `LongParameterList`       | Funciones con más de 8 parámetros                      |
| `TooManyFunctions`        | Clases con más de 20 funciones                         |
| `UseCheckOrError`         | `throw IllegalStateException(...)` → usar `error(...)` |
| `GlobalCoroutineUsage`    | Uso de `GlobalScope`                                   |
| `InjectDispatcher`        | Dispatchers hardcodeados en vez de inyectados          |
| `UnusedPrivateMember`     | Propiedades o funciones privadas no usadas             |
| `WildcardImport`          | `import com.example.*`                                 |
| `NoWildcardImports`       | Igual, vía regla de formateo                           |
| `MaximumLineLength`       | Líneas de más de 140 caracteres                        |

---

## 7. Agregar Detekt a un módulo nuevo

Solo añade el plugin en el bloque `plugins` del módulo:

```kotlin
// MiModulo/build.gradle.kts
plugins {
    id("com.android.library")
    id("drinkwater.android.library")
    id("drinkwater.detekt")          // ← agregar esta línea
}
```

La primera vez que el módulo tenga código con deuda técnica existente, genera su baseline:

```bash
./gradlew :mi-modulo:detektBaseline
```

Esto crea `mi-modulo/detekt-baseline.xml`. Haz commit de ese archivo junto con el cambio en
`build.gradle.kts`.

---

## 8. Suprimir una regla puntualmente

Cuando una violación es un falso positivo o existe una razón técnica válida para ignorarla,
usa la anotación `@Suppress`:

```kotlin
// Suprimir una sola regla en una declaración
@Suppress("MagicNumber")
val timeoutMs = 3000

// Suprimir múltiples reglas
@Suppress("LongMethod", "CyclomaticComplexMethod")
fun buildComplexUiState(): UiState {
    ...
}
```

> Prefiere el baseline para deuda técnica existente en masa. Usa `@Suppress` solo para casos
> puntuales con justificación clara. No uses `@Suppress` para esconder código que debería
> refactorizarse.

---

## 9. Configuración central (`config/detekt/detekt.yml`)

Todos los módulos comparten el mismo archivo de configuración. Los valores más relevantes:

| Parámetro                                        | Valor | Descripción                               |
|--------------------------------------------------|-------|-------------------------------------------|
| `complexity>LongMethod>threshold`                | 60    | Líneas máximas por función                |
| `complexity>LongParameterList>functionThreshold` | 8     | Parámetros máximos (ignora `@Composable`) |
| `complexity>CyclomaticComplexMethod>threshold`   | 15    | Complejidad ciclomática máxima            |
| `complexity>LargeClass>threshold`                | 600   | Líneas máximas por clase                  |
| `style>MaxLineLength>maxLineLength`              | 140   | Longitud máxima de línea                  |
| `formatting>Indentation>indentSize`              | 4     | Espacios de indentación                   |

### Reglas específicas para Compose

Las funciones `@Composable` tienen excepciones configuradas:

- `FunctionNaming` — permite nombres en `PascalCase` (estándar de Compose)
- `LongMethod` — ignorado en funciones anotadas con `@Composable` y `@Preview`
- `LongParameterList` — ignorado en `@Composable` (los composables naturalmente tienen muchos
  params)

### Modificar una regla

Edita `config/detekt/detekt.yml` y ajusta el threshold o desactiva la regla:

```yaml
complexity:
  LongMethod:
    active: true
    threshold: 80   # subir el límite
```

El cambio aplica a todos los módulos en el próximo análisis.

---

## 10. Flujo de trabajo recomendado

```
Antes de commitear
    1. make detekt-fix        → corrige formateo automáticamente
    2. git add -p             → revisa y staged los cambios
    3. git commit             → el hook corre detekt automáticamente
                                  ✓ pasa → commit exitoso
                                  ✗ falla → corrige las violaciones y repite
```

### Cuando el hook bloquea un commit

1. Lee el output — indica el archivo, línea y regla que falló
2. Navega al archivo y corrige la violación
3. `git add` el archivo corregido
4. `git commit` de nuevo

Si la violación es deuda técnica **aceptada** por el equipo:

```bash
make detekt-baseline   # actualiza el baseline
git add **/detekt-baseline.xml
git commit -m "chore: update detekt baseline for <módulo>"
```

---

## 11. Referencia rápida

```bash
make install-hooks          # instalar hook (una vez por máquina)
make detekt                 # analizar todos los módulos
make detekt-fix             # auto-corregir formateo
make detekt-baseline        # regenerar baseline con deuda actual

SKIP_DETEKT=1 git commit    # saltarse el hook en emergencia

./gradlew :compose:detekt           # analizar un módulo específico
./gradlew :compose:detektBaseline   # baseline de un módulo específico
```
