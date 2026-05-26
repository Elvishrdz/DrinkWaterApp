# Plan: Cloud Backup Feature (Google Drive — extensible)

## Objetivo

Permitir al usuario sincronizar sus datos de la app (historial de bebidas, metas diarias y
preferencias) con un servicio de nube. La primera implementación usa Google Drive. El diseño
debe ser lo suficientemente abstracto para agregar otros proveedores (Dropbox, OneDrive, etc.)
sin cambiar la capa de negocio.

El feature empieza protegido por un **feature flag de solo DEBUG** y tendrá su propio
**playground en el menú dev** para facilitar las pruebas.

---

## Comportamiento esperado

| Escenario | Detalle |
|-----------|---------|
| Usuario **sin backup** | App funciona exactamente igual que hoy (solo local). |
| Activar backup (primera vez) | Inicia flujo de autenticación con Google → crea archivo en Drive → sincroniza datos locales al cloud. |
| Backup desde onboarding | Nuevo paso opcional al final del onboarding. Si el usuario acepta, completa auth y hace primer sync antes de terminar el onboarding. |
| Sync manual | Botón en Settings → Cloud Backup → "Sincronizar ahora". |
| Sync automático | WorkManager `PeriodicWorkRequest` con intervalo configurable por el usuario (cada día / cada 3 días / cada semana). |
| Restaurar al reinstalar | El usuario inicia la app → acepta usar backup → se muestra diálogo de elección: "Usar datos de la nube" o "Empezar desde cero". |
| Conflicto local vs. nube | Diálogo explícito: "Tienes datos locales y datos en la nube. ¿Cuál quieres usar?" con opción de cancelar. |
| Desactivar backup | En Settings puede desconectar la cuenta. Los datos locales se mantienen; el archivo en Drive no se borra (decisión del usuario). |

---

## Arquitectura general

```
┌─────────────────────────────────────────────────────┐
│                  Presentation layer                  │
│  OnboardingStep(Backup)  ·  BackupSettingsScreen     │
│  RestoreConflictDialog   ·  DevBackupPlayground      │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│               BackupViewModel / UseCases             │
│  SyncBackupUseCase · RestoreBackupUseCase            │
│  CheckBackupStatusUseCase · DisconnectBackupUseCase  │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│                  BackupRepository                    │
│  interface: upload, download, getMetadata, delete    │
└──────────────┬──────────────────────────────────────┘
               │
   ┌───────────▼────────────┐      ┌───────────────────┐
   │ CloudBackupProvider    │      │  BackupSerializer  │
   │ interface              │      │  + MigrationMgr    │
   └──────────┬─────────────┘      └───────────────────┘
              │
   ┌──────────▼─────────────┐
   │ GoogleDriveBackupProv. │  ← primera implementación
   └────────────────────────┘
   (futura: DropboxBackupProvider, etc.)
```

### Abstracción `CloudBackupProvider`

