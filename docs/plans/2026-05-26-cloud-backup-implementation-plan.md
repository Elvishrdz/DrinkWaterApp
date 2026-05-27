# Implementation Plan: Cloud Backup — Todo el plan compilable

Feature spec: `docs/plans/2026-05-26-cloud-backup-feature.md`
Scope: all 4 phases — infrastructure + dev playground + production UI + strings.
Everything must compile after each phase.

---

## Key discoveries vs. plan assumptions

| Assumption in plan                | Reality                                                   | Resolution                                                                |
|-----------------------------------|-----------------------------------------------------------|---------------------------------------------------------------------------|
| `Result<T,E>` + `DataError` exist | Not in project                                            | Create `BackupResult<T>` (Success/Failure) in backup package              |
| Ktor already present              | Not present                                               | Add `ktor-client-android` + content-negotiation                           |
| `work-runtime-ktx` present        | Missing (Koin integration exists)                         | Add `work-runtime-ktx`                                                    |
| Google Sign-In in compose         | Only in legacy `:app`                                     | Add `play-services-auth` to compose                                       |
| Use case pattern exists           | Not used (Repo + ViewModel)                               | Use cases = plain Kotlin classes; no issue                                |
| strings.xml location              | `compose/src/main/res/values/strings.xml`                 | Confirmed                                                                 |
| All backup logic in `:compose`    | Project uses flat module structure (`:ads`, `:analytics`) | Create new `:backup` library module; `:compose` depends on it for UI only |

---

## Critical files to read before editing

