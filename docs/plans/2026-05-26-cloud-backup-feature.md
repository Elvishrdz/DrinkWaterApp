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

| Escenario                    | Detalle                                                                                                                                                                         |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Usuario **sin backup**       | App funciona exactamente igual que hoy (solo local).                                                                                                                            |
| Activar backup (primera vez) | Muestra selector de proveedor (Google Drive / Dropbox) → inicia auth del proveedor elegido → crea archivo → sincroniza datos locales al cloud.                                  |
| Backup desde onboarding      | Nuevo paso opcional al final del onboarding. Muestra selector de proveedor. Si el usuario acepta, completa auth y hace primer sync antes de terminar el onboarding.             |
| Sync manual                  | Botón en Settings → Cloud Backup → "Sincronizar ahora".                                                                                                                         |
| Sync automático              | WorkManager `PeriodicWorkRequest` con intervalo configurable (cada día / cada 3 días / cada semana).                                                                            |
| Restaurar al reinstalar      | App detecta backup remoto → diálogo: "Usar datos de la nube" o "Empezar desde cero".                                                                                            |
| Conflicto local vs. nube     | Diálogo explícito: "Tienes datos locales y datos en la nube. ¿Cuáles quieres usar?" con opción de cancelar.                                                                     |
| Cambiar de proveedor         | En Settings: desconectar proveedor actual → conectar proveedor nuevo. No hay migración automática entre proveedores en v1 (el usuario puede hacer sync manual tras reconectar). |
| Desactivar backup            | En Settings puede desconectar la cuenta. Los datos locales se mantienen; el archivo remoto no se borra.                                                                         |

---

## Arquitectura general

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation layer                    │
│  OnboardingStep(Backup)  ·  BackupSettingsSection        │
│  ProviderPickerSheet     ·  RestoreConflictDialog        │
│  DevBackupPlayground                                     │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                 BackupViewModel / UseCases               │
│  SyncBackupUseCase    · RestoreBackupUseCase             │
│  CheckBackupStatusUseCase · DisconnectBackupUseCase      │
│  ConnectBackupUseCase                                    │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                   BackupRepository                       │
│  interface: connect, upload, download, getMetadata,      │
│             disconnect, deleteRemoteFile                 │
└──────────────────┬────────────────────────────────────--─┘
                   │  resuelve proveedor activo vía
                   │  BackupProviderRegistry
        ┌──────────▼────────────────┐    ┌─────────────────────┐
        │  CloudBackupProvider      │    │  BackupSerializer   │
        │  interface                │    │  + MigrationManager │
        └──────┬────────────────────┘    └─────────────────────┘
               │
   ┌───────────┴──────────────┐
   │                          │
   ▼                          ▼
GoogleDriveBackupProvider   DropboxBackupProvider
(App Data folder)           (App Folder)
```

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
lo resuelve a través del registry en cada operación.

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
    val currentSchema: Int = 1   // incrementar con cada cambio estructural

    fun migrate(raw: JsonObject, fromSchema: Int, toSchema: Int): JsonObject
}
```

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

| Contexto                                | Disparador                                         | Comportamiento posterior                                                                      |
|-----------------------------------------|----------------------------------------------------|-----------------------------------------------------------------------------------------------|
| Restore manual (Settings)               | Usuario toca "Restaurar desde la nube"             | Muestra diálogo; si cancela, vuelve a Settings sin cambios                                    |
| Restauración al reinstalar (silenciosa) | `CheckBackupStatusUseCase` detecta schema mayor    | Muestra diálogo antes de llegar al home; si cancela, entra al home con datos locales intactos |
| Conflicto local vs. nube → "Usar nube"  | Usuario elige el backup en `RestoreConflictDialog` | Reemplaza el `RestoreConflictDialog` con `IncompatibleVersionDialog`                          |

En ningún caso se muestra primero el conflicto y luego falla silenciosamente: la verificación
de schema ocurre antes de mostrar cualquier opción de restauración al usuario.

### Strings para este caso

```xml

<string name="backup_incompatible_title">Actualiza la app para restaurar</string><string
name="backup_incompatible_body">Tu copia de seguridad fue creada con una versión más nueva de la
app.\n\nActualiza la app y luego vuelve a restaurar. Tus datos actuales no se modificaron.
</string><string name="backup_incompatible_update">Actualizar app</string><string
name="backup_incompatible_cancel">Cancelar
</string>
```

### Archivo a crear

