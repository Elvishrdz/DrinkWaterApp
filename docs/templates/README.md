# DrinkWaterApp — Code Templates

Two tools to accelerate feature development: Android Studio Live Templates and a feature scaffolding
script.

---

## 1. Android Studio Live Templates (`DrinkWaterApp.xml`)

Live Templates let you type a short abbreviation and press **Tab** to expand it into a full code
block — just like the built-in `comp` → `@Composable` snippet.

### How to install (automatic — recommended)

Run once from the project root:

```bash
bash docs/templates/install-templates.sh           # all Android Studio versions
bash docs/templates/install-templates.sh --latest  # latest stable version only
```

Then **restart Android Studio** (or *File → Invalidate Caches → Just Restart*).

### How to import (manual alternative)

1. Open **Android Studio**
2. Go to **Settings / Preferences → Editor → Live Templates**
3. Click the **⚙ gear icon → Import** (top-right of the panel)
4. Select `docs/templates/DrinkWaterApp.xml`
5. All templates appear under the **DrinkWaterApp** group

### Available abbreviations

| Abbreviation     | Expands to                                                                    |
|------------------|-------------------------------------------------------------------------------|
| `dwcomp`         | Basic `@Composable` function with `modifier` param                            |
| `dwprev`         | `@Preview` composable wrapped in `DrinkWaterAppTheme`                         |
| `dwstate`        | Feature State sealed interface (`Loading` + `Data`)                           |
| `dwaction`       | Feature Action sealed interface                                               |
| `dwevent`        | Feature Event sealed interface                                                |
| `dwvm`           | Feature ViewModel with `StateFlow` + `stateIn`                                |
| `dwroot`         | Feature Root: `koinViewModel` + `collectAsStateWithLifecycle` + `when(state)` |
| `dwscreen`       | Feature Screen with `Scaffold` + `innerPadding`                               |
| `dwtest`         | JUnit4 `@Test` method with AAA pattern (backtick name)                        |
| `dwtestclass`    | Full JUnit4 unit test class skeleton                                          |
| `dwtestui`       | Compose UI test class with `createComposeRule`                                |
| `dwinst`         | Instrumented test class with `@RunWith(AndroidJUnit4::class)`                 |
| `dwnav`          | `HomeDestination` enum entry (state-based navigation)                         |
| `dwshared`       | `.sharedElement()` Modifier chain                                             |
| `dwsharedlayout` | `SharedTransitionLayout` + `AnimatedContent` scaffold                         |

### Usage example

Inside any `.kt` file, type `dwroot` and press **Tab**:

```kotlin
// Before (you type):
dwroot<Tab>

// After (expanded):
@Composable
internal fun FeatureRoot(

    viewModel: FeatureViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        FeatureState.Loading -> { /* Idle */
        }

        is FeatureState.Data -> {
            FeatureScreen(
                state = data,

                )
        }
    }
}
```

Android Studio cycles through each `$VARIABLE$` placeholder as you press Tab.

---

## 2. Feature Scaffolding Script (`create-feature.sh`)

Generates the complete file structure for a new feature in one command.

### Usage

Run from the project root:

```bash
bash docs/templates/create-feature.sh <FeatureName>
```

**Example:**

```bash
bash docs/templates/create-feature.sh Reminder
```

### Generated files

```
compose/src/main/java/…/feature/reminder/
├── ReminderState.kt       # sealed interface Loading + Data
├── ReminderAction.kt      # user-initiated actions
├── ReminderEvent.kt       # one-shot side effects
├── ReminderViewModel.kt   # StateFlow + onAction
├── ReminderRoot.kt        # koinViewModel + when(state)
└── ReminderScreen.kt      # Scaffold + @Preview
```

### After running the script

The script prints the exact next steps:

1. Register `ReminderViewModel` in Koin (`FeatureModule.kt`)
2. Add `REMINDER` to the `HomeDestination` enum (`HomeScreen.kt`)
3. Add the `ReminderRoot()` call to `HomeScreen`'s `when(currentDestination)` block
4. Add a string resource `reminder_title`
5. Implement `ReminderState.Data` fields and ViewModel flows

---

## Project patterns at a glance

| Pattern      | Detail                                                              |
|--------------|---------------------------------------------------------------------|
| Navigation   | State-based `HomeDestination` enum — no NavController               |
| State        | Sealed interface `Loading` / `Data` (immutable)                     |
| DI           | Koin — `koinViewModel()` in composables, `viewModelOf()` in modules |
| Flows        | `combine()` chains → `stateIn(Lazily)`                              |
| Unit tests   | JUnit4, `kotlin.test.assertEquals`, backtick names, AAA             |
| UI tests     | `createComposeRule()`, `@RunWith(AndroidJUnit4::class)`             |
| Instrumented | `InstrumentationRegistry`, `@Before`/`@After`, `org.junit.Assert.*` |