```kotlin
interface CloudBackupProvider {
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

## Formato del archivo de backup

El archivo guardado en Drive se llama `drinkwater_backup.json` en la carpeta de datos de la app
(App Data folder de Google Drive — privada al usuario y a la app, no visible en "Mi unidad").

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
    "userId": "<google-account-email-hash>",
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

> **Nota sobre `userId`**: guardar un hash (SHA-256) del email, nunca el email en texto plano.

---

## Migraciones de backup

`BackupMigrationManager` maneja la evolución del formato entre versiones de la app.

```kotlin
object BackupMigrationManager {
    fun migrate(raw: JsonObject, fromSchema: Int, toSchema: Int): JsonObject
}
```

- Cada migración es un objeto `BackupMigration(from: Int, to: Int, migrate: (JsonObject) -> JsonObject)`.
- Se aplican encadenadas: si el archivo tiene `schemaVersion = 1` y la app está en schema 3,
  se aplican 1→2 y luego 2→3.
- Si `schemaVersion > currentSchema` (backup de una versión futura), se lanza `BackupError.IncompatibleVersion` y se muestra error al usuario.
- Al agregar un campo nuevo a las entidades locales también se debe agregar la migración
  correspondiente que rellene el default en archivos viejos.

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
feature está en desarrollo.

---

## Nuevo paso en onboarding

Se inserta entre el paso actual `Celebrations` (paso 9) y `Summary` (paso 10).

El total de pasos pasa de 10 a **11** (o bien el paso Backup se puede saltar si el feature
flag está desactivado, manteniendo el total en 10 en builds de producción).

### `OnboardingSteps.CloudBackup` (nuevo sealed object)

```kotlin
data object CloudBackup : OnboardingSteps   // solo visible si FeatureFlags.cloudBackupEnabled
```

### Lógica de `byStep` actualizada

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

Cuando el feature flag está desactivado, `byStep(10) → Summary` y `totalSteps = 10` (sin cambio).

### Contenido del paso `StepBackup`

- **Título**: "¿Quieres guardar tu progreso en la nube?"
- **Subtítulo**: Breve explicación de qué se guarda y que es opcional.
- **Botón primario**: "Conectar con Google Drive" → lanza `signIn`.
- **Botón secundario**: "Ahora no" → continúa sin backup.
- Estado de carga mientras ocurre auth.
- Si la auth falla: mensaje de error con opción de reintentar o saltar.

---

## Pantalla de Settings — sección Cloud Backup

Nuevo ítem en el grupo de configuración avanzada (o grupo propio "Datos y copia de seguridad").

**Si backup está desactivado:**
- Card con icono de Drive + texto "Protege tu progreso en la nube".
- Botón "Activar copia de seguridad".

**Si backup está activo:**
- Nombre/correo de la cuenta conectada (hasheado localmente; mostrar solo el nombre de Display).
- Última sincronización: fecha y hora.
- Botón "Sincronizar ahora" (muestra spinner mientras corre).
- Selector de intervalo automático: Diario / Cada 3 días / Semanal / Manual.
- Botón destructivo "Desconectar cuenta".
- Opción "Restaurar desde la nube" (disponible siempre que haya backup remoto).

---

## Sync automático — WorkManager

```kotlin
class CloudBackupWorker(
    context: Context,
    params: WorkerParameters,
    private val syncBackupUseCase: SyncBackupUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result { ... }
}
```

- Se programa con `PeriodicWorkRequest` cuando el usuario activa backup o cambia el intervalo.
- Constraints: `NetworkType.CONNECTED`, `requiresBatteryNotLow = false` (backup es ligero).
- Al completar, guarda `lastSyncAt` en `PrefsStorage`.
- Si falla, WorkManager reintenta con backoff exponencial (máx 3 intentos).
- Si el usuario desactiva backup, se cancela el work con `WorkManager.cancelUniqueWork(BACKUP_WORK_TAG)`.

---

## Almacenamiento de estado del backup (PrefsStorage)

Nuevas claves en DataStore:

```
KEY_BACKUP_ENABLED: Boolean            // ¿El usuario activó backup?
KEY_BACKUP_LAST_SYNC_AT: Long?         // Epoch ms de la última sync exitosa
KEY_BACKUP_AUTO_INTERVAL_DAYS: Int     // 1 | 3 | 7 | 0 (manual)
KEY_BACKUP_PROVIDER: String            // "google_drive" (preparado para multi-provider)
KEY_BACKUP_USER_DISPLAY_NAME: String?  // Nombre del usuario de Drive (para mostrar en UI)
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
CheckBackupStatusUseCase
    ↓ hay datos remotos?
    ├── NO  →  home normal
    └── SÍ
          ↓ hay datos locales?
          ├── NO  →  restaurar silenciosamente y navegar a home
          └── SÍ  →  mostrar RestoreConflictDialog
                        ├── "Usar nube"  →  sobreescribir local con backup, ir a home
                        ├── "Usar local" →  subir datos locales al cloud (merge: cloud = local), ir a home
                        └── "Cancelar"   →  ir a home sin cambios (backup queda "desincronizado" hasta próxima sync)
```

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
    data object QuotaExceeded : BackupError
}
```

Cada error mapea a un `UiText` para mostrar al usuario.

---

## Playground en Dev

Nueva entrada en el menú de desarrollo: **"Cloud Backup"**

```
BackupDevScreen
  ├── Estado actual (enabled, last sync, provider, user)
  ├── [Botón] Iniciar sesión con Google
  ├── [Botón] Cerrar sesión
  ├── [Botón] Sincronizar ahora (manual)
  ├── [Botón] Restaurar desde cloud
  ├── [Botón] Ver metadata del archivo remoto
  ├── [Botón] Eliminar archivo remoto (reset)
  ├── [Botón] Simular conflicto local vs. nube
  ├── [Botón] Corromper backup (simular schema viejo)
  ├── [Botón] Corromper backup (simular schema futuro)
  └── [Log] Output de cada operación en pantalla
```