| Archivo                                                | Descripción                                          |
|--------------------------------------------------------|------------------------------------------------------|
| `feature/settings/backup/IncompatibleVersionDialog.kt` | Diálogo de versión incompatible con CTA a Play Store |

---

## Feature flag

Agregar en `AndroidBuildConfigConventionPlugin.kt`:

```kotlin
// DEBUG
buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "true")

// STAGING, QA, RELEASE
buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "false")
```

Y en `FeatureFlags.kt`:

```kotlin
val cloudBackupEnabled: Boolean = BuildConfig.FEATURE_CLOUD_BACKUP
```

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

### Contenido del paso `StepBackup`

- **Título**: "¿Quieres guardar tu progreso en la nube?"
- **Subtítulo**: Breve explicación de qué se guarda y que es opcional.
- **Área central**: dos tarjetas con Google Drive y Dropbox (mismo diseño que `ChoiceCard` del
  onboarding).
- **Botón primario**: "Conectar" → abre `ProviderPickerSheet` si ningún proveedor está
  pre-seleccionado,
  o lanza directamente el auth si el usuario ya eligió uno en la tarjeta.
- **Botón secundario / link**: "Ahora no" → continúa sin backup.
- Estado de carga mientras ocurre auth.
- Si la auth falla: mensaje de error con opción de reintentar o saltar.

---

## Pantalla de Settings — sección Cloud Backup

**Si backup está desactivado:**

- Card con icono genérico de nube + "Protege tu progreso en la nube".
- Botón "Activar copia de seguridad" → abre `ProviderPickerSheet`.

**Si backup está activo:**

- Icono + nombre del proveedor activo (Google Drive o Dropbox).
- Nombre de la cuenta conectada (Display Name).
- Última sincronización: fecha y hora / "Nunca sincronizado".
- Botón "Sincronizar ahora" (spinner durante la operación).
- Selector de intervalo automático: Diario / Cada 3 días / Semanal / Solo manual.
- Opción "Restaurar desde la nube" (con diálogo de confirmación).
- Botón "Cambiar proveedor" → desconecta el actual y abre `ProviderPickerSheet`.
- Botón destructivo "Desconectar cuenta" (con diálogo de confirmación).

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

- `PeriodicWorkRequest` con `BACKUP_WORK_TAG` único.
- Constraints: `NetworkType.CONNECTED`.
- Al completar, guarda `lastSyncAt` en `PrefsStorage`.
- Backoff exponencial en fallo (máx 3 intentos antes de reportar error en UI).
- Al desactivar backup o cambiar de proveedor: `WorkManager.cancelUniqueWork(BACKUP_WORK_TAG)`.

---

## Almacenamiento de estado en `PrefsStorage`

```
KEY_BACKUP_ENABLED: Boolean              // ¿El usuario activó backup?
KEY_BACKUP_PROVIDER: String              // "google_drive" | "dropbox"
KEY_BACKUP_LAST_SYNC_AT: Long?           // Epoch ms de la última sync exitosa
KEY_BACKUP_AUTO_INTERVAL_DAYS: Int       // 1 | 3 | 7 | 0 (solo manual)
KEY_BACKUP_USER_DISPLAY_NAME: String?    // Nombre para mostrar en Settings
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
    data class QuotaExceeded(val provider: CloudProviderType) : BackupError
    data object NoProviderSelected : BackupError
    data object ProviderNotAvailable : BackupError
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

Este error merece un tratamiento especial porque el usuario necesita entender qué pasó, por
qué el backup ocupa tan poco y qué debe hacer.

#### Contexto 1 — Sync manual (Settings)

Mostrar `QuotaExceededDialog`:

```
╔════════════════════════════════════════════╗
║  Sin espacio en Google Drive               ║
╠════════════════════════════════════════════╣
║  Tu copia de seguridad ocupa menos de      ║
║  200 KB, pero tu Google Drive está lleno.  ║
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

Nueva clave añadida al listado de la sección de PrefsStorage:

```
KEY_BACKUP_LAST_ERROR: String?   // null | "quota_exceeded" | "auth_failed" | "network" | "upload_failed"
```

Esta clave solo se escribe durante syncs automáticas (background). Las syncs manuales y de
onboarding muestran el error directamente en UI sin pasar por prefs. Se limpia cada vez que
una sync completa sin error.

---

### Strings de errores (sección ampliada)

Añadir a los strings existentes del plan:

