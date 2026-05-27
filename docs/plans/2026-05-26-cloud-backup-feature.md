# Plan: Cloud Backup Feature (Google Drive + Dropbox — multi-proveedor extensible)

## Objetivo

Permitir al usuario sincronizar sus datos de la app (historial de bebidas, metas diarias y
preferencias) con un servicio de nube. Se implementan **dos proveedores concretos**: Google Drive
y Dropbox. El diseño es lo suficientemente abstracto para agregar futuros proveedores (OneDrive,
iCloud equivalente, etc.) sin tocar la capa de negocio.

Tener dos proveedores desde el inicio obliga a que la abstracción sea real y probada, no solo
teórica. Cada proveedor tiene su propio flujo de auth, su propia ubicación de archivo y sus
propios errores — la capa de negocio no debe saber nada de eso.

El feature empieza protegido por un **feature flag de solo DEBUG** y tendrá su propio
**playground en el menú dev** para facilitar las pruebas.

---

## Comportamiento esperado

| Escenario                    | Detalle                                                                                                                                                                              |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Usuario **sin backup**       | App funciona exactamente igual que hoy (solo local).                                                                                                                                 |
| Activar backup (primera vez) | Muestra selector de proveedor (Google Drive / Dropbox) → inicia auth del proveedor elegido → crea archivo → sincroniza datos locales al cloud.                                       |
| Backup desde onboarding      | Nuevo paso opcional al final del onboarding. Muestra selector de proveedor. Si el usuario acepta, completa auth y hace primer sync antes de terminar el onboarding.                  |
| Sync manual                  | Botón en Settings → Cloud Backup → "Sincronizar ahora".                                                                                                                              |
| Sync automático              | WorkManager `PeriodicWorkRequest` con intervalo configurable (cada día / cada 3 días / cada semana).                                                                                 |
| Restaurar al reinstalar      | App detecta backup remoto → diálogo: "Usar datos de la nube" o "Empezar desde cero".                                                                                                 |
| Conflicto local vs. nube     | Diálogo explícito: "Tienes datos locales y datos en la nube. ¿Cuáles quieres usar?" con opción de cancelar.                                                                          |
| Cambiar de proveedor         | En Settings: `ChangeProviderSheet` ofrece **Migrar backup** (copia el archivo al nuevo proveedor) o **Cambiar sin migrar** (empieza limpio en el nuevo).                             |
| Migrar backup en onboarding  | Si el usuario autentica Proveedor A y no hay backup, se ofrece verificar el otro proveedor. Si hay backup allí, puede migrar al proveedor elegido o simplemente usarlo directamente. |
| Desactivar backup            | En Settings puede desconectar la cuenta. Los datos locales se mantienen; el archivo remoto no se borra.                                                                              |

---

## Arquitectura general

```
┌─────────────────────────────────────────────────────────┐
│                 MÓDULO :compose                          │
│  ─────────────── Presentation layer ──────────────────  │
│  OnboardingStep(Backup)  ·  BackupSettingsSection        │
│  ProviderPickerSheet     ·  RestoreConflictDialog        │
│  ChangeProviderSheet     ·  MigrationProgressDialog      │
│  DevBackupPlayground                                     │
│  ────────────────────────────────────────────────────── │
│  BackupViewModel / UseCases (consume interfaces de       │
│  :backup; los ViewModels viven en :compose)              │
│  ────────────────────────────────────────────────────── │
│  Bridge impls: BackupSerializer (→ BackupDataSource)     │
│                DataStorePrefsStorage (→ BackupPreferences)│
│  BackupErrorMapper (necesita UiText / R.string.*)        │
└───────────────────────────┬─────────────────────────────┘
                            │  implementation(project(":backup"))
┌───────────────────────────▼─────────────────────────────┐
│                 MÓDULO :backup                           │
│  ─────────────────────────────────────────────────────  │
│  Use cases: SyncBackupUseCase · RestoreBackupUseCase     │
│             CheckBackupStatusUseCase                     │
│             ConnectBackupUseCase · DisconnectBackupUseCase│
│             MigrateBackupUseCase                         │
│  ─────────────────────────────────────────────────────  │
│  Interfaces puente: BackupDataSource · BackupPreferences │
│  ─────────────────────────────────────────────────────  │
│                   BackupRepository                       │
│  interface: connect, upload, download, getMetadata,      │
│             disconnect, deleteRemoteFile                 │
└──────────────────┬────────────────────────────────────--─┘
                   │  resuelve proveedor activo vía
                   │  BackupProviderRegistry
        ┌──────────▼────────────────┐    ┌─────────────────────┐
        │  CloudBackupProvider      │    │  BackupMigrationMgr  │
        │  interface                │    │  + BackupMigration   │
        └──────┬────────────────────┘    └─────────────────────┘
               │
   ┌───────────┴──────────────┐
   │                          │
   ▼                          ▼
GoogleDriveBackupProvider   DropboxBackupProvider
(App Data folder)           (App Folder)
```

## Módulo `:backup`

Toda la lógica de negocio del feature (modelos, providers, use cases, worker, DI core) vive en
un módulo Gradle independiente `:backup` (`com.eahm.drinkwaterapp.backup`). El módulo `:compose`
depende de él y aporta:

- Implementaciones concretas de las interfaces puente (`BackupDataSource`, `BackupPreferences`)
- `BackupErrorMapper` (necesita `UiText` / `R.string.*` de `:compose`)
- Toda la UI: ViewModels, screens, dialogs, onboarding step

### Interfaces puente

Para que `:backup` no dependa de Room ni DataStore (que viven en `:compose`), se definen dos
interfaces en `:backup`:

**`BackupDataSource`** — abstrae la serialización / deserialización desde la base de datos:

```kotlin
interface BackupDataSource {
    suspend fun serialize(): String
    suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit>
}
```

Implementada por `BackupSerializer` en `:compose`.

**`BackupPreferences`** — abstrae las 7 claves de prefs relacionadas con backup:

```kotlin
interface BackupPreferences {
    suspend fun getBackupEnabled(): Boolean
    suspend fun setBackupEnabled(enabled: Boolean)
    suspend fun getBackupProvider(): String?
    suspend fun setBackupProvider(id: String)
    suspend fun getBackupLastSyncAt(): Long?
    suspend fun setBackupLastSyncAt(millis: Long)
    suspend fun getBackupAutoIntervalDays(): Int
    suspend fun setBackupAutoIntervalDays(days: Int)
    suspend fun getBackupUserDisplayName(): String?
    suspend fun setBackupUserDisplayName(name: String?)
    suspend fun getBackupContentHash(): String?
    suspend fun setBackupContentHash(hash: String?)
    suspend fun getBackupLastError(): String?
    suspend fun setBackupLastError(code: String?)
    suspend fun clearBackupKeys()
}
```

Implementada por `DataStorePrefsStorage` en `:compose` (adicionalmente a `PrefsStorage`).

### `CloudProviderType` — enum de proveedores

```kotlin
enum class CloudProviderType(val id: String) {
    GOOGLE_DRIVE("google_drive"),
    DROPBOX("dropbox"),
}
```

El `id` es el valor que se persiste en `PrefsStorage`.

### `BackupProviderRegistry` — resolución del proveedor activo

```kotlin
class BackupProviderRegistry(
    private val googleDrive: GoogleDriveBackupProvider,
    private val dropbox: DropboxBackupProvider,
) {
    fun get(type: CloudProviderType): CloudBackupProvider = when (type) {
        CloudProviderType.GOOGLE_DRIVE -> googleDrive
        CloudProviderType.DROPBOX -> dropbox
    }
}
```

Inyectado vía Koin. `BackupRepositoryImpl` lee el proveedor activo desde `PrefsStorage` y
lo resuelve a través del registry en cada operación. Si `KEY_BACKUP_PROVIDER` está vacío o
no configurado, todas las operaciones devuelven `Err(BackupError.NoProviderSelected)` de
forma inmediata, sin llamar a ningún SDK.

### Interfaz `CloudBackupProvider`

```kotlin
interface CloudBackupProvider {
    val providerType: CloudProviderType

    suspend fun signIn(activity: Activity): Result<CloudUser, BackupError>
    suspend fun signOut(): Result<Unit, BackupError>
    suspend fun isSignedIn(): Boolean
    suspend fun upload(content: String, metadata: BackupMetadata): Result<Unit, BackupError>
    suspend fun download(): Result<BackupFile, BackupError>
    suspend fun getRemoteMetadata(): Result<BackupMetadata?, BackupError>
    suspend fun deleteRemoteFile(): Result<Unit, BackupError>
}
```

Usar el patrón `Result<T, E>` ya establecido en el proyecto (`DataError`).

---

## Diferencias entre proveedores