- `gradle/libs.versions.toml`
- `compose/build.gradle.kts`
- `settings.gradle.kts`
- `backup/build.gradle.kts` (new file — create during Fase 1)
- `build-logic/src/main/kotlin/AndroidBuildConfigConventionPlugin.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/core/config/FeatureFlags.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/core/data/datastore/PrefsStorage.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/core/data/datastore/DataStorePrefsStorage.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/di/DataModule.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/di/FeatureModule.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/onboarding/OnboardingState.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/onboarding/OnboardingScreen.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/onboarding/OnboardingViewModel.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/settings/SettingsScreen.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/settings/SettingsViewModel.kt`
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/dev/DevRoot.kt`
- `compose/src/main/AndroidManifest.xml`
- `compose/src/main/res/values/strings.xml`

Core/domain files: `backup/src/main/java/com/eahm/drinkwaterapp/backup/`
UI + integration files: `compose/src/main/java/com/eahm/drinkwaterapp/`

---

## FASE 1 — Infraestructura base (no UI)

### 0. Create `:backup` module

**`backup/build.gradle.kts`** (new file):

```kotlin
plugins {
    id("drinkwater.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}
android { namespace = "com.eahm.drinkwaterapp.backup" }
dependencies {
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.play.services.auth)
    implementation(libs.dropbox.android)
    implementation(libs.dropbox.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)
    implementation(libs.koin.androidx.workmanager)
}
```

**`backup/src/main/AndroidManifest.xml`** (new file — namespace only, no activities/permissions
yet — those come in step 1):

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

**`settings.gradle.kts`**: add `include(":backup")`.

---

### 1. Dependencies

**`gradle/libs.versions.toml`** — add:

```toml
[versions]
workManager = "2.10.1"
ktor = "3.1.3"
playServicesAuth = "21.3.0"
dropboxSdk = "7.0.0"

[libraries]
androidx-work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version.ref = "workManager" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
play-services-auth = { module = "com.google.android.gms:play-services-auth", version.ref = "playServicesAuth" }
dropbox-android = { module = "com.dropbox.core:dropbox-android-sdk", version.ref = "dropboxSdk" }
dropbox-core = { module = "com.dropbox.core:dropbox-core-sdk", version.ref = "dropboxSdk" }
```

**`backup/build.gradle.kts`** (new — see step 0 above for full content). All non-UI deps (Ktor,
WorkManager, play-services-auth, Dropbox) live here; they are NOT added to `:compose`.

**`compose/build.gradle.kts`** inside `dependencies` — only ADD:

```kotlin
implementation(project(":backup"))
```

(Ktor, WorkManager, play-services-auth, Dropbox deps are NOT added to `:compose` — they live in
`:backup` and are exposed transitively.)

**`backup/src/main/AndroidManifest.xml`** — update with INTERNET permission, Dropbox AuthActivity
intent-filter, and CloudBackupWorker declaration (merged into app manifest automatically):

```xml

<uses-permission android:name="android.permission.INTERNET" />

    <!-- Dropbox OAuth redirect -->
<activity android:name="com.dropbox.core.android.AuthActivity" android:exported="true"
android:launchMode="singleTask">
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="db-${BuildConfig.DROPBOX_APP_KEY}" />
</intent-filter>
</activity>

    <!-- CloudBackupWorker -->
<provider android:name="androidx.startup.InitializationProvider"
android:authorities="${applicationId}.androidx-startup" android:exported="false" tools:node="merge">
<meta-data android:name="com.eahm.drinkwaterapp.backup.CloudBackupWorker"
    android:value="androidx.startup" />
</provider>
```

---

### 2. Build config + FeatureFlags

**`AndroidBuildConfigConventionPlugin.kt`**: read `dropbox.app.key` from local.properties; add to
all variants:

```kotlin
buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "true")   // DEBUG
buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "false")  // STAGING / QA / RELEASE
buildConfigField("String", "DROPBOX_APP_KEY", "\"$dropboxAppKey\"") // all variants
```

**`FeatureFlags.kt`**: add `val cloudBackupEnabled: Boolean = BuildConfig.FEATURE_CLOUD_BACKUP`

User update: set this build config fields directly in the build.gradle module.

---

### 3. PrefsStorage backup keys

**`PrefsStorage.kt`**: add 7 method pairs:
`getBackupEnabled/setBackupEnabled`, `getBackupProvider/setBackupProvider`,
`getBackupLastSyncAt/setBackupLastSyncAt`, `getBackupAutoIntervalDays/setBackupAutoIntervalDays`,
`getBackupUserDisplayName/setBackupUserDisplayName`, `getBackupContentHash/setBackupContentHash`,
`getBackupLastError/setBackupLastError(code: String?)`

**`DataStorePrefsStorage.kt`**: implement all above with DataStore keys, and additionally implement
the `BackupPreferences` interface (defined in `:backup`).
Add `backupEnabled` (boolean), `backupProvider` (string), `backupLastSyncAt` (long),
`backupAutoIntervalDays` (int, default 1), `backupUserDisplayName` (string), `backupContentHash` (
string),
`backupLastError` (string). Clear backup keys in `clear()`.

---

### 4. Core domain models — new package in `:backup`

Path: `backup/src/main/java/com/eahm/drinkwaterapp/backup/`

| File                    | Content                                                                                             |
|-------------------------|-----------------------------------------------------------------------------------------------------|
| `CloudProviderType.kt`  | enum GOOGLE_DRIVE / DROPBOX with id, displayName, companion fromId()                                |
| `BackupResult.kt`       | `sealed class BackupResult<out T>` — Success(value) / Failure(error: BackupError) + getOrElse + map |
| `BackupError.kt`        | sealed interface with 13 variants (see plan)                                                        |
| `CloudUser.kt`          | data class (displayName, providerType)                                                              |
| `BackupMetadata.kt`     | @Serializable data class matching JSON spec                                                         |
| `BackupFile.kt`         | data class (metadata: BackupMetadata, rawData: JsonObject)                                          |
| `BackupSyncState.kt`    | sealed interface: Idle/InProgress/UpToDate/Success(syncedAt)/Error(error)                           |
| `SyncResult.kt`         | sealed interface: Uploaded/SkippedNoChanges/AlreadyInProgress                                       |
| `RemoteBackupStatus.kt` | data class (metadata: BackupMetadata, isCompatible: Boolean)                                        |
| `BackupDataSource.kt`   | interface: `serialize(): String` + `deserializeAndImport(data: JsonObject): BackupResult<Unit>`     |
| `BackupPreferences.kt`  | interface: 7 key pairs + `clearBackupKeys()` (implemented in `:compose` by DataStorePrefsStorage)   |

---

### 5. Provider abstraction

Path: `backup/src/main/java/com/eahm/drinkwaterapp/backup/`

| File                           | Content                                                                                                                                                    |
|--------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CloudBackupProvider.kt`       | interface with signIn/signOut/isSignedIn/upload/download/getRemoteMetadata/deleteRemoteFile                                                                |
| `BackupProviderRegistry.kt`    | class with get(CloudProviderType): CloudBackupProvider                                                                                                     |
| `GoogleDriveBackupProvider.kt` | Implementation: Drive REST API via Ktor + GoogleSignIn. signIn: GoogleSignIn launcher; upload/download: Drive v3 /files endpoints with appDataFolder space |
| `DropboxBackupProvider.kt`     | Implementation: Dropbox SDK v7. signIn: Auth.startOAuth2PKCE; upload: uploadBuilder; download: download()                                                  |

