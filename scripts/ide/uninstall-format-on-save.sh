#!/usr/bin/env bash
# =============================================================================
# uninstall-format-on-save.sh
#
# Removes the FormatOnSaveOptions component from .idea/workspace.xml,
# disabling automatic Reformat code + Optimize imports on save.
#
# Uses Python's xml.etree.ElementTree (stdlib) — removes only the target
# element; everything else in workspace.xml is left untouched.
#
# Usage:
#   bash scripts/ide/uninstall-format-on-save.sh
#
# Idempotent: safe to run even if format-on-save was never installed.
# =============================================================================

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORKSPACE="$ROOT/.idea/workspace.xml"

PASS="✅"
SKIP="↩️ "
FAIL="❌"

if [[ ! -f "$WORKSPACE" ]]; then
  echo "  $SKIP workspace.xml not found — nothing to uninstall."
  exit 0
fi

python3 - "$WORKSPACE" <<'PYEOF'
import sys
import xml.etree.ElementTree as ET

path = sys.argv[1]

tree = ET.parse(path)
root = tree.getroot()

COMPONENT = "FormatOnSaveOptions"

existing = root.find(f".//component[@name='{COMPONENT}']")
if existing is None:
    print(f"  ↩️  Format-on-save not configured — nothing to remove.")
    sys.exit(0)

root.remove(existing)

try:
    ET.indent(tree, space="  ")
except AttributeError:
    pass

tree.write(path, encoding="unicode", xml_declaration=False)
print(f"  ✅ Format-on-save removed from workspace.xml.")
PYEOF

echo ""
echo "  ⚠️  Restart Android Studio for the change to take effect."
echo ""