| Aspecto                     | Google Drive                             | Dropbox                                                           |
|-----------------------------|------------------------------------------|-------------------------------------------------------------------|
| Scope de auth               | `drive.appdata`                          | `files.content.write` + `files.content.read` (App Folder scope)   |
| Ubicación del archivo       | App Data folder (oculto para el usuario) | `/Apps/DrinkWaterApp/drinkwater_backup.json` (visible en Dropbox) |
| Visibilidad para el usuario | No visible en "Mi unidad"                | Visible en su Dropbox                                             |
| Flujo de auth               | Google Sign-In (`GoogleSignIn` SDK)      | OAuth 2.0 PKCE (Dropbox SDK v7+, sin WebView)                     |
| SDK Android                 | `play-services-auth` (ya existe)         | `com.dropbox.core:dropbox-android-sdk`                            |
| Callback de auth            | `ActivityResultContracts`                | `ActivityResultContracts` + redirect URI scheme                   |
| Quota de storage            | 15 GB compartido con cuenta Google       | 2 GB (cuenta gratuita)                                            |
| Nombre del archivo          | `drinkwater_backup.json`                 | `drinkwater_backup.json`                                          |

> El contenido del archivo JSON es **idéntico** en ambos proveedores. Solo difiere dónde se guarda.

---

## Formato del archivo de backup

El archivo se llama `drinkwater_backup.json` en la carpeta privada/app del proveedor elegido.

### Estructura JSON

```json
{
  "metadata": {
    "schemaVersion": 1,
    "appVersionName": "2.0.0-alpha08",
    "appVersionCode": 25,
    "savedAt": "2026-05-26T14:30:00Z",
    "platform": "android",
    "packageId": "com.eahm.drinkwaterapp",
    "provider": "google_drive",
    "userIdHash": "<SHA-256 del email>",
    "deviceModel": "Pixel 8",
    "locale": "es"
  },
  "data": {
    "history": [
      {
        "id": "uuid",
        "drink_amount": 250.0,
        "status": "IN_APP",
        "timestamp": 1748300000000
      }
    ],
    "dailyGoals": [
      {
        "id": "uuid",
        "effective_date": 20000,
        "goal_ml": 2000.0
      }
    ],
    "preferences": {
      "unitPreference": "ML",
      "timeFormat": "SYSTEM",
      "wakeHour": 7,
      "wakeMinute": 0,
      "sleepHour": 22,
      "sleepMinute": 0,
      "intervalHour": 0,
      "intervalMinute": 45,
      "amountPerDrink": 250.0
    }
  }
}
```

> `provider` en metadata indica con qué servicio se creó el backup (informativo, no afecta
> la deserialización). `userIdHash` es SHA-256 del identificador de cuenta; nunca texto plano.

---

## Migraciones de backup

`BackupMigrationManager` maneja la evolución del formato entre versiones de la app.

```kotlin
object BackupMigrationManager {
    const val currentSchema: Int = 1   // incrementar con cada cambio estructural

    fun migrate(raw: JsonObject, fromSchema: Int, toSchema: Int): JsonObject
}
```

> `BackupMigrationManager` es un `object` Kotlin (singleton global). Se pasa directamente a los
> use cases que lo necesitan (`RestoreBackupUseCase`, `CheckBackupStatusUseCase`). En Koin se
> puede registrar como `single<BackupMigrationManager> { BackupMigrationManager }` para facilitar
> el reemplazo en tests.

- Cada migración es `BackupMigration(from: Int, to: Int, migrate: (JsonObject) -> JsonObject)`.
- Se aplican encadenadas: si el archivo tiene `schemaVersion = 1` y la app está en schema 3,
  se aplican 1→2 y luego 2→3.
- Al agregar un campo nuevo a las entidades locales también se debe registrar la migración
  que rellene el default en archivos viejos.
- La validación de compatibilidad se hace **antes** de intentar migrar (ver sección siguiente).

---

## Compatibilidad de versiones en restore

### Regla de decisión

Antes de deserializar o migrar cualquier backup, `RestoreBackupUseCase` compara
`backup.metadata.schemaVersion` con `BackupMigrationManager.currentSchema`:

```
backupSchema < currentSchema  →  migrar hacia adelante y restaurar   ✓
backupSchema == currentSchema →  restaurar directamente               ✓
backupSchema > currentSchema  →  rechazar, mostrar IncompatibleVersionDialog  ✗
```

La tercera rama significa que el backup fue creado con **una versión más nueva de la app**
que la que el usuario tiene instalada. Los datos podrían contener campos que esta versión
desconoce, por lo que restaurar podría corromper la base de datos local. No se intenta la
restauración bajo ninguna circunstancia.

### Código de la validación

```kotlin
class RestoreBackupUseCase(
    private val provider: CloudBackupProvider,
    private val serializer: BackupSerializer,
    private val migrationManager: BackupMigrationManager,
    private val database: AppDatabase,
) {
    suspend operator fun invoke(): Result<Unit, BackupError> {
        val backupFile = provider.download().getOrElse { return Err(it) }
        val remoteSchema = backupFile.metadata.schemaVersion

        if (remoteSchema > migrationManager.currentSchema) {
            return Err(BackupError.IncompatibleVersion(remoteSchema))
        }

        val migratedData = migrationManager.migrate(
            raw = backupFile.rawData,
            fromSchema = remoteSchema,
            toSchema = migrationManager.currentSchema,
        )

        return serializer.deserializeAndImport(migratedData, database)
    }
}
```

### `IncompatibleVersionDialog` — UI

Este diálogo solo aparece cuando `backupSchema > currentSchema`. Se muestra en todos los
contextos donde se intenta restaurar (manual desde Settings, automático al reinstalar).

```
╔══════════════════════════════════════════════╗
║  Actualiza la app para restaurar             ║
╠══════════════════════════════════════════════╣
║  Tu copia de seguridad fue creada con una    ║
║  versión más nueva de la app.                ║
║                                              ║
║  Para no perder datos, actualiza la app y    ║
║  luego vuelve a restaurar.                   ║
║                                              ║
║  Tus datos locales actuales no se            ║
║  modificaron.                                ║
╠══════════════════════════════════════════════╣
║  [Actualizar app]          [Cancelar]        ║
╚══════════════════════════════════════════════╝
```

- "Actualizar app" → abre la ficha de la app en Google Play Store.
- "Cancelar" → cierra el diálogo. No se modifica nada: ni los datos locales ni el backup remoto.
- El diálogo **nunca** ofrece un botón de "restaurar de todas formas". No hay forma de forzar
  una restauración incompatible.

### Dónde aparece este diálogo

| Contexto                                | Disparador                                      | Comportamiento posterior                                                                      |
|-----------------------------------------|-------------------------------------------------|-----------------------------------------------------------------------------------------------|
| Restore manual (Settings)               | Usuario toca "Restaurar desde la nube"          | Muestra diálogo; si cancela, vuelve a Settings sin cambios                                    |
| Restauración al reinstalar (silenciosa) | `CheckBackupStatusUseCase` detecta schema mayor | Muestra diálogo antes de llegar al home; si cancela, entra al home con datos locales intactos |

> **Importante:** La comprobación de `schemaVersion` ocurre **antes** de mostrar
> `RestoreConflictDialog`. Si el backup es incompatible, el usuario ve `IncompatibleVersionDialog`
> directamente y **nunca** llega al diálogo de conflicto. `RestoreConflictDialog` solo aparece
> cuando el backup ya pasó la comprobación (`backupSchema <= currentSchema`).

### Strings para este caso

```xml

<string name="backup_incompatible_title">Actualiza la app para restaurar</string><string
name="backup_incompatible_body">Tu copia de seguridad fue creada con una versión más nueva de la
app (versión interna %1$d).\n\nActualiza la app y luego vuelve a restaurar. Tus datos actuales no
se modificaron.
</string><!-- %1$d = schemaVersion del backup remoto -->
<string name="backup_incompatible_update">Actualizar app</string><string
name="backup_incompatible_cancel">Cancelar
</string>
```

### Archivo a crear

| Archivo                                                | Descripción                                          |
|--------------------------------------------------------|------------------------------------------------------|
| `feature/settings/backup/IncompatibleVersionDialog.kt` | Diálogo de versión incompatible con CTA a Play Store |

---

## Feature flag

`FEATURE_CLOUD_BACKUP` se define directamente en **`compose/build.gradle.kts`** dentro de
`buildTypes {}`, **no** en `AndroidBuildConfigConventionPlugin.kt`:

```kotlin
// compose/build.gradle.kts
buildTypes {
    debug {
        buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "true")
    }
    // En staging / QA / release (cada buildType existente):
    // buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "false")
}
```

Y en `FeatureFlags.kt`:

```kotlin
val cloudBackupEnabled: Boolean = BuildConfig.FEATURE_CLOUD_BACKUP
```

`DROPBOX_APP_KEY` se define en **`backup/build.gradle.kts`** (con
`buildFeatures { buildConfig = true }`),
leyendo `dropbox.app.key` de `local.properties`. El mismo valor se expone como
`manifestPlaceholders["dropboxAppKey"]` para resolver el scheme `db-${dropboxAppKey}` en el
manifest del módulo `:backup`.

Todo el código de backup se compila siempre, pero la UI solo se muestra si
`FeatureFlags.cloudBackupEnabled == true`. Esto evita dead-code en release mientras el
feature está en pruebas.

---

## Selector de proveedor (`ProviderPickerSheet`)

Se muestra como Bottom Sheet al tocar "Activar copia de seguridad" (tanto en onboarding como
en settings). No hay un proveedor predeterminado — el usuario elige explícitamente.

