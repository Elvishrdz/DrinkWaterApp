#!/usr/bin/env bash
# =============================================================================
# install-detekt-plugin.sh
#
# Downloads and installs the Detekt IntelliJ plugin for Android Studio
# (macOS only). The plugin highlights Detekt violations inline as you type,
# using the same detekt.yml that the pre-commit hook enforces.
#
# No IDE interaction required — plugins are zip files extracted into the
# IDE's plugins directory. The plugin is active on the next IDE launch.
#
# Usage:
#   bash scripts/ide/install-detekt-plugin.sh
#
# Idempotent: exits early if the plugin is already installed.
# Requires: macOS, python3 (stdlib), curl
# =============================================================================

set -euo pipefail

PASS="✅"
SKIP="↩️ "
FAIL="❌"
INFO="ℹ️ "

PLUGIN_ID="detekt-intellij-plugin"
PLUGIN_MARKETPLACE_ID="10802"

# -----------------------------------------------------------------------------
# macOS guard
# -----------------------------------------------------------------------------
if [[ "$(uname)" != "Darwin" ]]; then
  echo "  $FAIL This script supports macOS only."
  echo "     On Linux/Windows, install the Detekt plugin manually via:"
  echo "     Android Studio → Settings → Plugins → search 'Detekt'"
  exit 1
fi

# -----------------------------------------------------------------------------
# Locate the newest Android Studio plugins directory
# -----------------------------------------------------------------------------
AS_SUPPORT_DIR="$HOME/Library/Application Support/Google"

if [[ ! -d "$AS_SUPPORT_DIR" ]]; then
  echo "  $FAIL Android Studio support directory not found at:"
  echo "     $AS_SUPPORT_DIR"
  echo "     Open Android Studio at least once, then re-run this script."
  exit 1
fi

# Pick the newest AndroidStudio* directory (non-Preview, then Preview as fallback)
PLUGINS_DIR=""
while IFS= read -r dir; do
  if [[ -d "$dir/plugins" ]]; then
    PLUGINS_DIR="$dir/plugins"
    break
  fi
done < <(ls -dt "$AS_SUPPORT_DIR"/AndroidStudio[0-9]* 2>/dev/null)

# Fallback: include Preview builds
if [[ -z "$PLUGINS_DIR" ]]; then
  while IFS= read -r dir; do
    if [[ -d "$dir/plugins" ]]; then
      PLUGINS_DIR="$dir/plugins"
      break
    fi
  done < <(ls -dt "$AS_SUPPORT_DIR"/AndroidStudio* 2>/dev/null)
fi

if [[ -z "$PLUGINS_DIR" ]]; then
  echo "  $FAIL Could not find an Android Studio plugins directory under:"
  echo "     $AS_SUPPORT_DIR"
  exit 1
fi

echo "  $INFO Target plugins directory:"
echo "       $PLUGINS_DIR"

# -----------------------------------------------------------------------------
# Idempotency check — already installed?
# -----------------------------------------------------------------------------
PLUGIN_DIR="$PLUGINS_DIR/$PLUGIN_ID"
if [[ -d "$PLUGIN_DIR" ]]; then
  echo "  $SKIP Detekt plugin already installed at:"
  echo "       $PLUGIN_DIR"
  echo "       To reinstall, run: bash scripts/ide/uninstall-detekt-plugin.sh"
  exit 0
fi

# -----------------------------------------------------------------------------
# Get Android Studio build number for Marketplace API compatibility check
# -----------------------------------------------------------------------------
AS_APP=$(mdfind "kMDItemCFBundleIdentifier == 'com.google.android.studio'" 2>/dev/null | head -1)
BUILD_NUMBER=""

if [[ -n "$AS_APP" && -f "$AS_APP/Contents/Resources/product-info.json" ]]; then
  BUILD_NUMBER=$(python3 -c "
import json, sys
with open('$AS_APP/Contents/Resources/product-info.json') as f:
    d = json.load(f)
print(d.get('buildNumber', ''))
" 2>/dev/null || true)
fi

# -----------------------------------------------------------------------------
# Resolve download URL via JetBrains Marketplace API
# -----------------------------------------------------------------------------
echo ""
echo "  Resolving plugin download URL from JetBrains Marketplace..."

if [[ -n "$BUILD_NUMBER" ]]; then
  DOWNLOAD_URL="https://plugins.jetbrains.com/pluginManager?action=download&id=${PLUGIN_ID}&build=AI-${BUILD_NUMBER}"
  echo "  $INFO Build number: $BUILD_NUMBER"
else
  # Fallback: latest stable, no build pin
  DOWNLOAD_URL="https://plugins.jetbrains.com/pluginManager?action=download&id=${PLUGIN_ID}"
  echo "  $INFO Build number not found — downloading latest stable release"
fi

# -----------------------------------------------------------------------------
# Download plugin zip to a temp directory
# -----------------------------------------------------------------------------
TMP_DIR=$(mktemp -d)
ZIP_PATH="$TMP_DIR/${PLUGIN_ID}.zip"

echo "  Downloading..."
if ! curl --silent --show-error --location --output "$ZIP_PATH" "$DOWNLOAD_URL"; then
  echo "  $FAIL Download failed. Check your internet connection and try again."
  rm -rf "$TMP_DIR"
  exit 1
fi

# Verify the download looks like a zip
if ! python3 -c "
import zipfile, sys
try:
    zipfile.ZipFile('$ZIP_PATH')
except Exception as e:
    print(f'Not a valid zip: {e}')
    sys.exit(1)
" 2>/dev/null; then
  echo "  $FAIL Downloaded file is not a valid zip archive."
  echo "     The Marketplace may have returned an error page."
  echo "     Install manually: Android Studio → Settings → Plugins → search 'Detekt'"
  rm -rf "$TMP_DIR"
  exit 1
fi

# -----------------------------------------------------------------------------
# Extract into plugins directory
# -----------------------------------------------------------------------------
echo "  Installing..."
unzip -q "$ZIP_PATH" -d "$PLUGINS_DIR"
rm -rf "$TMP_DIR"

# Verify extraction created the expected directory
if [[ ! -d "$PLUGIN_DIR" ]]; then
  # The zip might extract to a different folder name — find what was created
  echo "  $PASS Plugin extracted (folder name may differ from expected)."
else
  echo "  $PASS Detekt plugin installed at:"
  echo "       $PLUGIN_DIR"
fi

echo ""
echo "  ⚠️  Restart Android Studio to activate the plugin."
echo "     Once active, Detekt violations will appear as inline underlines"
echo "     using your project's detekt.yml rules."
echo ""