```xml
<!-- Quota exceeded — diálogo -->
<string name="backup_error_quota_title">Sin espacio en %1$s</string><string
name="backup_error_quota_body">Tu copia de seguridad ocupa menos de 200 KB, pero tu %1$s está
lleno.\n\nLibera espacio en %1$s para que tu progreso pueda guardarse. Tus datos locales están
seguros.
</string><string name="backup_error_quota_manage_space">Gestionar espacio</string><string
name="backup_error_quota_understood">Entendido
</string>

    <!-- Quota exceeded — banner en Settings (sync background fallida) -->
<string name="backup_error_quota_banner">Sin espacio en %1$s. Toca para saber más.</string>

    <!-- Quota exceeded — inline en onboarding -->
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

    <!-- Versión incompatible — diálogo -->
<string name="backup_error_incompatible_title">Backup de versión más nueva</string><string
name="backup_error_incompatible_body">Este backup fue creado con una versión más reciente de la app
(versión interna %1$d). Actualiza la app para poder restaurarlo.
</string><string name="backup_error_incompatible_update">Actualizar app</string><string
name="backup_error_incompatible_cancel">Cancelar
</string>

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

enum class BackupErrorContext { MANUAL_SYNC, AUTO_SYNC, ONBOARDING, RESTORE }
```

El mapper recibe el `CloudProviderType` del error (cuando aplica) y construye el string
con el nombre del proveedor ya resuelto, listo para mostrar en UI.

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
  ├── ── Simulaciones ──
  ├── [Botón] Simular conflicto local vs. nube
  ├── [Botón] Corromper backup (schema viejo → migración hacia adelante)
  ├── [Botón] Corromper backup (schema futuro → IncompatibleVersion)
  ├── [Botón] Simular fallo de red
  ├── [Botón] Simular QuotaExceeded
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

| Archivo                                                | Descripción                                          |
|--------------------------------------------------------|------------------------------------------------------|
| `core/backup/CloudProviderType.kt`                     | Enum con `GOOGLE_DRIVE`, `DROPBOX` y su `id` String  |
| `core/backup/CloudUser.kt`                             | Data class: `displayName`, `providerType`            |
| `core/backup/BackupMetadata.kt`                        | Data class del bloque `metadata` del JSON            |
| `core/backup/BackupFile.kt`                            | Wrapper: `metadata + rawData: JsonObject`            |
| `core/backup/BackupError.kt`                           | Sealed interface de errores                          |
| `core/backup/CloudBackupProvider.kt`                   | Interface abstracta del proveedor                    |
| `core/backup/BackupProviderRegistry.kt`                | Resolución del proveedor activo por tipo             |
| `core/backup/GoogleDriveBackupProvider.kt`             | Implementación Drive (REST API + `GoogleSignIn`)     |
| `core/backup/DropboxBackupProvider.kt`                 | Implementación Dropbox (SDK v7, OAuth PKCE)          |
| `core/backup/BackupSerializer.kt`                      | Serialización/deserialización JSON ↔ entidades Room  |
| `core/backup/BackupMigration.kt`                       | Data class para una migración individual             |
| `core/backup/BackupMigrationManager.kt`                | Registro y ejecución encadenada de migraciones       |
| `core/backup/BackupRepository.kt`                      | Interface de alto nivel                              |
| `core/backup/BackupRepositoryImpl.kt`                  | Orquesta registry + serializer + migration           |
| `core/backup/ConnectBackupUseCase.kt`                  | Auth + primera sync                                  |
| `core/backup/SyncBackupUseCase.kt`                     | Exporta DB local → serializa → sube                  |
| `core/backup/RestoreBackupUseCase.kt`                  | Descarga → deserializa → migra → importa a Room      |
| `core/backup/CheckBackupStatusUseCase.kt`              | Retorna si hay backup remoto y su metadata           |
| `core/backup/DisconnectBackupUseCase.kt`               | Cierra sesión + cancela WorkManager                  |
| `core/backup/CloudBackupWorker.kt`                     | `CoroutineWorker` que llama a `SyncBackupUseCase`    |
| `core/backup/BackupScheduler.kt`                       | Abstracción sobre WorkManager                        |
| `feature/onboarding/ui/steps/StepBackup.kt`            | Paso de onboarding para activar backup               |
| `feature/settings/backup/BackupSettingsSection.kt`     | Sección de Settings                                  |
| `feature/settings/backup/ProviderPickerSheet.kt`       | Bottom Sheet selector de proveedor                   |
| `feature/settings/backup/RestoreConflictDialog.kt`     | Diálogo de conflicto local vs. nube                  |
| `feature/settings/backup/IncompatibleVersionDialog.kt` | Diálogo de versión incompatible con CTA a Play Store |
| `feature/dev/backup/BackupDevState.kt`                 | State del playground                                 |
| `feature/dev/backup/BackupDevAction.kt`                | Actions del playground                               |
| `feature/dev/backup/BackupDevEvent.kt`                 | Events del playground                                |
| `feature/dev/backup/BackupDevViewModel.kt`             | ViewModel del playground                             |
| `feature/dev/backup/BackupDevScreen.kt`                | Pantalla del playground                              |
| `feature/dev/backup/BackupDevRoot.kt`                  | Root composable del playground                       |
| `di/BackupModule.kt`                                   | Módulo Koin con todas las dependencias de backup     |