```
╔══════════════════════════════════════════╗
║  ¿Dónde guardar tu copia de seguridad?   ║
╠══════════════════════════════════════════╣
║  [icono Google Drive]  Google Drive      ║
║  Almacenamiento privado de la app        ║
╠──────────────────────────────────────────╣
║  [icono Dropbox]       Dropbox           ║
║  Visible en tu carpeta de Dropbox        ║
╠══════════════════════════════════════════╣
║              Cancelar                    ║
╚══════════════════════════════════════════╝
```

Cada opción muestra nombre del proveedor, icono oficial y una línea con la ubicación del archivo
(para que el usuario sepa dónde quedará). Al seleccionar, se inicia el flujo de auth del proveedor
elegido.

---

## Nuevo paso en onboarding

Se inserta entre `Celebrations` (paso 9) y `Summary` (paso 10).
Total de pasos: 10 → **11** (solo cuando el feature flag está habilitado).

### `OnboardingSteps.CloudBackup` (nuevo)

```kotlin
data object CloudBackup : OnboardingSteps   // solo visible si FeatureFlags.cloudBackupEnabled
```

### `byStep` actualizado

```
1  → Intro
2  → Units
3  → DailyGoal
4  → StartTime
5  → EndTime
6  → Interval
7  → Notification
8  → ExactAlarm
9  → Celebrations
10 → CloudBackup  ← nuevo (solo si feature habilitado)
11 → Summary      ← pasa de 10 a 11
```

Cuando el feature flag está desactivado: `byStep(10) → Summary` y `totalSteps = 10` (sin cambio).

### Contenido del paso `StepBackup` — sub-estados

El paso tiene cuatro sub-estados internos manejados por `OnboardingViewModel`.

#### Sub-estado A — Selección inicial

- **Título**: "¿Quieres guardar tu progreso en la nube?"
- Dos tarjetas: Google Drive y Dropbox (mismo diseño que `ChoiceCard`).
- Botón "Conectar" → auth con el proveedor elegido.
- Link "Ahora no" → continúa sin backup.

#### Sub-estado B — No se encontró backup en el proveedor elegido

Ocurre cuando la auth fue exitosa pero `CheckBackupStatusUseCase` no encontra archivo remoto.

- Texto: "No encontramos backup en [Proveedor A]."
- Opción principal: "Continuar con [Proveedor A]" → hace primera sync y avanza al Summary.
- Opción secundaria (link): "¿Tienes backup en [Proveedor B]?" → auth con Proveedor B.
    - `[Proveedor B]` es siempre el otro proveedor (el que el usuario **no** eligió primero).

#### Sub-estado C — Backup encontrado en el otro proveedor

Después de auth exitosa con Proveedor B y backup encontrado allí:

- Texto: "Encontramos un backup en [Proveedor B]."
- **Opción 1** (recomendada): "Migrar backup a [Proveedor A]" → copia el archivo de B a A,
  usa A como proveedor activo. Muestra `MigrationProgressDialog`.
- **Opción 2**: "Usar [Proveedor B] en su lugar" → cambia el proveedor activo a B, restaura
  desde B. No hay copia; simplemente se usa B.
- **Opción 3** (link): "Continuar sin backup" → descarta ambos, avanza sin backup.

#### Sub-estado D — No hay backup en ningún proveedor

- Texto: "No encontramos backups en ningún servicio."
- Botón único: "Continuar" → cierra sesión en **ambos** proveedores (el usuario no tenía backup en
  ninguno; no tiene sentido quedarse conectado sin datos) → avanza al Summary sin backup.

#### Sub-estado C, Opción 3 — "Continuar sin backup"

Al elegir este link (visible en C), el usuario descarta ambos proveedores. La app cierra
sesión en Proveedor A y en Proveedor B y avanza al Summary sin backup configurado.

#### Manejo de errores en auth del segundo proveedor

Si la auth con el segundo proveedor falla o se cancela, volver al Sub-estado B sin perder
la autenticación ya obtenida con el primero.

---

## Pantalla de Settings — sección Cloud Backup

**Si backup está desactivado:**

- Card con icono genérico de nube + "Protege tu progreso en la nube".
- Botón "Activar copia de seguridad" → abre `ProviderPickerSheet`.

**Si backup está activo:**

- Icono + nombre del proveedor activo (Google Drive o Dropbox).
- Nombre de la cuenta conectada (Display Name).
- Última sincronización: fecha y hora / "Nunca sincronizado".
- **Área de estado de sync** — reactiva a `BackupSyncState` (ver sección de concurrencia):
    - `Idle`: botón "Sincronizar ahora" habilitado.
    - `InProgress`: `LinearProgressIndicator` + texto "Sincronizando…" + botón deshabilitado. Se
      muestra tanto si la sync la inició el usuario como si la inició WorkManager en segundo plano.
    - `UpToDate`: texto "Sin cambios, todo está actualizado" (skip por hash idéntico).
    - `Success`: texto "Sincronizado correctamente".
    - `Error`: banner de error (ver sección de manejo de errores).
- Selector de intervalo automático: Diario / Cada 3 días / Semanal / Solo manual.
- Opción "Restaurar desde la nube" (con diálogo de confirmación).
- Botón "Cambiar proveedor" → abre `ChangeProviderSheet` (ver sección de migración).
- Botón destructivo "Desconectar cuenta" (con diálogo de confirmación).

---

## Migración de backup entre proveedores

### Concepto clave

La migración es una **copia de archivo**, no una importación de datos. `MigrateBackupUseCase`
descarga el JSON crudo del proveedor origen y lo sube sin modificar al proveedor destino.
No hay deserialización ni validación de schema: se copian los bytes tal cual. La compatibilidad
de schema solo importa cuando se restaura a la base de datos local.

```kotlin
class MigrateBackupUseCase(
    private val registry: BackupProviderRegistry,
    private val prefs: PrefsStorage,
    private val mutex: Mutex,
) {
    suspend operator fun invoke(
        source: CloudProviderType,
        destination: CloudProviderType,
    ): Result<Unit, BackupError>
}
```

**Flujo interno:**

```
1. Precondición: source != destination (el caller garantiza esto; IllegalArgumentException si no)
2. tryLock(mutex) → si falla: return AlreadyInProgress
3. Emit BackupSyncState.InProgress
4. source provider: download() → rawContent: String
5. Si FileNotFound en origen: return Err(BackupError.MigrationNoSourceBackup)
6. destination provider: upload(rawContent, metadata actualizado)
   — si falla: el proveedor activo NO cambia; return Err(error)
7. Actualizar KEY_BACKUP_PROVIDER → destination.id
8. Actualizar KEY_BACKUP_CONTENT_HASH con hash del rawContent
9. Actualizar KEY_BACKUP_LAST_SYNC_AT
10. Emit BackupSyncState.Success
11. return Ok
```

La metadata se actualiza al subir: `provider` pasa a ser el destino, `savedAt` se actualiza
al momento de la copia. El resto de los campos del bloque `metadata` se preservan del archivo
original (schemaVersion, appVersionName, etc.).

### `ChangeProviderSheet` — UX en Settings

Se abre al tocar "Cambiar proveedor". Muestra el contexto actual y ofrece dos caminos:

```
╔══════════════════════════════════════════════════╗
║  Cambiar proveedor de copia de seguridad         ║
╠══════════════════════════════════════════════════╣
║  Actualmente usas: [icono] Google Drive          ║
╠══════════════════════════════════════════════════╣
║  ● Migrar backup a Dropbox          (recomendado)║
║    Tu backup se copia a Dropbox. La             ║
║    copia en Google Drive no se elimina.          ║
╠──────────────────────────────────────────────────╣
║  ○ Cambiar sin migrar                            ║
║    Empiezas desde cero en Dropbox.               ║
║    Tu backup en Google Drive queda allí.         ║
╠══════════════════════════════════════════════════╣
║  Cancelar                                        ║
╚══════════════════════════════════════════════════╝
```

- El proveedor destino se muestra directamente (solo hay dos proveedores; si el activo es
  Drive, el destino es Dropbox y viceversa). No se abre `ProviderPickerSheet` aquí.
- Si el usuario elige "Migrar backup": auth con el destino → `MigrationProgressDialog` →
  resultado. Si la auth falla o se cancela, el sheet muestra el error y el proveedor activo
  **no cambia** (el usuario sigue con el proveedor original).
- Si elige "Cambiar sin migrar": auth con el destino → si auth exitosa: `ConnectBackupUseCase`
  con el nuevo proveedor (que incluye desconectar el anterior y hacer primera sync). Si la auth
  falla o se cancela, el sheet muestra el error y el proveedor activo no cambia.

### `MigrationProgressDialog` — UX durante la copia

```
╔══════════════════════════════════════════════════╗
║  Migrando tu backup…                             ║
╠══════════════════════════════════════════════════╣
║  Copiando de Google Drive a Dropbox              ║
║  ████████████░░░░░░░░  descargando…              ║
╠══════════════════════════════════════════════════╣
║  (no cancelable mientras esté en progreso)       ║
╚══════════════════════════════════════════════════╝
```

Progreso en dos fases (indeterminado, ya que no hay tamaño previo conocido):

