# In-App Language Selection

**Feature:** Allow the user to select the app language from Settings → App section.
**Supported locales (from `locale_config.xml`):** `en-US`, `es-Es`
**minSdk:** 24

---

## API-Level Strategy

The recommended API for in-app language preference is
`AppCompatDelegate.setApplicationLocales(LocaleListCompat)` from
`androidx.appcompat:appcompat:1.6.0+` (currently `1.7.1` in the version catalog).

| API Level                 | Behavior                                                                                                                                                                                                                                                                                                                                                                |
|---------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **API 33+** (Android 13+) | `AppCompatDelegate` delegates to the native `LocaleManager`. The system stores the preference, restores it on restart automatically. `android:localeConfig` in the manifest is required — already present.                                                                                                                                                              |
| **API 24–32**             | AppCompat backport: preference stored in `SharedPreferences` by the library. **Requires the Activity to extend `AppCompatActivity`** so `AppCompatDelegate` intercepts `attachBaseContext` and reapplies the saved locale on every restart. `AppCompatActivity` extends `ComponentActivity`, so Compose `setContent {}` and all lifecycle APIs remain fully compatible. |

`minSdk = 24` means no devices below API 24, so the backport covers 100% of legacy devices.

---

## UI: Language Dialog

Uses `BasicAlertDialog` — the same pattern as `RevokeTrackingDialog`, `ClearAllDataDialog`, etc. in
`SettingsScreen.kt`.

**Structure:**

```
┌──────────────────────────────┐
│  Language                    │  ← title (Bold, 18sp)
│                              │
│  ○  System default           │  ← first option (tag = "")
│  ●  English                  │  ← en-US
│  ○  Español                  │  ← es-Es
│                              │
│  [ Apply changes ]           │  ← DwPrimaryButton (applies and dismisses)
│  [ Cancel         ]          │  ← DwSecondaryButton (dismisses, no change)
└──────────────────────────────┘
```

**Behaviour:**

- Dialog opens with the current locale pre-selected (RadioButton).
- User can change selection freely without any immediate effect.
- **"Apply changes"** → calls `onLanguageSelected(pendingTag)` → dismisses. The Activity recreates
  itself (locale change triggers recomposition/recreation).
- **"Cancel"** / dismiss by tapping outside → closes without applying.
- Selection is held in a local `var pendingLocaleTag by remember { mutableStateOf(current) }` inside
  the dialog.

**Language display names** are shown in the language's own script (standard UX for language
pickers):

- `en-US` → "English"
- `es-Es` → "Español"

Derived at runtime:
`Locale.forLanguageTag(tag).getDisplayLanguage(Locale.forLanguageTag(tag)).replaceFirstChar { it.uppercase() }`

---

## Files to Change

### 1. `compose/build.gradle.kts`

Add (already in `libs.versions.toml` at `1.7.1`):

```kotlin
implementation(libs.androidx.appcompat)
```

### 2. `MainActivity.kt`

```kotlin
// Before
class MainActivity : ComponentActivity()

// After
import androidx . appcompat . app . AppCompatActivity
class MainActivity : AppCompatActivity()
```

No other changes. `AppCompatActivity` automatically hooks into `attachBaseContext` to restore the
saved locale on pre-33 restarts.

### 3. `SettingsState.kt`

Add:

```kotlin
data class SettingsState(
    ...
    val currentLocaleTag: String = "",  // "" = system default; "en-US", "es-Es" otherwise
)
```

### 4. `SettingsViewModel.kt`

**New function:**

```kotlin
fun onLanguageChanged(tag: String) {
    val locales = if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
    else LocaleListCompat.forLanguageTags(tag)
    AppCompatDelegate.setApplicationLocales(locales)
    // No _state.update needed — Activity recreates automatically on locale change
}
```

**In the `state` combine chain**, add `currentLocaleTag` reading from `AppCompatDelegate`:

```kotlin
.combine(prefsStorage.getUnitPreference()) { state, unitPref ->
    state.copy(
        unitPreference = unitPref,
        currentLocaleTag = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .let { if (it == "und" || it.isBlank()) "" else it },
    )
}
```

