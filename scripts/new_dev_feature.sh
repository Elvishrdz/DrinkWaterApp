#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# new_dev_feature.sh
# Scaffolds a new MVI dev feature under feature/dev/<name>/
# Usage: ./scripts/new_dev_feature.sh
# ──────────────────────────────────────────────────────────────────────────────

set -euo pipefail

# ── Paths ─────────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BASE_SRC="$PROJECT_ROOT/compose/src/main/java/com/eahm/drinkwaterapp"
DEV_ROOT_FILE="$BASE_SRC/feature/dev/DevRoot.kt"
FEATURE_MODULE_FILE="$BASE_SRC/di/FeatureModule.kt"

# ── Colors ────────────────────────────────────────────────────────────────────
BOLD='\033[1m'
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
DIM='\033[2m'
RESET='\033[0m'

ICONS=(Drop Wave Settings Glass Star Plus ChevronRight Bell Clock Sun Moon Share History Trash Ruler)

# ── Helpers ───────────────────────────────────────────────────────────────────
header() { echo -e "\n${BOLD}${CYAN}$1${RESET}"; }
info()   { echo -e "  ${DIM}$1${RESET}"; }
ok()     { echo -e "  ${GREEN}✓${RESET} $1"; }
warn()   { echo -e "  ${YELLOW}!${RESET} $1"; }
err()    { echo -e "  ${RED}✗${RESET} $1"; }

lower() { echo "$1" | tr '[:upper:]' '[:lower:]'; }

ask() {
    local prompt="$1" default="${2:-}" var
    if [[ -n "$default" ]]; then
        read -r -p "  $prompt [$default]: " var
        echo "${var:-$default}"
    else
        read -r -p "  $prompt: " var
        echo "$var"
    fi
}

ask_yn() {
    local prompt="$1" default="${2:-y}" var lvar
    read -r -p "  $prompt [${default}]: " var
    var="${var:-$default}"
    lvar=$(lower "$var")
    [[ "$lvar" == "y" || "$lvar" == "yes" ]]
}