- Fase 1: "Descargando de [origen]…"
- Fase 2: "Subiendo a [destino]…"

Al completar, el diálogo cambia a pantalla de éxito:

```
╔══════════════════════════════════════════════════╗
║  ✓ Backup migrado a Dropbox                      ║
╠══════════════════════════════════════════════════╣
║  La copia en Google Drive sigue guardada allí.   ║
║  Puedes eliminarla desde Google Drive si ya no   ║
║  la necesitas.                                   ║
╠══════════════════════════════════════════════════╣
║  [Listo]                                         ║
╚══════════════════════════════════════════════════╝
```

Si la migración falla (error de red, quota en destino, etc.), el diálogo muestra el error
correspondiente con botón "Reintentar" y "Cancelar". El proveedor activo **no cambia** hasta
que la migración complete con éxito.

### Errores específicos de migración

Agregar a `BackupError`:

```kotlin
data object MigrationNoSourceBackup : BackupError  // no hay archivo en el origen para copiar
```

El resto de errores de migración reutilizan los existentes: `NetworkUnavailable`,
`QuotaExceeded(destination)`, `UploadFailed`, `DownloadFailed`, `AuthFailed`.

Agregar a `BackupErrorContext`:

```kotlin
enum class BackupErrorContext { MANUAL_SYNC, AUTO_SYNC, ONBOARDING, RESTORE, MIGRATION }
```

El mapper mapea `MigrationNoSourceBackup` a:
> "No encontramos un backup en [origen] para copiar. El proveedor puede haber cambiado o
> el archivo fue eliminado."

### Strings de migración

```xml
<!-- ChangeProviderSheet -->
<string name="backup_change_provider_title">Cambiar proveedor de copia de seguridad</string><string
name="backup_change_provider_current">Actualmente usas: %1$s
</string><string name="backup_change_provider_migrate_title">Migrar backup a %1$s</string><string
name="backup_change_provider_migrate_desc">Tu backup se copia a %1$s. La copia en %2$s no se
elimina.
</string><string name="backup_change_provider_switch_title">Cambiar sin migrar</string><string
name="backup_change_provider_switch_desc">Empiezas desde cero en %1$s. Tu backup en %2$s queda allí.
</string><string name="backup_change_provider_cancel">Cancelar</string>

    <!-- MigrationProgressDialog -->
<string name="backup_migration_title">Migrando tu backup…</string><string
name="backup_migration_copying">Copiando de %1$s a %2$s
</string><string name="backup_migration_phase_download">Descargando de %1$s…</string><string
name="backup_migration_phase_upload">Subiendo a %1$s…
</string><string name="backup_migration_success_title">Backup migrado a %1$s</string><string
name="backup_migration_success_body">La copia en %2$s sigue guardada allí. Puedes eliminarla desde
%2$s si ya no la necesitas.
</string><string name="backup_migration_done">Listo</string><string name="backup_migration_retry">
Reintentar
</string><string name="backup_migration_cancel">Cancelar</string>

    <!-- Onboarding — sub-estado B: no backup en proveedor elegido -->
<string name="backup_onb_no_backup_found">No encontramos backup en %1$s.</string><string
name="backup_onb_continue_with_provider">Continuar con %1$s
</string><string name="backup_onb_check_other_provider">¿Tienes backup en %1$s?</string>

    <!-- Onboarding — sub-estado C: backup encontrado en otro proveedor -->
<string name="backup_onb_found_in_other">Encontramos un backup en %1$s.</string><string
name="backup_onb_migrate_to_chosen">Migrar backup a %1$s
</string><string name="backup_onb_use_other_provider">Usar %1$s en su lugar</string><string
name="backup_onb_continue_no_backup">Continuar sin backup
</string>

    <!-- Onboarding — sub-estado D: sin backup en ningún proveedor -->
<string name="backup_onb_no_backup_anywhere">No encontramos backups en ningún servicio.</string>

    <!-- Error: sin backup en origen para migrar -->
<string name="backup_error_migration_no_source">No encontramos un backup en %1$s para copiar. El
archivo puede haber sido eliminado.
</string>
```

---

## Sync automático — WorkManager

```kotlin
class CloudBackupWorker(
    context: Context,
    params: WorkerParameters,
    private val syncBackupUseCase: SyncBackupUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = TODO("invoke SyncBackupUseCase")
}
```

- **Nombres únicos** (dos constantes separadas para evitar que el trabajo manual cancele el
  periódico):
  ```
  BACKUP_SYNC_WORK_PERIODIC = "cloud_backup_sync_periodic"
  BACKUP_SYNC_WORK_MANUAL   = "cloud_backup_sync_manual"
  ```
- Al encolar el sync manual:
  `WorkManager.enqueueUniqueWork(BACKUP_SYNC_WORK_MANUAL, ExistingWorkPolicy.KEEP, oneTimeRequest)` —
  si ya hay un trabajo manual en curso, la solicitud se descarta.
- Al encolar el periódico:
  `enqueueUniquePeriodicWork(BACKUP_SYNC_WORK_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodicRequest)`.
- Constraints: `NetworkType.CONNECTED`. (Nota: incluye redes metered; red unmetered se puede añadir
  como opción en Settings en una versión futura.)
- `CloudBackupWorker.doWork()` llama `SyncBackupUseCase`, que persiste `lastSyncAt` y limpia
  `KEY_BACKUP_LAST_ERROR` al completar. El worker solo convierte el `Result` a `Result.success()`
  o `Result.failure()`.
- Política de reintento según error:
    - **Error genérico** (`UploadFailed`, `NetworkUnavailable`): `Result.retry()` con backoff
      exponencial (máx 3 intentos).
    - **`QuotaExceeded`**: `Result.failure()` — no reintentar (no se resolverá sola).
    - **`AuthFailed` / `NotSignedIn`**: `Result.failure()` — no reintentar (requiere acción del
      usuario). Escribe `KEY_BACKUP_LAST_ERROR = "auth_failed"` para el banner en Settings.
- Al desactivar backup o cambiar de proveedor:
  ```
  WorkManager.cancelUniqueWork(BACKUP_SYNC_WORK_PERIODIC)
  WorkManager.cancelUniqueWork(BACKUP_SYNC_WORK_MANUAL)
  ```

---

## Concurrencia y detección de cambios

### Prevención de syncs simultáneas

`SyncBackupUseCase` usa un `Mutex` para garantizar que solo una sync se ejecute a la vez,
independientemente de si la lanzó el usuario o WorkManager:

```kotlin
class SyncBackupUseCase(private val mutex: Mutex /* + demás dependencias */) {
    suspend operator fun invoke(): Result<SyncResult, BackupError> {
        if (!mutex.tryLock()) return Ok(SyncResult.AlreadyInProgress)
        try {
            // serializar → comparar hash → subir si hay cambios
        } finally {
            mutex.unlock()
        }
    }
}

sealed interface SyncResult {
    data object Uploaded : SyncResult
    data object SkippedNoChanges : SyncResult
    data object AlreadyInProgress : SyncResult
}
```

El `Mutex` se registra como `single { Mutex() }` en el módulo Koin de backup y se inyecta
en `SyncBackupUseCase`. Al ser un `single`, es la misma instancia para WorkManager y para
las llamadas desde ViewModel.

> **Nota sobre procesos:** El `Mutex` en memoria solo protege concurrencia dentro del mismo
> proceso Android. `CloudBackupWorker` no configura un proceso separado (`android:process`) en
> `AndroidManifest.xml`, así que WorkManager corre en el proceso principal. El nombre único de
> trabajo (`ExistingWorkPolicy.KEEP`) actúa como guarda adicional a nivel de sistema operativo
> para evitar encolar duplicados desde distintos contextos.

### `BackupSyncState` — estado reactivo para la UI

```kotlin
sealed interface BackupSyncState {
    data object Idle : BackupSyncState
    data object InProgress : BackupSyncState
    data object UpToDate : BackupSyncState
    data class Success(val syncedAt: Instant) : BackupSyncState
    data class Error(val error: BackupError) : BackupSyncState
}
```

Expuesto desde `BackupRepository` como `val syncState: StateFlow<BackupSyncState>`.
`SyncBackupUseCase` actualiza este flow al adquirir/liberar el mutex y al concluir la
operación.

**Transiciones de estados terminales:**

- `Success` y `UpToDate` son estados transitorios. El ViewModel de Settings los muestra durante
  3 segundos y luego los limpia a `Idle` con un `delay(3_000)` + emit `Idle`.
- `Error` persiste hasta que una sync exitosa la reemplace con `Success` o `UpToDate`.
- La "última sincronización" se muestra siempre desde `KEY_BACKUP_LAST_SYNC_AT` (no desde el
  estado en memoria), de modo que el timestamp persiste entre sesiones.

### Observar WorkManager desde el ViewModel de Settings

Para cubrir el caso donde WorkManager arranca mientras la app está en segundo plano (y el
mutex en memoria ya no aplica), el ViewModel combina ambas fuentes:

```kotlin
val isSyncing: StateFlow<Boolean> = combine(
    backupRepository.syncState.map { it is BackupSyncState.InProgress },
    workManager.getWorkInfosForUniqueWorkFlow(BACKUP_SYNC_WORK_PERIODIC)
        .map { infos -> infos.any { it.state == WorkInfo.State.RUNNING } },
    workManager.getWorkInfosForUniqueWorkFlow(BACKUP_SYNC_WORK_MANUAL)
        .map { infos -> infos.any { it.state == WorkInfo.State.RUNNING } },
) { repoSyncing, periodicRunning, manualRunning -> repoSyncing || periodicRunning || manualRunning }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
```

La UI reacciona a `isSyncing` para mostrar el `LinearProgressIndicator` y deshabilitar el
botón "Sincronizar ahora".

---

### Detección de cambios antes de subir

Antes de hacer el upload, `SyncBackupUseCase` calcula un hash del contenido serializado y
lo compara con el del último backup exitoso guardado en `PrefsStorage`. Si son iguales, no
hay nada nuevo que subir.

**Flujo completo de `SyncBackupUseCase`:**

```
1. tryLock(mutex) → si falla: return AlreadyInProgress
2. Emit BackupSyncState.InProgress
3. Serializar datos locales → content: String
4. Calcular newHash = SHA-256(content).take(32)
5. Leer storedHash de PrefsStorage
6. Si newHash == storedHash:
       → Emit BackupSyncState.UpToDate
       → return SkippedNoChanges          (sin llamada de red)
7. Upload(content) → si falla: Emit Error, return Err
8. Guardar newHash en KEY_BACKUP_CONTENT_HASH
9. Guardar now() en KEY_BACKUP_LAST_SYNC_AT
10. Emit BackupSyncState.Success(syncedAt = now())
11. return Uploaded
```

El hash usa SHA-256 truncado a los primeros 32 caracteres hex (16 bytes) — suficiente para
detectar cualquier cambio, sin costo criptográfico relevante dado el tamaño del archivo.

---

## `ConnectBackupUseCase` — flujo y persistencia

**Flujo interno:**

```
1. Si hay proveedor activo previo: signOut() del proveedor anterior
   (los datos remotos de ese proveedor no se borran)
2. signIn(provider) → cloudUser: CloudUser
3. Primera SyncBackupUseCase → si falla QuotaExceeded: desconectar y retornar error
4. Persistir en PrefsStorage:
   KEY_BACKUP_PROVIDER         = provider.id
   KEY_BACKUP_USER_DISPLAY_NAME = cloudUser.displayName
   KEY_BACKUP_ENABLED          = true   ← solo se escribe true aquí
   KEY_BACKUP_CONTENT_HASH     = hash de los datos subidos (lo persiste SyncBackupUseCase)
   KEY_BACKUP_LAST_SYNC_AT     = now()  (lo persiste SyncBackupUseCase)
```

`KEY_BACKUP_ENABLED` solo se pone a `true` cuando `ConnectBackupUseCase` completa con éxito.
Se pone a `false` (o se borra) en `DisconnectBackupUseCase`.

---

## Almacenamiento de estado en `PrefsStorage`

```
KEY_BACKUP_ENABLED: Boolean              // ¿El usuario activó backup? true solo tras ConnectBackupUseCase exitoso
KEY_BACKUP_PROVIDER: String              // "google_drive" | "dropbox"
KEY_BACKUP_LAST_SYNC_AT: Long?           // Epoch ms de la última sync exitosa
KEY_BACKUP_AUTO_INTERVAL_DAYS: Int       // 1 | 3 | 7 | 0 (solo manual)
KEY_BACKUP_USER_DISPLAY_NAME: String?    // Nombre de cuenta; persiste ConnectBackupUseCase
KEY_BACKUP_CONTENT_HASH: String?         // Hash SHA-256 truncado del último backup subido
KEY_BACKUP_LAST_ERROR: String?           // null | "quota_exceeded" | "auth_failed" | "network" | "upload_failed"
```

---

## Flujo de restauración al reinstalar

```
App inicio
    ↓
onboardingCompleted == false  →  onboarding normal
onboardingCompleted == true y backupEnabled == false  →  home
onboardingCompleted == true y backupEnabled == true
    ↓
CheckBackupStatusUseCase (con el proveedor guardado)
    ↓ hay datos remotos?
    ├── NO  →  home normal
    └── SÍ
          ↓ ¿backupSchema > currentSchema?
          ├── SÍ →  IncompatibleVersionDialog → cancelar → home (datos locales intactos)
          └── NO (backupSchema <= currentSchema → se puede restaurar)
                ↓ hay datos locales?
                ├── NO  →  restaurar silenciosamente (con migraciones si aplica), ir a home
                └── SÍ  →  mostrar RestoreConflictDialog
                              ├── "Usar nube"   →  migrar si aplica → sobreescribir local, ir a home
                              ├── "Usar local"  →  subir datos locales al cloud, ir a home
                              └── "Cancelar"    →  ir a home sin cambios
```

La comprobación de `schemaVersion` ocurre **siempre antes** de mostrar cualquier opción
de restauración. El usuario nunca ve el `RestoreConflictDialog` para un backup que la app
no puede leer.

---

## `CheckBackupStatusUseCase` — tipo de retorno

```kotlin
data class RemoteBackupStatus(
    val metadata: BackupMetadata,
    val isCompatible: Boolean,   // metadata.schemaVersion <= BackupMigrationManager.currentSchema
)

class CheckBackupStatusUseCase(
    private val registry: BackupProviderRegistry,
    private val prefs: PrefsStorage,
    private val migrationManager: BackupMigrationManager,
) {
    /**
     * Returns [Ok(RemoteBackupStatus)] si existe backup remoto para el proveedor activo.
     * Returns [Ok(null)] si no existe archivo remoto (FileNotFound).
     * Returns [Err] en error de red, auth, o si no hay proveedor seleccionado.
     */
    suspend operator fun invoke(): Result<RemoteBackupStatus?, BackupError>
}
```

Si `isCompatible == false`, la capa de presentación muestra `IncompatibleVersionDialog` y
no intenta la restauración.

---

## Manejo de errores — `BackupError`

```kotlin
sealed interface BackupError : DataError {
    data object NotSignedIn : BackupError
    data object NetworkUnavailable : BackupError
    data object FileNotFound : BackupError
    data object UploadFailed : BackupError
    data object DownloadFailed : BackupError
    data class IncompatibleVersion(val remoteSchema: Int) : BackupError
    data class MigrationFailed(val cause: Throwable) : BackupError
    data object AuthCancelled : BackupError
    data object AuthFailed : BackupError
    data class QuotaExceeded(val provider: CloudProviderType, val fileSizeBytes: Long) : BackupError
    data object NoProviderSelected : BackupError
    data object ProviderNotAvailable : BackupError
    data object MigrationNoSourceBackup : BackupError
}
```

Cada error mapea a un `UiText`. A continuación se detalla la superficie de cada error según
el contexto en que ocurre.

---

### Tabla de superficie de errores

| Error                        | Sync manual                    | Sync automático                                 | Onboarding                  | Restaurar                           |
|------------------------------|--------------------------------|-------------------------------------------------|-----------------------------|-------------------------------------|
| `NetworkUnavailable`         | Snackbar inline                | Silencioso + flag en prefs + banner en Settings | Mensaje inline + reintentar | Snackbar + reintentar               |
| `AuthFailed` / `NotSignedIn` | Snackbar + CTA "Reconectar"    | Silencioso + flag + banner en Settings          | Mensaje inline + saltar     | Snackbar + CTA "Reconectar"         |
| `QuotaExceeded`              | `QuotaExceededDialog` con link | Flag + banner en Settings                       | Mensaje inline + saltar     | N/A (no sube)                       |
| `UploadFailed`               | Snackbar genérico              | Silencioso + reintento WorkManager              | Mensaje inline + saltar     | N/A                                 |
| `DownloadFailed`             | N/A                            | N/A                                             | N/A                         | Snackbar + reintentar               |
| `FileNotFound`               | Snackbar informativo           | N/A                                             | N/A                         | Mensaje: "No hay backup disponible" |
| `IncompatibleVersion`        | `IncompatibleVersionDialog`    | `IncompatibleVersionDialog`                     | N/A (no se restaura aquí)   | `IncompatibleVersionDialog`         |
| `MigrationFailed`            | Diálogo + contactar soporte    | Diálogo                                         | N/A                         | Diálogo + contactar soporte         |
| `AuthCancelled`              | Ignorar                        | N/A                                             | Volver al paso sin acción   | N/A                                 |

**"Silencioso"** significa que WorkManager no interrumpe al usuario. El error se escribe en
`KEY_BACKUP_LAST_ERROR` de `PrefsStorage`. La pantalla de Settings lee esa clave y muestra
un banner mientras el error no se resuelva. Al completar una sync exitosa, el flag se limpia.

---

### `QuotaExceeded` — flujo detallado

#### Cálculo del tamaño real del archivo

La cadena JSON ya está en memoria cuando se intenta el upload. Antes de llamar a
`provider.upload()`, `SyncBackupUseCase` calcula el tamaño exacto en bytes:

```kotlin
val content: String = serializer.serialize(snapshot)
val fileSizeBytes = content.toByteArray(Charsets.UTF_8).size.toLong()

val uploadResult = provider.upload(content, metadata)
if (uploadResult is Err && uploadResult.error is BackupError.QuotaExceeded) {
    return Err(BackupError.QuotaExceeded(provider = activeProvider, fileSizeBytes = fileSizeBytes))
}
```