Seguir el mismo patrón MVI que usan los otros dev screens: `BackupDevState`, `BackupDevAction`, `BackupDevEvent`, `BackupDevViewModel`, `BackupDevScreen`, `BackupDevRoot`.

---

## Dependencias a agregar

```toml
# libs.versions.toml

[versions]
google-drive-api = "v3-rev20250426-2.0.0"  # API REST client
google-auth-library = "1.23.0"

[libraries]
google-api-drive    = { module = "com.google.apis:google-api-services-drive", version.ref = "google-drive-api" }
google-auth-http    = { module = "com.google.auth:google-auth-library-oauth2-http", version.ref = "google-auth-library" }
# Google Sign-In ya está disponible vía play-services-auth existente
```

> Alternativa más simple: usar únicamente **Google Drive REST API** con el token OAuth de
> `GoogleSignIn` (sin el cliente Java pesado). Evaluar tamaño del APK antes de decidir.
> La abstracción `CloudBackupProvider` permite cambiar la implementación sin tocar el resto.

---

## Traducciones necesarias

Todas las cadenas deben agregarse a `compose/src/main/res/values/strings.xml` y traducirse
a los idiomas ya soportados (en, es, fr, de, it, pt, ru, nl, da, ja, ko, zh, hi, ar, af, az, uk, sr).

### Strings requeridos (keys de referencia)

```xml
<!-- Onboarding step -->
<string name="backup_onb_title">Guarda tu progreso</string>
<string name="backup_onb_subtitle">Sincroniza tus datos con Google Drive para no perder tu historial si cambias de dispositivo.</string>
<string name="backup_onb_btn_connect">Conectar con Google Drive</string>
<string name="backup_onb_btn_skip">Ahora no</string>
<string name="backup_onb_connecting">Conectando…</string>
<string name="backup_onb_success">¡Listo! Tus datos están respaldados.</string>
<string name="backup_onb_error">No se pudo conectar. Puedes activarlo más tarde en Ajustes.</string>

<!-- Settings section -->
<string name="backup_settings_section_title">Copia de seguridad</string>
<string name="backup_settings_status_disabled">Sin copia de seguridad</string>
<string name="backup_settings_status_enabled">Conectado como %1$s</string>
<string name="backup_settings_last_sync">Última sincronización: %1$s</string>
<string name="backup_settings_last_sync_never">Nunca sincronizado</string>
<string name="backup_settings_enable_cta">Activar copia de seguridad</string>
<string name="backup_settings_sync_now">Sincronizar ahora</string>
<string name="backup_settings_syncing">Sincronizando…</string>
<string name="backup_settings_sync_success">Sincronizado correctamente</string>
<string name="backup_settings_disconnect">Desconectar cuenta</string>
<string name="backup_settings_restore">Restaurar desde la nube</string>
<string name="backup_settings_interval_label">Sincronización automática</string>
<string name="backup_settings_interval_daily">Diaria</string>
<string name="backup_settings_interval_three_days">Cada 3 días</string>
<string name="backup_settings_interval_weekly">Semanal</string>
<string name="backup_settings_interval_manual">Solo manual</string>

<!-- Restore conflict dialog -->
<string name="backup_conflict_title">Tienes datos en dos lugares</string>
<string name="backup_conflict_message">Se encontraron datos locales y una copia en la nube. ¿Cuáles quieres usar?</string>
<string name="backup_conflict_use_cloud">Usar datos de la nube</string>
<string name="backup_conflict_use_local">Usar datos de este dispositivo</string>
<string name="backup_conflict_cancel">Cancelar</string>

<!-- Disconnect confirmation dialog -->
<string name="backup_disconnect_title">¿Desconectar copia de seguridad?</string>
<string name="backup_disconnect_message">Tu historial local no se borrará, pero dejarás de sincronizar con Google Drive.</string>
<string name="backup_disconnect_confirm">Desconectar</string>
<string name="backup_disconnect_cancel">Cancelar</string>

<!-- Restore confirmation dialog -->
<string name="backup_restore_title">Restaurar desde la nube</string>
<string name="backup_restore_message">Esto reemplazará tus datos locales actuales con los del backup. ¿Continuar?</string>
<string name="backup_restore_confirm">Restaurar</string>
<string name="backup_restore_cancel">Cancelar</string>
<string name="backup_restore_success">Datos restaurados correctamente</string>

<!-- Errors -->
<string name="backup_error_not_signed_in">Debes iniciar sesión primero</string>
<string name="backup_error_network">Sin conexión a internet</string>
<string name="backup_error_upload">No se pudo guardar el backup</string>
<string name="backup_error_download">No se pudo descargar el backup</string>
<string name="backup_error_incompatible">El backup fue creado con una versión más nueva de la app</string>
<string name="backup_error_migration">Error al procesar el backup. Contacta soporte.</string>
<string name="backup_error_auth_cancelled">Inicio de sesión cancelado</string>
<string name="backup_error_auth_failed">No se pudo iniciar sesión</string>
<string name="backup_error_quota">Espacio insuficiente en Google Drive</string>
```