pick_icon() {
    echo -e "  Available DwIcons:"
    local i=1
    for icon in "${ICONS[@]}"; do
        printf "    %2d) %s\n" "$i" "$icon"
        ((i++))
    done
    while true; do
        local choice
        choice=$(ask "Pick an icon (number or name)" "Bell")
        # Accept number
        if [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#ICONS[@]} )); then
            echo "${ICONS[$((choice-1))]}"
            return
        fi
        # Accept name (case-insensitive)
        for icon in "${ICONS[@]}"; do
            if [[ "$(lower "$icon")" == "$(lower "$choice")" ]]; then
                echo "$icon"
                return
            fi
        done
        warn "Not found. Enter a number (1–${#ICONS[@]}) or an icon name."
    done
}

# ── Validate feature name ──────────────────────────────────────────────────────
validate_name() {
    local name="$1"
    [[ "$name" =~ ^[A-Z][a-zA-Z0-9]+$ ]]
}

# ── Main ──────────────────────────────────────────────────────────────────────
run_main() {

# ── Collect inputs ─────────────────────────────────────────────────────────────
header "Dev Feature Scaffolder"
echo -e "  Creates the MVI boilerplate for a new dev playground screen."
echo

# 1. Feature name (PascalCase)
while true; do
    FEATURE_NAME=$(ask "Feature name (PascalCase, e.g. Notifications)")
    if validate_name "$FEATURE_NAME"; then break; fi
    warn "Must start with an uppercase letter and contain only letters/digits."
done

FEATURE_LOWER=$(lower "$FEATURE_NAME")
FEATURE_PACKAGE="com.eahm.drinkwaterapp.feature.dev.$FEATURE_LOWER"
FEATURE_DIR="$BASE_SRC/feature/dev/$FEATURE_LOWER"

# 2. Menu label (shown on the Dev menu card)
LABEL=$(ask "Menu label (shown on card)" "$FEATURE_NAME")

# 3. Icon
echo
echo -e "  ${BOLD}Icon${RESET}"
ICON=$(pick_icon)

# 4. Destination enum key (SCREAMING_SNAKE_CASE, derived from name)
ENUM_KEY=$(echo "$FEATURE_NAME" | sed 's/\([A-Z]\)/_\1/g' | sed 's/^_//' | tr '[:lower:]' '[:upper:]')
ENUM_KEY=$(ask "DevDestination enum key" "$ENUM_KEY")

# 5. Auto-patch DevRoot and FeatureModule?
echo
AUTO_PATCH=false
if ask_yn "Auto-patch DevRoot.kt and FeatureModule.kt?" "y"; then
    AUTO_PATCH=true
fi

# ── Preview ────────────────────────────────────────────────────────────────────
header "Preview"
echo
echo -e "  ${BOLD}Files to create:${RESET}"
info "$FEATURE_DIR/AlarmsState.kt  → ${FEATURE_NAME}State.kt"
info "$FEATURE_DIR/${FEATURE_NAME}State.kt"
info "$FEATURE_DIR/${FEATURE_NAME}ViewModel.kt"
info "$FEATURE_DIR/${FEATURE_NAME}Screen.kt"
info "$FEATURE_DIR/${FEATURE_NAME}Root.kt"

echo
echo -e "  ${BOLD}Package:${RESET} $FEATURE_PACKAGE"
echo -e "  ${BOLD}Icon:${RESET}    DwIcons.$ICON"
echo -e "  ${BOLD}Label:${RESET}   \"$LABEL\""
echo -e "  ${BOLD}Enum key:${RESET} $ENUM_KEY"

echo
echo -e "  ${BOLD}Modifications to DevRoot.kt:${RESET}"
echo -e "  ${DIM}+ ${ENUM_KEY}(\"${LABEL}\", DwIcons.${ICON})${RESET}   ← added to DevDestination enum"
echo -e "  ${DIM}+ DevDestination.${ENUM_KEY} -> ${FEATURE_NAME}Root(onBackClicked = onBackClicked)${RESET}"

echo
echo -e "  ${BOLD}Modifications to FeatureModule.kt:${RESET}"
echo -e "  ${DIM}+ viewModelOf(::${FEATURE_NAME}ViewModel)${RESET}"

if [[ "$AUTO_PATCH" == "false" ]]; then
    warn "Auto-patch disabled — you will need to add these manually."
fi

# ── Confirm or adjust ──────────────────────────────────────────────────────────
echo
if ! ask_yn "Looks good? Generate now?" "y"; then
    echo
    warn "Aborted. Re-run the script to start over."
    exit 0
fi

# ── Check target dir doesn't already exist ─────────────────────────────────────
if [[ -d "$FEATURE_DIR" ]]; then
    err "Directory already exists: $FEATURE_DIR"
    err "Remove it first or choose a different name."
    exit 1
fi

# ── Generate files ─────────────────────────────────────────────────────────────
header "Generating"
mkdir -p "$FEATURE_DIR"

# ── AlarmsState.kt ─────────────────────────────────────────────────────────────
cat > "$FEATURE_DIR/${FEATURE_NAME}State.kt" <<KOTLIN
package $FEATURE_PACKAGE

// UI state (Loading skeleton → Data)
internal sealed interface ${FEATURE_NAME}State {
    data object Loading : ${FEATURE_NAME}State
    class Data(
        // fields added as the feature grows
    ) : ${FEATURE_NAME}State
}

// User intents (UI → ViewModel)
internal sealed interface ${FEATURE_NAME}Action {
    // actions added as the feature grows
}

// One-shot side effects (ViewModel → UI)
internal sealed interface ${FEATURE_NAME}Event {
    // events added as the feature grows
}
KOTLIN
ok "${FEATURE_NAME}State.kt"

# ── ViewModel ──────────────────────────────────────────────────────────────────
cat > "$FEATURE_DIR/${FEATURE_NAME}ViewModel.kt" <<KOTLIN
package $FEATURE_PACKAGE

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class ${FEATURE_NAME}ViewModel : ViewModel() {

    private val _state = MutableStateFlow<${FEATURE_NAME}State>(${FEATURE_NAME}State.Loading)
    val state: StateFlow<${FEATURE_NAME}State> = _state

    private val _events = Channel<${FEATURE_NAME}Event>(Channel.BUFFERED)
    val events: Flow<${FEATURE_NAME}Event> = _events.receiveAsFlow()

    fun onAction(action: ${FEATURE_NAME}Action) {
        // handled as the feature grows
    }
}
KOTLIN
ok "${FEATURE_NAME}ViewModel.kt"

# ── Screen ─────────────────────────────────────────────────────────────────────
cat > "$FEATURE_DIR/${FEATURE_NAME}Screen.kt" <<KOTLIN
package $FEATURE_PACKAGE

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ${FEATURE_NAME}Screen(
    state: ${FEATURE_NAME}State,
    onAction: (${FEATURE_NAME}Action) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$LABEL") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) {
                        Text("← Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is ${FEATURE_NAME}State.Loading -> CircularProgressIndicator()
                is ${FEATURE_NAME}State.Data    -> Text("$LABEL playground")
            }
        }
    }
}
KOTLIN
ok "${FEATURE_NAME}Screen.kt"

