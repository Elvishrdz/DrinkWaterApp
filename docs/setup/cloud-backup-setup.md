# Cloud Backup — Setup Guide

Before testing or releasing the cloud backup feature, complete each step below.

---

## 1. Dropbox

### 1.1 Create a Dropbox app

1. Go to [https://www.dropbox.com/developers/apps](https://www.dropbox.com/developers/apps)
2. Click **Create app**
3. Select **Scoped access** → **App folder**
4. Name the app (e.g. `DrinkWaterBackup`)
5. Under **Permissions**, enable:
    - `files.content.write`
    - `files.content.read`
6. Under **Settings**, copy the **App key**

### 1.2 Add the app key to `local.properties`

```
dropbox.app.key=YOUR_DROPBOX_APP_KEY
```

> `local.properties` is gitignored — never commit this file.

---

## 2. Google Drive

### 2.1 Enable the Drive API

1. Go to [https://console.cloud.google.com](https://console.cloud.google.com)
2. Select the project linked to your `google-services.json`
3. Navigate to **APIs & Services → Library**
4. Search for **Google Drive API** and click **Enable**

### 2.2 Create OAuth credentials

1. Go to **APIs & Services → Credentials**
2. Click **Create Credentials → OAuth client ID**
3. Select **Android**
4. Enter the package name: `com.eahm.drinkwaterapp`
5. Enter the **SHA-1 fingerprint** of your debug keystore:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore \
     -alias androiddebugkey -storepass android -keypass android
   ```
6. Click **Create** — no file download needed; the client ID is embedded in `google-services.json`

### 2.3 Verify `google-services.json`

Make sure the Drive API scope is not blocked. The sign-in flow requests:

```
https://www.googleapis.com/auth/drive.appdata
```

This scope stores files in the app's private Drive folder — invisible to the user's Drive.

---

## 3. Wire the OAuth flow in the UI

> The backend (providers, use cases, repository) is complete.
> The Settings screen still needs ActivityResultLauncher integration.

### 3.1 Google Drive — `SettingsScreen.kt`

Add a launcher that receives the sign-in intent result:

```kotlin
val googleSignInLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    viewModel.onGoogleSignInResult(result.resultCode, result.data)
}
```

In `SettingsViewModel`, add:

```kotlin
fun getGoogleSignInIntent(): Intent? =
    googleDriveProvider.getSignInIntent(application)

fun onGoogleSignInResult(resultCode: Int, data: Intent?) {
    viewModelScope.launch {
        val provider = CloudProviderType.GOOGLE_DRIVE
        backupRepository.connect(application, provider)
        refreshBackupState()
    }
}
```

### 3.2 Dropbox — `SettingsScreen.kt`

Dropbox uses an external browser/app; the result arrives via `onResume`.
Use `LifecycleEventEffect` (or `DisposableEffect`) to check for the token:

```kotlin
LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.onDropboxSignInResume()
}
```

In `SettingsViewModel`, add:

```kotlin
fun onDropboxSignInResume() {
    viewModelScope.launch {
        val result = dropboxProvider.handleSignInResult(application, Activity.RESULT_OK, null)
        if (result is BackupResult.Success) {
            backupRepository.connect(application, CloudProviderType.DROPBOX)
            refreshBackupState()
        }
    }
}
```

---

## 4. Feature flag

The feature is enabled only in **debug** builds by default.

| Variant | `FEATURE_CLOUD_BACKUP` |
|---------|------------------------|
| debug   | `true`                 |
| staging | `false`                |
| qa      | `false`                |
| release | `false`                |

To enable it in another variant, edit `compose/build.gradle.kts`:

```kotlin
buildConfigField("boolean", "FEATURE_CLOUD_BACKUP", "true")
```

---

## Checklist

- [ ] `dropbox.app.key` added to `local.properties`
- [ ] Google Drive API enabled in Google Cloud Console
- [ ] Android OAuth client created with correct SHA-1
- [ ] `ActivityResultLauncher` wired for Google Sign-In in `SettingsScreen`
- [ ] `onResume` handler wired for Dropbox in `SettingsScreen`
- [ ] Smoke-tested in emulator via dev playground (`Dev → Backup`)