---

## Archivos a crear

| Archivo | Descripción |
|---------|-------------|
| `core/backup/BackupMetadata.kt` | Data class con los campos del bloque `metadata` del JSON |
| `core/backup/BackupFile.kt` | Wrapper: `metadata + rawData: JsonObject` |
| `core/backup/BackupSerializer.kt` | Serialización/deserialización completa del JSON |
| `core/backup/BackupMigrationManager.kt` | Registro y ejecución de migraciones |
| `core/backup/BackupMigration.kt` | Interface/data class para una migración individual |
| `core/backup/BackupError.kt` | Sealed interface de errores de backup |
| `core/backup/CloudBackupProvider.kt` | Interface del proveedor abstracto |
| `core/backup/CloudUser.kt` | Data class: id, displayName |
| `core/backup/GoogleDriveBackupProvider.kt` | Implementación con Google Drive App Data folder |
| `core/backup/BackupRepository.kt` | Interface de alto nivel: upload, download, getMetadata |
| `core/backup/BackupRepositoryImpl.kt` | Implementación: orquesta provider + serializer + migration |
| `core/backup/SyncBackupUseCase.kt` | Exporta DB local → serializa → sube |
| `core/backup/RestoreBackupUseCase.kt` | Descarga → deserializa → migra → importa a Room |
| `core/backup/CheckBackupStatusUseCase.kt` | Retorna si hay backup remoto y sus metadatos |
| `core/backup/DisconnectBackupUseCase.kt` | Cierra sesión + cancela WorkManager |
| `core/backup/CloudBackupWorker.kt` | CoroutineWorker que llama a SyncBackupUseCase |
| `core/backup/BackupScheduler.kt` | Abstracción sobre WorkManager para programar/cancelar syncs |
| `feature/onboarding/ui/steps/StepBackup.kt` | Paso de onboarding para activar backup |
| `feature/settings/backup/BackupSettingsSection.kt` | Sección de Settings para gestionar backup |
| `feature/settings/backup/RestoreConflictDialog.kt` | Diálogo de conflicto local vs. nube |
| `feature/dev/backup/BackupDevViewModel.kt` | ViewModel del playground de dev |
| `feature/dev/backup/BackupDevState.kt` | State del playground |
| `feature/dev/backup/BackupDevAction.kt` | Actions del playground |
| `feature/dev/backup/BackupDevEvent.kt` | Events del playground |
| `feature/dev/backup/BackupDevScreen.kt` | Pantalla del playground |
| `feature/dev/backup/BackupDevRoot.kt` | Root composable del playground |
| `di/BackupModule.kt` | Módulo Koin con todas las dependencias de backup |

---

## Archivos a modificar