---

## Archivos a modificar

| Archivo                                     | Cambio                                                                                               |
|---------------------------------------------|------------------------------------------------------------------------------------------------------|
| `AndroidBuildConfigConventionPlugin.kt`     | Agregar `FEATURE_CLOUD_BACKUP` por variante                                                          |
| `core/config/FeatureFlags.kt`               | Agregar `cloudBackupEnabled`                                                                         |
| `core/data/datastore/PrefsStorage.kt`       | Agregar keys de backup (enabled, provider, lastSync, interval, displayName)                          |
| `feature/onboarding/OnboardingState.kt`     | Agregar `OnboardingSteps.CloudBackup`, actualizar `byStep` y `totalSteps`                            |
| `feature/onboarding/OnboardingViewModel.kt` | Agregar `skipBackup()` y `connectBackup(provider)`                                                   |
| `feature/onboarding/OnboardingScreen.kt`    | Agregar rama para `OnboardingSteps.CloudBackup`                                                      |
| `feature/settings/SettingsScreen.kt`        | Agregar `BackupSettingsSection` (detrás de feature flag)                                             |
| `feature/settings/SettingsViewModel.kt`     | Exponer estado de backup y acciones                                                                  |
| `feature/dev/DevScreen.kt`                  | Agregar entrada "Cloud Backup" al menú dev (detrás de feature flag)                                  |
| `di/DataModule.kt`                          | Incluir `backupModule`                                                                               |
| `di/FeatureModule.kt`                       | Registrar ViewModels de backup settings y dev                                                        |
| `compose/build.gradle.kts`                  | Agregar dependencias Dropbox SDK                                                                     |
| `gradle/libs.versions.toml`                 | Versiones y referencias Dropbox                                                                      |
| `AndroidManifest.xml`                       | Registrar `CloudBackupWorker`; agregar `intent-filter` con scheme custom para Dropbox OAuth callback |

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
   `CheckBackupStatusUseCase`, `DisconnectBackupUseCase`.
10. `CloudBackupWorker` + `BackupScheduler`.
11. Koin `BackupModule`.
12. Nuevas keys en `PrefsStorage`.

### Fase 2 — Dev Playground

1. `BackupDevState/Action/Event/ViewModel/Screen/Root`.
2. Entrada en `DevScreen` (detrás de feature flag).
3. Probar ambos proveedores (Google Drive y Dropbox) desde el playground antes de continuar.

### Fase 3 — UI de producción

1. `ProviderPickerSheet`.
2. `StepBackup` en onboarding + cambios en `OnboardingState/ViewModel/Screen`.
3. `BackupSettingsSection` + `RestoreConflictDialog`.
4. Cambios en `SettingsScreen/ViewModel`.
5. Lógica de restauración al inicio de la app (detección en `MainViewModel`).

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
- Cambiar de proveedor en v1 es manual: desconectar → conectar nuevo. No hay migración automática
  entre proveedores (el archivo anterior en el proveedor viejo no se borra automáticamente).
- El contenido del archivo JSON es idéntico entre proveedores. En el futuro se podría migrar de uno
  a otro descargando y subiendo.
- **Privacidad**: documentar en la Privacy Policy que el backup contiene datos de uso de la app y se
  almacena en el servicio elegido por el usuario.
- **Tamaño**: para usuarios típicos (<365 días), el JSON pesa < 200 KB. Sin compresión en v1.
- **WorkManager mínimo**: Android no garantiza intervals < 15 minutos. "Diaria" =
  `repeatInterval = 1, TimeUnit.DAYS`.
- **Token refresh**: ambos SDKs manejan token refresh automáticamente. Si el token expira y no se
  puede refrescar, lanzar `BackupError.AuthFailed` y mostrar CTA para reconectar.