Provider impls must be **complete enough to compile** — real SDK calls, not TODOs.
Functional testing happens in Fase 2 playground.

---

### 6. Serialization + migration

Path: `backup/src/main/java/com/eahm/drinkwaterapp/backup/`

| File                        | Content                                                                    |
|-----------------------------|----------------------------------------------------------------------------|
| `BackupMigration.kt`        | data class(from, to, migrate: (JsonObject)->JsonObject)                    |
| `BackupMigrationManager.kt` | object: currentSchema=1, migrations=emptyList(), fun migrate() chains them |

`BackupSerializer.kt` lives in `:compose` at
`compose/src/main/java/com/eahm/drinkwaterapp/core/backup/BackupSerializer.kt` and implements
`BackupDataSource`:

```
class BackupSerializer(database, json): BackupDataSource {
    override suspend fun serialize(): String  // reads all history+goals+prefs
    override suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit>  // wipes + re-inserts
}
```

---

### 7. Repository + use cases

Path: `backup/src/main/java/com/eahm/drinkwaterapp/backup/`

| File                          | Key logic                                                                                                 |
|-------------------------------|-----------------------------------------------------------------------------------------------------------|
| `BackupRepository.kt`         | interface: syncState StateFlow + 6 suspend ops                                                            |
| `BackupRepositoryImpl.kt`     | resolves provider from BackupPreferences; returns Failure(NoProviderSelected) if empty                    |
| `ConnectBackupUseCase.kt`     | signOut prev → signIn new → first sync → persist 4 keys                                                   |
| `SyncBackupUseCase.kt`        | Mutex.tryLock → serialize (via BackupDataSource) → SHA-256 hash compare → upload → persist hash+timestamp |
| `RestoreBackupUseCase.kt`     | download → schema check → migrate → deserializeAndImport (via BackupDataSource)                           |
| `CheckBackupStatusUseCase.kt` | getRemoteMetadata → RemoteBackupStatus(isCompatible check)                                                |
| `DisconnectBackupUseCase.kt`  | signOut → clear backup keys (via BackupPreferences) → cancel WorkManager                                  |
| `MigrateBackupUseCase.kt`     | source!=dest precondition → tryLock → download raw → upload raw → update provider key                     |

`BackupErrorMapper.kt` stays in `:compose` at
`compose/src/main/java/com/eahm/drinkwaterapp/core/backup/BackupErrorMapper.kt` because it needs
`UiText` / `R.string.*` from `:compose`.

---

### 8. WorkManager + Scheduler

Path: `backup/src/main/java/com/eahm/drinkwaterapp/backup/`

| File                   | Content                                                                                                                                       |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `CloudBackupWorker.kt` | CoroutineWorker: calls SyncBackupUseCase → Result.success/retry/failure per policy                                                            |
| `BackupScheduler.kt`   | wraps WorkManager: scheduleAutoSync(intervalDays), triggerManualSync(), cancelAll() using BACKUP_SYNC_WORK_PERIODIC + BACKUP_SYNC_WORK_MANUAL |

---

### 9. Koin DI

**New files:**

`backup/src/main/java/com/eahm/drinkwaterapp/backup/di/BackupCoreModule.kt`
(in `:backup`): single Mutex, single Json, singleOf for all providers/use cases/repo,
worker{} for CloudBackupWorker. Does NOT bind BackupDataSource/BackupPreferences (those
implementations live in `:compose`).

`compose/src/main/java/com/eahm/drinkwaterapp/di/BackupModule.kt`
(in `:compose`): includes(backupCoreModule), singleOf(::BackupSerializer) bind BackupDataSource,
ensures DataStorePrefsStorage is bound as BackupPreferences.

`compose/src/main/java/com/eahm/drinkwaterapp/core/backup/BackupSerializer.kt`
implements BackupDataSource (uses AppDatabase + PrefsStorage from `:compose`).

**`DataModule.kt`**: add `includes(backupModule)` — the `:compose` one, which in turn includes
`backupCoreModule`.

---

## FASE 2 — Dev Playground

### New files under `feature/dev/backup/`