| Archivo | Cambio |
|---------|--------|
| `AndroidBuildConfigConventionPlugin.kt` | Agregar `FEATURE_CLOUD_BACKUP` por variante |
| `core/config/FeatureFlags.kt` | Agregar `cloudBackupEnabled` |
| `core/data/datastore/PrefsStorage.kt` | Agregar keys de backup (enabled, lastSync, interval, provider, displayName) |
| `feature/onboarding/OnboardingState.kt` | Agregar `OnboardingSteps.CloudBackup`, actualizar `byStep`, `totalSteps` |
| `feature/onboarding/OnboardingViewModel.kt` | Manejar `skipBackup()` y `connectBackup()` en el nuevo paso |
| `feature/onboarding/OnboardingScreen.kt` | Agregar rama para `OnboardingSteps.CloudBackup` |
| `feature/settings/SettingsScreen.kt` | Agregar sección Cloud Backup (detrás de feature flag) |
| `feature/settings/SettingsViewModel.kt` | Exponer estado de backup y acciones |
| `feature/dev/DevScreen.kt` | Agregar entrada "Cloud Backup" al menú dev (detrás de feature flag) |
| `di/DataModule.kt` | Incluir `backupModule` de Koin |
| `di/FeatureModule.kt` | Registrar ViewModels de backup settings y dev |
| `compose/build.gradle.kts` | Agregar dependencias de Google Drive / Auth |
| `gradle/libs.versions.toml` | Nuevas versiones y referencias de librería |
| `AndroidManifest.xml` | Permisos: `INTERNET` (ya existe), `GET_ACCOUNTS`; registrar `CloudBackupWorker` |

---

## Orden de implementación sugerido

### Fase 1 — Infraestructura base (sin UI)
1. Feature flag `FEATURE_CLOUD_BACKUP` en build config y `FeatureFlags`.
2. `BackupError`, `BackupMetadata`, `BackupFile`, `CloudUser`.
3. `CloudBackupProvider` interface.
4. `BackupSerializer` (JSON ↔ entidades Room).
5. `BackupMigrationManager` con migración vacía (schema 1 → 1, como base).
6. `BackupRepository` interface + `BackupRepositoryImpl`.
7. `GoogleDriveBackupProvider` (App Data folder, auth con `GoogleSignIn`).
8. `SyncBackupUseCase`, `RestoreBackupUseCase`, `CheckBackupStatusUseCase`, `DisconnectBackupUseCase`.
9. `CloudBackupWorker` + `BackupScheduler`.
10. Koin `BackupModule`.
11. Nuevas keys en `PrefsStorage`.

### Fase 2 — Dev Playground
12. `BackupDevState/Action/Event/ViewModel/Screen/Root`.
13. Entrada en `DevScreen` (detrás de feature flag).
14. Verificar todos los flujos desde el playground antes de continuar.

### Fase 3 — UI de producción
15. `StepBackup` en onboarding + cambios en `OnboardingState/ViewModel/Screen`.
16. `BackupSettingsSection` + `RestoreConflictDialog`.
17. Cambios en `SettingsScreen/ViewModel`.
18. Lógica de restauración al inicio de la app (en `MainViewModel` o en `SplashScreen`).

### Fase 4 — Strings y traducciones
19. Agregar todas las strings a `strings.xml`.
20. Traducir a todos los idiomas soportados.

### Fase 5 — Pulido y activación gradual
21. Revisar el happy path completo desde el playground y en dispositivo real.
22. Manejar edge cases: sin conexión, cuenta cambiada, quota de Drive.
23. Activar el feature flag en `STAGING` para QA interno.
24. Una vez estable, activar en `RELEASE`.

---

## Notas adicionales

- **App Data folder vs. "Mi unidad"**: usar `Spaces.AppDataFolder` de la Drive API. El archivo
  solo es visible para la app, no para el usuario desde la interfaz de Drive. Esto simplifica
  permisos (scope `drive.appdata`) y evita que el usuario accidentalmente lo borre.
- **Sin cuenta de Google**: mostrar mensaje explicativo y ofrecer activarlo más tarde. No
  bloquear el flujo de onboarding.
- **Frecuencia mínima de WorkManager**: Android no garantiza intervals menores a 15 minutos.
  "Diaria" se programa como `PeriodicWorkRequest` con `repeatInterval = 1, TimeUnit.DAYS`.
- **Scope de autenticación requerido**: `DriveScopes.DRIVE_APPDATA` únicamente. No solicitar
  acceso completo a Drive.
- **Privacidad**: el archivo de backup contiene datos de salud del usuario. Documentar en la
  Privacy Policy que se almacena de forma privada en el App Data folder del usuario.
- **Tamaño del backup**: para usuarios típicos (<365 días de datos), el JSON debería pesar
  < 200 KB. No se necesita compresión en esta primera versión.
- **Sin Firebase**: la sync usa la cuenta de Google del dispositivo directamente vía Google
  Sign-In, no requiere Firebase Auth.
