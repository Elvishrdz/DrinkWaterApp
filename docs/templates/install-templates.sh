#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# install-templates.sh
# Copies DrinkWaterApp.xml Live Templates into every Android Studio
# installation found on this machine so they are ready without any manual
# import step.
#
# Usage (run from project root):
#   bash docs/templates/install-templates.sh          # installs to all versions
#   bash docs/templates/install-templates.sh --latest # installs only to the
#                                                      # most recent stable version
#
# After running: restart Android Studio (or File → Invalidate Caches).
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

# ── Resolve paths ─────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_FILE="$SCRIPT_DIR/DrinkWaterApp.xml"

if [[ ! -f "$TEMPLATE_FILE" ]]; then
  echo "✗  DrinkWaterApp.xml not found at: $TEMPLATE_FILE"
  exit 1
fi

# ── Detect OS and config root ─────────────────────────────────────────────────
case "$(uname -s)" in
  Darwin)
    CONFIG_ROOT="$HOME/Library/Application Support/Google"
    ;;
  Linux)
    CONFIG_ROOT="${XDG_CONFIG_HOME:-$HOME/.config}/Google"
    ;;
  MINGW*|MSYS*|CYGWIN*)
    CONFIG_ROOT="${APPDATA}/Google"
    ;;
  *)
    echo "✗  Unsupported OS: $(uname -s)"
    exit 1
    ;;
esac

if [[ ! -d "$CONFIG_ROOT" ]]; then
  echo "✗  Android Studio config directory not found: $CONFIG_ROOT"
  echo "   Is Android Studio installed?"
  exit 1
fi

# ── Collect matching Studio directories ───────────────────────────────────────
LATEST_ONLY=false
if [[ "${1:-}" == "--latest" ]]; then
  LATEST_ONLY=true
fi

# Find all stable AndroidStudio* dirs (exclude Preview builds), sorted by version
ALL_VERSIONS=()
while IFS= read -r line; do
  ALL_VERSIONS+=("$line")
done < <(
  ls "$CONFIG_ROOT" 2>/dev/null \
    | grep -E '^AndroidStudio[0-9]' \
    | grep -v -i 'preview' \
    | sort -t. -k1,1V -k2,2n -k3,3n 2>/dev/null \
    || ls "$CONFIG_ROOT" 2>/dev/null \
         | grep -E '^AndroidStudio[0-9]' \
         | grep -v -i 'preview' \
         | sort
)

if [[ ${#ALL_VERSIONS[@]} -eq 0 ]]; then
  echo "✗  No Android Studio installations found in: $CONFIG_ROOT"
  exit 1
fi

if $LATEST_ONLY; then
  LATEST="${ALL_VERSIONS[${#ALL_VERSIONS[@]}-1]}"
  TARGETS=("$LATEST")
else
  TARGETS=("${ALL_VERSIONS[@]}")
fi

# ── Install ───────────────────────────────────────────────────────────────────
echo ""
echo "Installing DrinkWaterApp Live Templates..."
echo ""

INSTALLED=0
for VERSION_DIR in "${TARGETS[@]}"; do
  TEMPLATES_DIR="$CONFIG_ROOT/$VERSION_DIR/templates"
  DEST="$TEMPLATES_DIR/DrinkWaterApp.xml"

  mkdir -p "$TEMPLATES_DIR"
  cp "$TEMPLATE_FILE" "$DEST"

  echo "  ✓  $VERSION_DIR"
  INSTALLED=$((INSTALLED + 1))
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Done — $INSTALLED installation(s) updated."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  Restart Android Studio to activate the templates."
echo "  (or: File → Invalidate Caches → Just Restart)"
echo ""
echo "  Then type a prefix + Tab in any .kt file:"
echo "    dwcomp   →  @Composable function"
echo "    dwroot   →  Feature Root"
echo "    dwstate  →  Feature State sealed interface"
echo "    dwvm     →  ViewModel skeleton"
echo "    dwtest   →  @Test method (AAA + backtick name)"
echo "    (see docs/templates/README.md for the full list)"
echo ""