No hay I/O adicional: la serialización ya ocurrió y la cadena está en heap. El tamaño se
formatea en `BackupErrorMapper` como una cadena legible ("48 KB", "1,2 MB") antes de
construir el `UiText`. El diálogo muestra el valor real, no un tope fijo.

#### Contexto 1 — Sync manual (Settings)

Mostrar `QuotaExceededDialog` con el tamaño real del archivo:

```
╔════════════════════════════════════════════╗
║  Sin espacio en Google Drive               ║
╠════════════════════════════════════════════╣
║  Tu copia de seguridad ocupa 48 KB,        ║
║  pero tu Google Drive está lleno.          ║
║                                            ║
║  Libera espacio en Google Drive para que   ║
║  tu progreso pueda guardarse.              ║
║                                            ║
║  Tus datos locales están seguros.          ║
╠════════════════════════════════════════════╣
║  [Gestionar espacio]      [Entendido]      ║
╚════════════════════════════════════════════╝
```

- "Gestionar espacio" → abre la URL de gestión del proveedor en el navegador del sistema.
- "Entendido" → cierra el diálogo. El backup queda pendiente; el usuario puede reintentar
  manualmente cuando haya liberado espacio.

URLs de gestión de espacio:

- Google Drive: `https://one.google.com/storage`
- Dropbox: `https://www.dropbox.com/account/plan`

Estas URLs se definen como constantes en cada `BackupProvider` y se exponen a través de
`CloudBackupProvider.storageManagementUrl: String`.

#### Contexto 2 — Sync automático (WorkManager en background)

No se muestra ningún diálogo (la app puede estar cerrada). En cambio:

1. WorkManager escribe `BackupErrorCode.QUOTA_EXCEEDED` en `KEY_BACKUP_LAST_ERROR`.
2. WorkManager **no** reintenta (reintentar no servirá hasta que el usuario libere espacio;
   el backoff exponencial solo desperdiciaría batería).
3. La próxima vez que el usuario abra Settings → sección Backup, se muestra un banner de
   advertencia:

```
⚠ Sin espacio en Dropbox — toca para saber más
```

Tocar el banner abre `QuotaExceededDialog` con el mismo contenido que en el caso manual.
El banner desaparece en cuanto una sync automática o manual complete sin error.

#### Contexto 3 — Onboarding (primer sync tras conectar)

El usuario acaba de autenticarse y la primera sync falla por quota. Mostrar mensaje inline
en el paso `StepBackup`, debajo del indicador de carga:

```
Tu [proveedor] no tiene espacio suficiente. Tu progreso
se guardará solo localmente por ahora. Puedes activar
el backup más tarde desde Ajustes cuando liberes espacio.
```

- Botón único: "Continuar" → avanza al Summary.
- El proveedor queda **desconectado** (no tiene sentido quedarse conectado si no hay espacio).
  El usuario lo puede reconectar desde Settings cuando haya liberado espacio.

---

### Estado de error persistido (`PrefsStorage`)

`KEY_BACKUP_LAST_ERROR` (ya definida en la sección de PrefsStorage) solo se escribe durante
syncs automáticas (background). Las syncs manuales y de onboarding muestran el error
directamente en UI sin pasar por prefs. Se limpia cada vez que una sync completa sin error.

---

### Strings de errores (sección ampliada)

Añadir a los strings existentes del plan:

```xml
<!-- Quota exceeded — diálogo
     %1$s = nombre del proveedor (ej. "Google Drive")
     %2$s = tamaño formateado del archivo (ej. "48 KB") -->
<string name="backup_error_quota_title">Sin espacio en %1$s</string><string
name="backup_error_quota_body">Tu copia de seguridad ocupa %2$s, pero tu %1$s está lleno.\n\nLibera
espacio en %1$s para que tu progreso pueda guardarse. Tus datos locales están seguros.
</string><string name="backup_error_quota_manage_space">Gestionar espacio</string><string
name="backup_error_quota_understood">Entendido
</string>

    <!-- Quota exceeded — banner en Settings (sync background fallida)
         %1$s = nombre del proveedor -->
<string name="backup_error_quota_banner">Sin espacio en %1$s. Toca para saber más.</string>

    <!-- Quota exceeded — inline en onboarding
         %1$s = nombre del proveedor -->
<string name="backup_error_quota_onboarding">%1$s no tiene espacio suficiente. Tus datos se
guardarán solo localmente por ahora.
</string>

    <!-- Auth expirada — banner en Settings -->
<string name="backup_error_auth_banner">La sesión de %1$s expiró. Reconecta tu cuenta para continuar
sincronizando.
</string><string name="backup_error_auth_reconnect">Reconectar cuenta</string>

    <!-- Sin red — banner en Settings -->
<string name="backup_error_network_banner">Sin conexión. Se sincronizará cuando vuelva la red.
</string>

    <!-- Versión incompatible — diálogo (usar strings backup_incompatible_* definidos en sección
         "Compatibilidad de versiones en restore"; no duplicar aquí) -->

    <!-- Migración fallida — diálogo -->
<string name="backup_error_migration_title">Error al procesar el backup</string><string
name="backup_error_migration_body">No se pudo leer el archivo de backup. Por favor contacta soporte
con el código: MIGRATION_FAILED.
</string><string name="backup_error_migration_contact">Contactar soporte</string><string
name="backup_error_migration_cancel">Cancelar
</string>
```

> `%1$s` recibe el nombre del proveedor (p. ej. `"Google Drive"` o `"Dropbox"`). Se
> formatea en `BackupErrorMapper` antes de construir el `UiText`.

---

### `BackupErrorMapper`

Clase responsable de convertir `BackupError` a `UiText` según el contexto:

```kotlin
object BackupErrorMapper {
    fun toUiText(error: BackupError, context: BackupErrorContext): UiText
}

enum class BackupErrorContext { MANUAL_SYNC, AUTO_SYNC, ONBOARDING, RESTORE, MIGRATION }
```

Para `QuotaExceeded`, el mapper formatea `fileSizeBytes` en una cadena legible antes de
construir el `UiText`:

```kotlin
when (error) {
    is BackupError.QuotaExceeded -> {
        val providerName = error.provider.displayName()      // ej. "Google Drive" o "Dropbox"
        val sizeLabel = formatFileSize(error.fileSizeBytes)  // ej. "48 KB" o "1,2 MB"
        UiText.StringResource(R.string.backup_error_quota_body, providerName, sizeLabel)
    }
    else -> { /* otros errores */
    }
}
```

#### `formatFileSize(bytes: Long): String`

Función pura en el mapper (sin dependencia de `Context`):

```kotlin
private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024 -> "${bytes / 1024} KB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
}
```

Umbral de KB: cualquier valor < 1 MB se muestra en KB redondeado al entero más cercano.
Para los tamaños esperados del backup (< 2 MB en práctica), el resultado siempre será
en KB salvo casos extremos con años de historial muy denso.

---

## Playground en Dev

Nueva entrada en el menú dev: **"Cloud Backup"**

```
BackupDevScreen
  ├── Estado actual (enabled, provider, last sync, user, schemaVersion)
  │
  ├── ── Google Drive ──
  ├── [Botón] Iniciar sesión con Google Drive
  ├── [Botón] Cerrar sesión de Google Drive
  ├── [Botón] Sync manual → Google Drive
  ├── [Botón] Restaurar desde Google Drive
  ├── [Botón] Ver metadata remota (Google Drive)
  ├── [Botón] Eliminar archivo remoto (Google Drive)
  │
  ├── ── Dropbox ──
  ├── [Botón] Iniciar sesión con Dropbox
  ├── [Botón] Cerrar sesión de Dropbox
  ├── [Botón] Sync manual → Dropbox
  ├── [Botón] Restaurar desde Dropbox
  ├── [Botón] Ver metadata remota (Dropbox)
  ├── [Botón] Eliminar archivo remoto (Dropbox)
  │
  ├── ── Migración entre proveedores ──
  ├── [Botón] Migrar Google Drive → Dropbox
  ├── [Botón] Migrar Dropbox → Google Drive
  ├── [Botón] Simular MigrationNoSourceBackup (origen sin archivo)
  ├── [Botón] Simular fallo en descarga durante migración
  ├── [Botón] Simular QuotaExceeded en destino durante migración
  │
  ├── ── Simulaciones ──
  ├── [Botón] Simular conflicto local vs. nube
  ├── [Botón] Corromper backup (schema viejo → migración hacia adelante)
  ├── [Botón] Corromper backup (schema futuro → IncompatibleVersion)
  ├── [Botón] Simular fallo de red
  ├── [Botón] Simular QuotaExceeded en sync normal
  │
  └── [Log] Output de cada operación en pantalla (scrollable)
```

Patrón MVI estándar: `BackupDevState`, `BackupDevAction`, `BackupDevEvent`,
`BackupDevViewModel`, `BackupDevScreen`, `BackupDevRoot`.

---

## Dependencias a agregar