`BackupDevState.kt`, `BackupDevAction.kt`, `BackupDevEvent.kt`,
`BackupDevViewModel.kt`, `BackupDevScreen.kt`, `BackupDevRoot.kt`

ViewModel injects: `ConnectBackupUseCase`, `SyncBackupUseCase`, `RestoreBackupUseCase`,
`CheckBackupStatusUseCase`, `DisconnectBackupUseCase`, `MigrateBackupUseCase`, `BackupRepository`,
`PrefsStorage`.

Screen has sections: Google Drive controls, Dropbox controls, Migration controls, Simulation
buttons, scrollable log output.

### `DevRoot.kt` changes

Add `CLOUD_BACKUP` to `DevDestination` enum, add branch in when-expression pointing to
`BackupDevRoot`.

### `FeatureModule.kt`

Add `viewModelOf(::BackupDevViewModel)`.

---

## FASE 3 — Production UI

### New files under `feature/settings/backup/`

| File                           | Description                                                  |
|--------------------------------|--------------------------------------------------------------|
| `ProviderPickerSheet.kt`       | Bottom sheet: Google Drive / Dropbox / Cancel                |
| `ChangeProviderSheet.kt`       | Migrate or switch-without-migrating + current provider shown |
| `MigrationProgressDialog.kt`   | 2-phase indeterminate progress + success/error result        |
| `RestoreConflictDialog.kt`     | "Usar nube" / "Usar local" / Cancelar                        |
| `IncompatibleVersionDialog.kt` | Update CTA → Play Store                                      |
| `BackupSettingsSection.kt`     | Full backup section for SettingsScreen                       |
| `QuotaExceededDialog.kt`       | Shows real file size + storage management URL link           |

### `SettingsScreen.kt`

Add `BackupSettingsSection` call inside `SettingsContent`, behind
`if (FeatureFlags.cloudBackupEnabled)`.

### `SettingsViewModel.kt`

Inject `BackupRepository`, `ConnectBackupUseCase`, `DisconnectBackupUseCase`,
`SyncBackupUseCase`, `RestoreBackupUseCase`, `MigrateBackupUseCase`, `BackupScheduler`.
Expose: `backupState: StateFlow<BackupUiState>`, `isSyncing: StateFlow<Boolean>` (combining repo
state + WorkManager).

### New files under `feature/onboarding/ui/steps/`

`StepBackup.kt` — 4 sub-states A/B/C/D as described in plan.

### `OnboardingState.kt`

Add `data object CloudBackup : OnboardingSteps`.
Update `totalSteps`: `if (FeatureFlags.cloudBackupEnabled) 11 else 10`.
Update `byStep()`: if enabled, step 10 = CloudBackup, step 11 = Summary; else step 10 = Summary.

### `OnboardingViewModel.kt`

Add `onBackupSubState`, `skipBackup()`, `connectBackup(provider)`, `checkOtherProvider()`,
`migrateInOnboarding()`.

### `OnboardingScreen.kt`

Add `OnboardingSteps.CloudBackup -> stepBackup(...)` branch in the when expression.

---

## FASE 4 — Strings

**`compose/src/main/res/values/strings.xml`**
Add all backup strings as defined in the feature plan: `backup_picker_*`, `backup_onb_*`,
`backup_settings_*`, `backup_disconnect_*`, `backup_restore_*`, `backup_conflict_*`,
`backup_incompatible_*`, `backup_error_*`, `backup_change_provider_*`, `backup_migration_*`.

After adding to `strings.xml`, create translated versions for all 18 other language files in
`compose/src/main/res/values-*/strings.xml`. Use placeholder translations (copy of Spanish)
initially; real translations in a follow-up.

---

## Execution order

1. Fase 1 steps 0→9 (compile `:backup` after step 9, then `:compose`)
2. Fase 2 dev playground (compile + smoke test in emulator)
3. Fase 3 production UI + onboarding changes
4. Fase 4 strings
5. `./gradlew :backup:compileDebugKotlin :compose:compileDebugKotlin` must pass after each phase

---

## Verification

1. `./gradlew :backup:compileDebugKotlin :compose:compileDebugKotlin` — no errors after each phase
2. Fase 2: smoke-test both providers (Google Drive + Dropbox) from dev playground in emulator
3. Fase 3: verify onboarding step count is 11 when flag=true, 10 when flag=false
4. Fase 3: verify Settings backup section hidden when flag=false
5. Fase 4: verify no missing string resource errors at build time
