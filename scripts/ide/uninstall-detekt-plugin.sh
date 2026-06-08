#!/usr/bin/env bash
# =============================================================================
# uninstall-detekt-plugin.sh
#
# Removes the Detekt IntelliJ plugin from Android Studio's plugins directory.
#
# Usage:
#   bash scripts/ide/uninstall-detekt-plugin.sh
#
# Idempotent: exits cleanly if the plugin is not installed.
# Requires: macOS
# =============================================================================

set -euo pipefail

PASS="✅"
SKIP="↩️ "
FAIL="❌"
INFO="ℹ️ "

PLUGIN_ID="detekt-intellij-plugin"

# -----------------------------------------------------------------------------
# macOS guard
# -----------------------------------------------------------------------------
if [[ "$(uname)" != "Darwin" ]]; then
  echo "  $FAIL This script supports macOS only."
  exit 1
fi

# -----------------------------------------------------------------------------
# Locate the newest Android Studio plugins directory
# -----------------------------------------------------------------------------
AS_SUPPORT_DIR="$HOME/Library/Application Support/Google"

PLUGINS_DIR=""
while IFS= read -r dir; do
  if [[ -d "$dir/plugins" ]]; then
    PLUGINS_DIR="$dir/plugins"
    break
  fi
done < <(ls -dt "$AS_SUPPORT_DIR"/AndroidStudio[0-9]* 2>/dev/null)

if [[ -z "$PLUGINS_DIR" ]]; then
  while IFS= read -r dir; do
    if [[ -d "$dir/plugins" ]]; then
      PLUGINS_DIR="$dir/plugins"
      break
    fi
  done < <(ls -dt "$AS_SUPPORT_DIR"/AndroidStudio* 2>/dev/null)
fi

if [[ -z "$PLUGINS_DIR" ]]; then
  echo "  $SKIP No Android Studio plugins directory found — nothing to uninstall."
  exit 0
fi

# -----------------------------------------------------------------------------
# Remove plugin directory if present
# -----------------------------------------------------------------------------
PLUGIN_DIR="$PLUGINS_DIR/$PLUGIN_ID"

if [[ ! -d "$PLUGIN_DIR" ]]; then
  echo "  $SKIP Detekt plugin not found at:"
  echo "       $PLUGIN_DIR"
  echo "       Nothing to remove."
  exit 0
fi

echo "  $INFO Removing: $PLUGIN_DIR"
rm -rf "$PLUGIN_DIR"
echo "  $PASS Detekt plugin uninstalled."
echo ""
echo "  ⚠️  Restart Android Studio for the change to take effect."
echo ""