```toml
# gradle/libs.versions.toml

[versions]
dropbox-sdk = "7.0.0"
# Google Sign-In y play-services-auth ya existen en el proyecto

[libraries]
dropbox-android = { module = "com.dropbox.core:dropbox-android-sdk", version.ref = "dropbox-sdk" }
dropbox-core = { module = "com.dropbox.core:dropbox-core-sdk", version.ref = "dropbox-sdk" }
```

> Para Google Drive **no** se necesita el cliente Java pesado (`google-api-services-drive`).
> Se usa la Drive REST API directamente con el token OAuth de `GoogleSignIn` mediante
> llamadas HTTP con el cliente Ktor ya existente. Esto evita ~4 MB de dependencias transitivas.

> Para Dropbox: el SDK v7+ incluye el flujo OAuth 2.0 PKCE nativo en Android sin WebView.
> Requiere registrar un `redirect_uri` con un scheme custom en `AndroidManifest.xml`.

---

## Traducciones necesarias

Agregar a `compose/src/main/res/values/strings.xml` y traducir a todos los idiomas soportados
(en, es, fr, de, it, pt, ru, nl, da, ja, ko, zh, hi, ar, af, az, uk, sr).

### Strings requeridos

```xml
<!-- Selector de proveedor -->
<string name="backup_picker_title">¿Dónde guardar tu copia de seguridad?</string><string
name="backup_picker_google_drive_name">Google Drive
</string><string name="backup_picker_google_drive_desc">Almacenamiento privado de la app
</string><string name="backup_picker_dropbox_name">Dropbox</string><string
name="backup_picker_dropbox_desc">Visible en tu carpeta de Dropbox
</string><string name="backup_picker_cancel">Cancelar</string>

    <!-- Onboarding step -->
<string name="backup_onb_title">Guarda tu progreso</string><string name="backup_onb_subtitle">
Sincroniza tus datos con la nube para no perder tu historial si cambias de dispositivo.
</string><string name="backup_onb_btn_connect">Elegir servicio de nube</string><string
name="backup_onb_btn_skip">Ahora no
</string><string name="backup_onb_connecting">Conectando…</string><string name="backup_onb_success">
¡Listo! Tus datos están respaldados.
</string><string name="backup_onb_error">No se pudo conectar. Puedes activarlo más tarde en
Ajustes.
</string>

    <!-- Settings section -->
<string name="backup_settings_section_title">Copia de seguridad</string><string
name="backup_settings_status_disabled">Sin copia de seguridad
</string><string name="backup_settings_provider_active">%1$s · %2$s
</string><!-- ejemplo: "Google Drive · Elvish" -->
<string name="backup_settings_last_sync">Última sincronización: %1$s</string><string
name="backup_settings_last_sync_never">Nunca sincronizado
</string><string name="backup_settings_enable_cta">Activar copia de seguridad</string><string
name="backup_settings_sync_now">Sincronizar ahora
</string><string name="backup_settings_syncing">Sincronizando…</string><string
name="backup_settings_sync_success">Sincronizado correctamente
</string><string name="backup_settings_sync_up_to_date">Sin cambios, todo está actualizado
</string><string name="backup_settings_sync_in_progress_worker">Sincronización automática en curso…
</string><string name="backup_settings_restore">Restaurar desde la nube</string><string
name="backup_settings_change_provider">Cambiar proveedor
</string><string name="backup_settings_disconnect">Desconectar cuenta</string><string
name="backup_settings_interval_label">Sincronización automática
</string><string name="backup_settings_interval_daily">Diaria</string><string
name="backup_settings_interval_three_days">Cada 3 días
</string><string name="backup_settings_interval_weekly">Semanal</string><string
name="backup_settings_interval_manual">Solo manual
</string>

    <!-- Diálogo: desconectar -->
<string name="backup_disconnect_title">¿Desconectar copia de seguridad?</string><string
name="backup_disconnect_message">Tu historial local no se borrará, pero dejarás de sincronizar con
%1$s.
</string><!-- %1$s = nombre del proveedor -->
<string name="backup_disconnect_confirm">Desconectar</string><string
name="backup_disconnect_cancel">Cancelar
</string>

    <!-- Diálogo: restaurar -->
<string name="backup_restore_title">Restaurar desde la nube</string><string
name="backup_restore_message">Esto reemplazará tus datos locales actuales con los del backup.
¿Continuar?
</string><string name="backup_restore_confirm">Restaurar</string><string
name="backup_restore_cancel">Cancelar
</string><string name="backup_restore_success">Datos restaurados correctamente</string>

    <!-- Diálogo: conflicto local vs. nube -->
<string name="backup_conflict_title">Tienes datos en dos lugares</string><string
name="backup_conflict_message">Se encontraron datos locales y una copia en la nube. ¿Cuáles quieres
usar?
</string><string name="backup_conflict_use_cloud">Usar datos de la nube</string><string
name="backup_conflict_use_local">Usar datos de este dispositivo
</string><string name="backup_conflict_cancel">Cancelar</string>

    <!-- Errores -->
<string name="backup_error_not_signed_in">Debes iniciar sesión primero</string><string
name="backup_error_network">Sin conexión a internet
</string><string name="backup_error_upload">No se pudo guardar el backup</string><string
name="backup_error_download">No se pudo descargar el backup
</string><string name="backup_error_incompatible">El backup fue creado con una versión más nueva de
la app
</string><string name="backup_error_migration">Error al procesar el backup. Contacta soporte.
</string><string name="backup_error_auth_cancelled">Inicio de sesión cancelado</string><string
name="backup_error_auth_failed">No se pudo iniciar sesión
</string><string name="backup_error_quota">Espacio insuficiente en %1$s
</string><!-- %1$s = nombre del proveedor -->
<string name="backup_error_no_provider">No hay proveedor de nube seleccionado</string>
```

---

## Archivos a crear

| Módulo     | Archivo                                                | Descripción                                                                          |
|------------|--------------------------------------------------------|--------------------------------------------------------------------------------------|
| `:backup`  | `core/backup/CloudProviderType.kt`                     | Enum con `GOOGLE_DRIVE`, `DROPBOX` y su `id` String                                  |
| `:backup`  | `core/backup/CloudUser.kt`                             | Data class: `displayName`, `providerType`                                            |
| `:backup`  | `core/backup/BackupMetadata.kt`                        | Data class del bloque `metadata` del JSON                                            |
| `:backup`  | `core/backup/BackupFile.kt`                            | Wrapper: `metadata + rawData: JsonObject`                                            |
| `:backup`  | `core/backup/BackupError.kt`                           | Sealed interface de errores                                                          |
| `:backup`  | `core/backup/BackupDataSource.kt`                      | Interface puente: abstrae serialización/deserialización desde DB                     |
| `:backup`  | `core/backup/BackupPreferences.kt`                     | Interface puente: abstrae las 7 claves de prefs relacionadas con backup              |
| `:backup`  | `core/backup/CloudBackupProvider.kt`                   | Interface abstracta del proveedor                                                    |
| `:backup`  | `core/backup/BackupProviderRegistry.kt`                | Resolución del proveedor activo por tipo                                             |
| `:backup`  | `core/backup/GoogleDriveBackupProvider.kt`             | Implementación Drive (REST API + `GoogleSignIn`)                                     |
| `:backup`  | `core/backup/DropboxBackupProvider.kt`                 | Implementación Dropbox (SDK v7, OAuth PKCE)                                          |
| `:backup`  | `core/backup/BackupMigration.kt`                       | Data class para una migración individual                                             |
| `:backup`  | `core/backup/BackupMigrationManager.kt`                | Registro y ejecución encadenada de migraciones                                       |
| `:backup`  | `core/backup/BackupRepository.kt`                      | Interface de alto nivel                                                              |
| `:backup`  | `core/backup/BackupRepositoryImpl.kt`                  | Orquesta registry + serializer + migration                                           |
| `:backup`  | `core/backup/ConnectBackupUseCase.kt`                  | Auth + primera sync                                                                  |
| `:backup`  | `core/backup/MigrateBackupUseCase.kt`                  | Copia de archivo entre proveedores sin deserializar                                  |
| `:backup`  | `core/backup/SyncBackupUseCase.kt`                     | Exporta DB local → serializa → sube                                                  |
| `:backup`  | `core/backup/RestoreBackupUseCase.kt`                  | Descarga → deserializa → migra → importa a Room                                      |
| `:backup`  | `core/backup/CheckBackupStatusUseCase.kt`              | Retorna si hay backup remoto y su metadata                                           |
| `:backup`  | `core/backup/DisconnectBackupUseCase.kt`               | Cierra sesión + cancela WorkManager                                                  |
| `:backup`  | `core/backup/CloudBackupWorker.kt`                     | `CoroutineWorker` que llama a `SyncBackupUseCase`                                    |
| `:backup`  | `core/backup/BackupScheduler.kt`                       | Abstracción sobre WorkManager                                                        |
| `:backup`  | `di/BackupCoreModule.kt`                               | Módulo Koin core: Mutex, Json, providers, use cases, repo, worker — sin bridge impls |
| `:compose` | `core/backup/BackupSerializer.kt`                      | Implementa `BackupDataSource`; usa Room + PrefsStorage de `:compose`                 |
| `:compose` | `di/BackupModule.kt`                                   | Koin: `includes(backupCoreModule)` + bind `BackupSerializer → BackupDataSource`      |
| `:compose` | `core/backup/BackupErrorMapper.kt`                     | Mapea `BackupError` + `BackupErrorContext` a `UiText` (necesita `R.string.*`)        |
| `:compose` | `feature/onboarding/ui/steps/StepBackup.kt`            | Paso de onboarding con 4 sub-estados (A/B/C/D)                                       |
| `:compose` | `feature/settings/backup/BackupSettingsSection.kt`     | Sección de Settings                                                                  |
| `:compose` | `feature/settings/backup/ProviderPickerSheet.kt`       | Bottom Sheet selector de proveedor                                                   |
| `:compose` | `feature/settings/backup/ChangeProviderSheet.kt`       | Sheet para cambiar proveedor: migrar o cambiar limpio                                |
| `:compose` | `feature/settings/backup/MigrationProgressDialog.kt`   | Progreso + resultado de la migración entre proveedores                               |
| `:compose` | `feature/settings/backup/RestoreConflictDialog.kt`     | Diálogo de conflicto local vs. nube                                                  |
| `:compose` | `feature/settings/backup/IncompatibleVersionDialog.kt` | Diálogo de versión incompatible con CTA a Play Store                                 |
| `:compose` | `feature/dev/backup/BackupDevState.kt`                 | State del playground                                                                 |
| `:compose` | `feature/dev/backup/BackupDevAction.kt`                | Actions del playground                                                               |
| `:compose` | `feature/dev/backup/BackupDevEvent.kt`                 | Events del playground                                                                |
| `:compose` | `feature/dev/backup/BackupDevViewModel.kt`             | ViewModel del playground                                                             |
| `:compose` | `feature/dev/backup/BackupDevScreen.kt`                | Pantalla del playground                                                              |
| `:compose` | `feature/dev/backup/BackupDevRoot.kt`                  | Root composable del playground                                                       |

