#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# create-feature.sh
# Scaffolds a new feature following the DrinkWaterApp MVI pattern.
#
# Usage:
#   bash docs/templates/create-feature.sh <FeatureName>
#
# Example:
#   bash docs/templates/create-feature.sh Reminder
#
# Generated files (inside compose/src/main/java/…/feature/<featurelower>/):
#   <Feature>State.kt
#   <Feature>Action.kt
#   <Feature>Event.kt
#   <Feature>ViewModel.kt
#   <Feature>Root.kt
#   <Feature>Screen.kt
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

# ── Validate input ────────────────────────────────────────────────────────────
if [[ $# -lt 1 ]]; then
  echo "Usage: bash docs/templates/create-feature.sh <FeatureName>"
  echo "Example: bash docs/templates/create-feature.sh Reminder"
  exit 1
fi

FEATURE="$1"                                        # e.g. "Reminder"
FEATURE_LOWER="$(echo "$FEATURE" | tr '[:upper:]' '[:lower:]')"  # e.g. "reminder"
PKG="com.eahm.drinkwaterapp"

# ── Resolve project root (script can be run from any directory) ───────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

PKG_PATH="$(echo "$PKG" | tr '.' '/')"
FEATURE_DIR="$PROJECT_ROOT/compose/src/main/java/$PKG_PATH/feature/$FEATURE_LOWER"

if [[ -d "$FEATURE_DIR" ]]; then
  echo "⚠  Directory already exists: $FEATURE_DIR"
  echo "   Delete it first if you want to regenerate."
  exit 1
fi

mkdir -p "$FEATURE_DIR"
echo "✓  Created $FEATURE_DIR"

# ── Helper: write a file and confirm ─────────────────────────────────────────
write_file() {
  local path="$1"
  local content="$2"
  printf '%s\n' "$content" > "$path"
  echo "✓  $(basename "$path")"
}

# ─────────────────────────────────────────────────────────────────────────────
# 1. <Feature>State.kt
# ─────────────────────────────────────────────────────────────────────────────
write_file "$FEATURE_DIR/${FEATURE}State.kt" \
"package $PKG.feature.$FEATURE_LOWER

import androidx.compose.runtime.Immutable

internal sealed interface ${FEATURE}State {

    data object Loading : ${FEATURE}State

    @Immutable
    data class Data(
        // TODO: add your state fields here
    ) : ${FEATURE}State
}"

# ─────────────────────────────────────────────────────────────────────────────
# 2. <Feature>Action.kt
# ─────────────────────────────────────────────────────────────────────────────
write_file "$FEATURE_DIR/${FEATURE}Action.kt" \
"package $PKG.feature.$FEATURE_LOWER

internal sealed interface ${FEATURE}Action {
    // TODO: define user-initiated actions, e.g.:
    // data class OnItemClicked(val id: Int) : ${FEATURE}Action
}"

# ─────────────────────────────────────────────────────────────────────────────
# 3. <Feature>Event.kt
# ─────────────────────────────────────────────────────────────────────────────
write_file "$FEATURE_DIR/${FEATURE}Event.kt" \
"package $PKG.feature.$FEATURE_LOWER

internal sealed interface ${FEATURE}Event {
    // TODO: define one-shot side effects, e.g.:
    // data object ShowError : ${FEATURE}Event
    // data class Navigate(val destination: String) : ${FEATURE}Event
}"

# ─────────────────────────────────────────────────────────────────────────────
# 4. <Feature>ViewModel.kt
# ─────────────────────────────────────────────────────────────────────────────
write_file "$FEATURE_DIR/${FEATURE}ViewModel.kt" \
"package $PKG.feature.$FEATURE_LOWER

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

internal class ${FEATURE}ViewModel(
    private val prefsStorage: PrefsStorage,
) : ViewModel() {

    // TODO: replace flowOf(...) with your real data flows and .combine() chains
    val state: StateFlow<${FEATURE}State> = flowOf(${FEATURE}State.Loading)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ${FEATURE}State.Loading,
        )

    fun onAction(action: ${FEATURE}Action) {
        when (action) {
            // TODO: handle actions
        }
    }
}"

# ─────────────────────────────────────────────────────────────────────────────
# 5. <Feature>Root.kt
# ─────────────────────────────────────────────────────────────────────────────
write_file "$FEATURE_DIR/${FEATURE}Root.kt" \
"package $PKG.feature.$FEATURE_LOWER

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ${FEATURE}Root(
    // TODO: add navigation callbacks, e.g. onNavigateBack: () -> Unit,
    viewModel: ${FEATURE}ViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        ${FEATURE}State.Loading -> { /* Idle — show nothing or a loading indicator */ }

        is ${FEATURE}State.Data -> {
            ${FEATURE}Screen(
                state = data,
                onAction = viewModel::onAction,
            )
        }
    }
}"

# ─────────────────────────────────────────────────────────────────────────────
# 6. <Feature>Screen.kt
# ─────────────────────────────────────────────────────────────────────────────
write_file "$FEATURE_DIR/${FEATURE}Screen.kt" \
"package $PKG.feature.$FEATURE_LOWER

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

@Composable
internal fun ${FEATURE}Screen(
    state: ${FEATURE}State.Data,
    onAction: (${FEATURE}Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        // TODO: build your screen content here
        // Content(modifier = Modifier.padding(innerPadding))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun ${FEATURE}ScreenPreview() {
    DrinkWaterAppTheme {
        ${FEATURE}Screen(
            state = ${FEATURE}State.Data(),
            onAction = {},
        )
    }
}"

# ─────────────────────────────────────────────────────────────────────────────
# Done — print next steps
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  ${FEATURE} feature scaffolded successfully!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Next steps:"
echo ""
echo "  1. Register the ViewModel in Koin (FeatureModule.kt or similar):"
echo "       viewModelOf(::${FEATURE}ViewModel)"
echo ""
echo "  2. Add a destination to HomeDestination enum (HomeScreen.kt):"
echo "       $(echo "$FEATURE" | tr '[:lower:]' '[:upper:]')(R.string.${FEATURE_LOWER}_title, DwIcons.MyIcon, DwIcons.MyIconFilled),"
echo ""
echo "  3. Wire the Root in HomeScreen's when(currentDestination) block:"
echo "       HomeDestination.$(echo "$FEATURE" | tr '[:lower:]' '[:upper:]') -> ${FEATURE}Root()"
echo ""
echo "  4. Add string resource:"
echo "       <string name=\"${FEATURE_LOWER}_title\">${FEATURE}</string>"
echo ""
echo "  5. Implement ${FEATURE}State.Data fields and fill the ViewModel flows."
echo ""
