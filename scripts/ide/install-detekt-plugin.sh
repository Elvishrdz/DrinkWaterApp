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

# Numeric plugin ID from JetBrains Marketplace (plugins.jetbrains.com/plugin/10761-detekt)
PLUGIN_NUMERIC_ID="10761"
# Folder name the zip extracts to (as shipped by JetBrains)
PLUGIN_DIR_NAME="Detekt IntelliJ Plugin"

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

# Pick the newest non-Preview AndroidStudio* directory, then Preview as fallback
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
  echo "  $FAIL Could not find an Android Studio plugins directory under:"
  echo "     $AS_SUPPORT_DIR"
  exit 1
fi

echo "  $INFO Target plugins directory:"
echo "       $PLUGINS_DIR"

# -----------------------------------------------------------------------------
# Idempotency check — already installed?
# -----------------------------------------------------------------------------
PLUGIN_INSTALL_DIR="$PLUGINS_DIR/$PLUGIN_DIR_NAME"
if [[ -d "$PLUGIN_INSTALL_DIR" ]]; then
  echo "  $SKIP Detekt plugin already installed at:"
  echo "       $PLUGIN_INSTALL_DIR"
  echo "       To reinstall, run: bash scripts/ide/uninstall-detekt-plugin.sh"
  exit 0
fi

# -----------------------------------------------------------------------------
# Resolve the latest stable release via JetBrains Marketplace JSON API
# The pluginManager download endpoint requires a matching build number, which
# varies per IDE version. Using the /api/plugins/{id}/updates endpoint instead
# gives us a direct file URL that always works.
# -----------------------------------------------------------------------------
echo ""
echo "  Resolving latest stable release from JetBrains Marketplace..."

DOWNLOAD_URL=$(python3 - <<PYEOF
import urllib.request, json, sys

url = "https://plugins.jetbrains.com/api/plugins/${PLUGIN_NUMERIC_ID}/updates?size=1"
try:
    with urllib.request.urlopen(url, timeout=15) as r:
        updates = json.load(r)
except Exception as e:
    print(f"ERROR: {e}", file=sys.stderr)
    sys.exit(1)

if not updates:
    print("ERROR: no updates returned", file=sys.stderr)
    sys.exit(1)

file_path = updates[0].get("file", "")
if not file_path:
    print("ERROR: no file path in update", file=sys.stderr)
    sys.exit(1)

print(f"https://plugins.jetbrains.com/files/{file_path}")
PYEOF
)

if [[ -z "$DOWNLOAD_URL" ]]; then
  echo "  $FAIL Could not resolve download URL from JetBrains Marketplace."
  echo "     Install manually: Android Studio → Settings → Plugins → search 'Detekt'"
  exit 1
fi

echo "  $INFO Download URL: $DOWNLOAD_URL"

# -----------------------------------------------------------------------------
# Download plugin zip to a temp directory
# -----------------------------------------------------------------------------
TMP_DIR=$(mktemp -d)
ZIP_NAME=$(basename "$DOWNLOAD_URL")
ZIP_PATH="$TMP_DIR/$ZIP_NAME"

echo "  Downloading $ZIP_NAME..."
if ! curl --silent --show-error --location --output "$ZIP_PATH" "$DOWNLOAD_URL"; then
  echo "  $FAIL Download failed. Check your internet connection and try again."
  rm -rf "$TMP_DIR"
  exit 1
fi

# Verify the download is a valid zip
if ! python3 -c "import zipfile; zipfile.ZipFile('$ZIP_PATH')" 2>/dev/null; then
  echo "  $FAIL Downloaded file is not a valid zip archive."
  rm -rf "$TMP_DIR"
  exit 1
fi

# -----------------------------------------------------------------------------
# Extract into plugins directory
# -----------------------------------------------------------------------------
echo "  Installing..."
unzip -q "$ZIP_PATH" -d "$PLUGINS_DIR"
rm -rf "$TMP_DIR"

if [[ -d "$PLUGIN_INSTALL_DIR" ]]; then
  echo "  $PASS Detekt plugin installed at:"
  echo "       $PLUGIN_INSTALL_DIR"
else
  # Zip may extract to a slightly different folder name — still succeeded
  echo "  $PASS Detekt plugin extracted into $PLUGINS_DIR"
fi

echo ""
echo "  ⚠️  Restart Android Studio to activate the plugin."
echo "     Once active, Detekt violations will appear as inline underlines"
echo "     using your project's detekt.yml rules."
echo ""