> Rutas base: `:backup` → `backup/src/main/java/com/eahm/drinkwaterapp/backup/` |
> `:compose` → `compose/src/main/java/com/eahm/drinkwaterapp/`

---

## Archivos a modificar

| Archivo                                        | Cambio                                                                                                                          |
|------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `compose/build.gradle.kts`                     | Agregar `FEATURE_CLOUD_BACKUP` por build type en `buildTypes {}` + `implementation(project(":backup"))`                         |
| `backup/build.gradle.kts`                      | Crear: `drinkwater.android.library` + deps + `buildConfig = true` + `DROPBOX_APP_KEY` buildConfigField + `manifestPlaceholders` |
| `core/config/FeatureFlags.kt`                  | Agregar `cloudBackupEnabled`                                                                                                    |
| `core/data/datastore/PrefsStorage.kt`          | Agregar keys de backup (enabled, provider, lastSync, interval, displayName)                                                     |
| `core/data/datastore/DataStorePrefsStorage.kt` | Implementar adicionalmente `BackupPreferences` (las 7 claves de backup)                                                         |
| `feature/onboarding/OnboardingState.kt`        | Agregar `OnboardingSteps.CloudBackup`, actualizar `byStep` y `totalSteps`                                                       |
| `feature/onboarding/OnboardingViewModel.kt`    | Agregar sub-estados A/B/C/D del paso backup + `migrateInOnboarding()` + `checkOtherProvider()`                                  |
| `feature/onboarding/OnboardingScreen.kt`       | Agregar rama para `OnboardingSteps.CloudBackup`                                                                                 |
| `feature/settings/SettingsScreen.kt`           | Agregar `BackupSettingsSection` (detrás de feature flag)                                                                        |
| `feature/settings/SettingsViewModel.kt`        | Exponer estado de backup y acciones                                                                                             |
| `feature/dev/DevScreen.kt`                     | Agregar entrada "Cloud Backup" al menú dev (detrás de feature flag)                                                             |
| `di/DataModule.kt`                             | Incluir `backupModule` (el de `:compose`, que a su vez incluye `backupCoreModule`)                                              |
| `di/FeatureModule.kt`                          | Registrar ViewModels de backup settings y dev                                                                                   |
| `settings.gradle.kts`                          | Agregar `include(":backup")`                                                                                                    |
| `backup/src/main/AndroidManifest.xml`          | Crear con INTERNET permission, AuthActivity (Dropbox OAuth, scheme `db-${dropboxAppKey}`), CloudBackupWorker declaration        |
| `gradle/libs.versions.toml`                    | Versiones y referencias: workManager, ktor, playServicesAuth, dropboxSdk                                                        |

---

## Orden de implementación sugerido

### Fase 1 — Infraestructura base (sin UI)

1. Feature flag en build config y `FeatureFlags`.
2. `CloudProviderType`, `BackupError`, `BackupMetadata`, `BackupFile`, `CloudUser`.
3. `CloudBackupProvider` interface + `BackupProviderRegistry`.
4. `BackupSerializer`.
5. `BackupMigrationManager` + `BackupMigration` (base con schema 1, sin migraciones aún).
6. `BackupRepository` interface + `BackupRepositoryImpl`.
7. `GoogleDriveBackupProvider` (Drive REST API + `GoogleSignIn`).
8. `DropboxBackupProvider` (SDK v7, OAuth PKCE).
9. `ConnectBackupUseCase`, `SyncBackupUseCase`, `RestoreBackupUseCase`,
   `CheckBackupStatusUseCase`, `DisconnectBackupUseCase`, `MigrateBackupUseCase`.
10. `CloudBackupWorker` + `BackupScheduler`.
11. Koin `BackupCoreModule` (en `:backup`) + `BackupModule` (en `:compose`).
12. Nuevas keys en `PrefsStorage`.

### Fase 2 — Dev Playground

1. `BackupDevState/Action/Event/ViewModel/Screen/Root`.
2. Entrada en `DevScreen` (detrás de feature flag).
3. Probar ambos proveedores (Google Drive y Dropbox) desde el playground antes de continuar.

### Fase 3 — UI de producción

1. `ProviderPickerSheet` + `ChangeProviderSheet`.
2. `StepBackup` (4 sub-estados) + cambios en `OnboardingState/ViewModel/Screen`.
3. `MigrationProgressDialog`.
4. `BackupSettingsSection` + `RestoreConflictDialog` + `IncompatibleVersionDialog`.
5. Cambios en `SettingsScreen/ViewModel`.
6. Lógica de restauración + detección de migración al inicio de la app (`MainViewModel`).

### Fase 4 — Strings y traducciones

1. Agregar todas las strings a `strings.xml`.
2. Traducir a los 19 idiomas soportados.

### Fase 5 — Pulido y activación gradual

1. Verificar happy path completo con ambos proveedores en dispositivo real.
2. Manejar edge cases: sin conexión, token expirado, cuenta cambiada, quota excedida.
3. Activar el feature flag en `STAGING` para QA interno.
4. Una vez estable, activar en `RELEASE`.

---

## Notas adicionales

### Google Drive

- Usar `Spaces.AppDataFolder` — el archivo es invisible para el usuario en "Mi unidad".
- Scope requerido: `DriveScopes.DRIVE_APPDATA` únicamente.
- Implementar con Drive REST API y el token OAuth de `GoogleSignIn` mediante Ktor (ya en el
  proyecto), evitando el cliente Java pesado.

### Dropbox

- El archivo queda en `/Apps/DrinkWaterApp/drinkwater_backup.json`, visible para el usuario en su
  Dropbox.
- Scope requerido: `files.content.write` + `files.content.read` (App folder scoped).
- SDK v7+ usa OAuth 2.0 PKCE sin WebView embebido. Requiere:
    - Registrar un `app_key` en Dropbox App Console.
    - Agregar un `intent-filter` con scheme `db-<app_key>` en `AndroidManifest.xml`.
    - El `app_key` va en `local.properties` / variable de entorno, no en el repo.
- Agregar `DROPBOX_APP_KEY` como `BuildConfig` field en `AndroidBuildConfigConventionPlugin.kt`.

### General

- El usuario solo puede tener **un proveedor activo** a la vez.
- Cambiar de proveedor se hace desde `ChangeProviderSheet` en Settings: el usuario puede migrar
  el backup (copia de archivo) o cambiar limpio. El archivo en el proveedor anterior no se elimina
  automáticamente en ningún caso.
- El contenido del archivo JSON es idéntico entre proveedores; la migración es una copia de bytes
  sin deserialización.
- **Privacidad**: documentar en la Privacy Policy que el backup contiene datos de uso de la app y se
  almacena en el servicio elegido por el usuario.
- **Tamaño**: para usuarios típicos (<365 días), el JSON pesa < 200 KB. Sin compresión en v1.
- **WorkManager mínimo**: Android no garantiza intervals < 15 minutos. "Diaria" =
  `repeatInterval = 1, TimeUnit.DAYS`. En práctica, Doze Mode puede retrasar la ejecución
  varias horas; esto es aceptable para un backup de datos de salud.
- **Token refresh**: ambos SDKs manejan token refresh automáticamente. Si el token expira y no se
  puede refrescar, lanzar `BackupError.AuthFailed` y mostrar CTA para reconectar.