# ── Root ───────────────────────────────────────────────────────────────────────
cat > "$FEATURE_DIR/${FEATURE_NAME}Root.kt" <<KOTLIN
package $FEATURE_PACKAGE

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ${FEATURE_NAME}Root(onBackClicked: () -> Unit) {
    val viewModel: ${FEATURE_NAME}ViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { /* handled as feature grows */ }
    }

    BackHandler(onBack = onBackClicked)

    ${FEATURE_NAME}Screen(
        state = state,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
KOTLIN
ok "${FEATURE_NAME}Root.kt"

# ── Patch DevRoot.kt ───────────────────────────────────────────────────────────
if [[ "$AUTO_PATCH" == "true" ]]; then
    echo

    # Add enum entry after the last entry in the enum block
    LAST_ENUM_ENTRY=$(grep -E '^\s+\w+\("[^"]+",\s*DwIcons\.' "$DEV_ROOT_FILE" | tail -1)
    NEW_ENUM_ENTRY="    ${ENUM_KEY}(\"${LABEL}\", DwIcons.${ICON}),"

    if grep -q "${ENUM_KEY}" "$DEV_ROOT_FILE"; then
        warn "DevRoot.kt already contains $ENUM_KEY — skipping enum entry."
    else
        # Insert after the last enum entry line
        sed -i '' "/${LAST_ENUM_ENTRY//\//\\/}/a\\
$NEW_ENUM_ENTRY
" "$DEV_ROOT_FILE"
        ok "Added $ENUM_KEY to DevDestination enum"
    fi

    # Add import for Root
    IMPORT_LINE="import $FEATURE_PACKAGE.${FEATURE_NAME}Root"
    if grep -q "${FEATURE_NAME}Root" "$DEV_ROOT_FILE"; then
        warn "DevRoot.kt already imports ${FEATURE_NAME}Root — skipping import."
    else
        sed -i '' "/import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.AlarmCalculatorRoot/a\\
$IMPORT_LINE
" "$DEV_ROOT_FILE"
        ok "Added import for ${FEATURE_NAME}Root"
    fi

    # Add when branch before the closing brace of the when block
    WHEN_BRANCH="        DevDestination.${ENUM_KEY} -> ${FEATURE_NAME}Root(onBackClicked = onBackClicked)"
    if grep -q "DevDestination.${ENUM_KEY}" "$DEV_ROOT_FILE"; then
        warn "DevRoot.kt already has a branch for $ENUM_KEY — skipping."
    else
        # Find the line with the last ->  inside the when block and insert after it
        LAST_WHEN_LINE=$(grep -n "onBackClicked = onBackClicked" "$DEV_ROOT_FILE" | tail -1 | cut -d: -f1)
        # Insert two lines: the branch closing paren is on LAST_WHEN_LINE+1
        INSERT_AT=$((LAST_WHEN_LINE + 1))
        sed -i '' "${INSERT_AT}a\\
\\
$WHEN_BRANCH" "$DEV_ROOT_FILE"
        ok "Added when branch for $ENUM_KEY in DevRoot.kt"
    fi

    # Patch FeatureModule.kt
    VM_LINE="    viewModelOf(::${FEATURE_NAME}ViewModel)"
    IMPORT_VM="import $FEATURE_PACKAGE.${FEATURE_NAME}ViewModel"

    if grep -q "${FEATURE_NAME}ViewModel" "$FEATURE_MODULE_FILE"; then
        warn "FeatureModule.kt already has ${FEATURE_NAME}ViewModel — skipping."
    else
        # Add import after AlarmCalculatorViewModel import
        sed -i '' "/import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.AlarmCalculatorViewModel/a\\
$IMPORT_VM
" "$FEATURE_MODULE_FILE"
        # Add viewModelOf after AlarmCalculatorViewModel
        sed -i '' "/viewModelOf(::AlarmCalculatorViewModel)/a\\
$VM_LINE
" "$FEATURE_MODULE_FILE"
        ok "Registered ${FEATURE_NAME}ViewModel in FeatureModule.kt"
    fi
else
    # Print manual instructions
    header "Manual steps required"
    echo
    echo -e "  ${BOLD}1. DevRoot.kt${RESET} — add to DevDestination enum:"
    echo -e "  ${DIM}    ${ENUM_KEY}(\"${LABEL}\", DwIcons.${ICON}),${RESET}"
    echo
    echo -e "  ${BOLD}2. DevRoot.kt${RESET} — add import:"
    echo -e "  ${DIM}    import $FEATURE_PACKAGE.${FEATURE_NAME}Root${RESET}"
    echo
    echo -e "  ${BOLD}3. DevRoot.kt${RESET} — add when branch:"
    echo -e "  ${DIM}    DevDestination.${ENUM_KEY} -> ${FEATURE_NAME}Root(onBackClicked = onBackClicked)${RESET}"
    echo
    echo -e "  ${BOLD}4. FeatureModule.kt${RESET} — add import and registration:"
    echo -e "  ${DIM}    import $FEATURE_PACKAGE.${FEATURE_NAME}ViewModel${RESET}"
    echo -e "  ${DIM}    viewModelOf(::${FEATURE_NAME}ViewModel)${RESET}"
fi

header "Done"
echo -e "  Feature ${BOLD}$FEATURE_NAME${RESET} created at:"
echo -e "  ${DIM}$FEATURE_DIR${RESET}"
echo -e "  Run ${BOLD}./gradlew :compose:compileDebugKotlin${RESET} to verify."
echo
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_main
fi