> `toLanguageTags()` returns `"und"` when no locale is set (system default). Normalise to `""`.

### 5. `feature/settings/ui/LanguageDialog.kt` *(new file)*

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguageDialog(
    current: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
)
```

Internal structure:

- `var pendingTag by remember { mutableStateOf(current) }`
- `BasicAlertDialog(onDismissRequest = onDismiss)` wrapping a `Column` with:
    - `clip(RoundedCornerShape(20.dp))` + `background(MaterialTheme.colorScheme.background)` +
      `padding(24.dp)`
    - Title text
    - `Spacer(8.dp)`
    - `LanguageOption` for system default (tag `""`)
    - `LanguageOption` for each entry in `SUPPORTED_LOCALES`
    - `Spacer(24.dp)`
    - `DwPrimaryButton("Apply changes")` → `onLanguageSelected(pendingTag); onDismiss()`
    - `Spacer(8.dp)`
    - `DwSecondaryButton("Cancel")` → `onDismiss()`

Private helper:

```kotlin
@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit)
```

Same layout as `TimeFormatOption` in `TimeFormatBottomSheet.kt`: `Row` + `RadioButton` + `Text`.

**Supported locales constant** (mirrors `locale_config.xml`, kept in sync manually):

```kotlin
private val SUPPORTED_LOCALES = listOf("en-US", "es-Es")
```

### 6. `SettingsScreen.kt`

**State variable** (alongside the other `showXxxSheet` / `showXxxDialog` vars):

```kotlin
var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
```

**New row in `settings_group_app` section** (after Time Format row):

```kotlin
HorizontalDivider(color = DwInk100)
SettingRow(
    icon = DwIcons.Globe,
    iconBg = DwTintWater,
    iconTint = DwWater,
    label = stringResource(R.string.settings_row_language),
    onClick = { showLanguageDialog = true },
) {
    LanguageBadge(tag = state.currentLocaleTag)
}
```

**`LanguageBadge` composable** (same style as `UnitPreferenceBadge`, private in
`SettingsScreen.kt`):

```kotlin
@Composable
private fun LanguageBadge(tag: String) {
    val label = if (tag.isEmpty()) stringResource(R.string.settings_language_system)
    else Locale.forLanguageTag(tag).getDisplayLanguage(Locale.forLanguageTag(tag))
        .replaceFirstChar { it.uppercase() }
    // Row { Box(badge pill) { Text(label) }; Icon(ChevronRight) }
}
```

**Dialog placement** (alongside the other `if (showXxxDialog)` blocks):

```kotlin
// ── Language dialog ───────────────────────────────────────────────────────────
if (showLanguageDialog) {
    LanguageDialog(
        current = state.currentLocaleTag,
        onLanguageSelected = { tag ->
            showLanguageDialog = false
            viewModel.onLanguageChanged(tag)
        },
        onDismiss = { showLanguageDialog = false },
    )
}
```

### 7. String resources

**`res/values/strings.xml`:**

```xml

<string name="settings_row_language">Language</string><string name="settings_language_system">System
default
</string><string name="settings_language_apply">Apply changes</string>
```

**`res/values-es/strings.xml`:**

```xml

<string name="settings_row_language">Idioma</string><string name="settings_language_system">
Predeterminado del sistema
</string><string name="settings_language_apply">Aplicar cambios</string>
```

> `action_cancel` string already exists in the app — reuse it for the Cancel button.

---

## What Does NOT Change

- `PrefsStorage` / DataStore — AppCompat owns locale storage.
- `locale_config.xml` — already correct.
- `AndroidManifest.xml` — `android:localeConfig` already present.
- No `autoStoreLocales` metadata needed — covered by extending `AppCompatActivity`.

---

## Icon

`DwIcons.Globe` — created in `DwIcons.kt`. Three paths: outer circle (r=9), central meridian (
vertical ellipse, a=4 b=9), equator line. Same stroke style (2f, Round cap/join) as all other icons
in the file. Added to `DwIconsPreviewProvider` for design review.
