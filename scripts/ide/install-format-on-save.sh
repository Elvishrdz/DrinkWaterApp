#!/usr/bin/env bash
# =============================================================================
# install-format-on-save.sh
#
# Enables "Reformat code" (⌥⌘L) and "Optimize imports" (⌃⌥O) on every save
# in Android Studio / IntelliJ by injecting the FormatOnSaveOptions component
# into .idea/workspace.xml.
#
# Uses Python's xml.etree.ElementTree (stdlib) — position-independent and safe
# regardless of how the IDE reorganises workspace.xml over time.
#
# Usage:
#   bash scripts/ide/install-format-on-save.sh
#
# Idempotent: running more than once has no effect after the first install.
# =============================================================================

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORKSPACE="$ROOT/.idea/workspace.xml"

PASS="✅"
SKIP="↩️ "
FAIL="❌"

# -----------------------------------------------------------------------------
# Guard: .idea must exist (opened in IDE at least once)
# -----------------------------------------------------------------------------
if [[ ! -d "$ROOT/.idea" ]]; then
  echo "  $FAIL .idea/ directory not found."
  echo "     Open the project in Android Studio once, then re-run this script."
  exit 1
fi

# -----------------------------------------------------------------------------
# Create a minimal workspace.xml if it doesn't exist yet
# -----------------------------------------------------------------------------
if [[ ! -f "$WORKSPACE" ]]; then
  echo "  workspace.xml not found — creating a minimal one..."
  cat > "$WORKSPACE" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
</project>
EOF
fi

# -----------------------------------------------------------------------------
# Inject FormatOnSaveOptions via Python XML parser
# -----------------------------------------------------------------------------
python3 - "$WORKSPACE" <<'PYEOF'
import sys
import xml.etree.ElementTree as ET

path = sys.argv[1]
ET.register_namespace("", "")

tree = ET.parse(path)
root = tree.getroot()

COMPONENT = "FormatOnSaveOptions"

existing = root.find(f".//component[@name='{COMPONENT}']")
if existing is not None:
    print(f"  ↩️  Format-on-save already configured — nothing to do.")
    sys.exit(0)

component = ET.SubElement(root, "component")
component.set("name", COMPONENT)

options = [
    ("myRunOnSaveEnabled",       "true"),
    ("myFormatOnlyChangedLines", "false"),
    ("myOptimizeImports",        "true"),
]
for name, value in options:
    opt = ET.SubElement(component, "option")
    opt.set("name", name)
    opt.set("value", value)

# Indent the new element (Python 3.9+ has ET.indent; fallback for older)
try:
    ET.indent(tree, space="  ")
except AttributeError:
    pass

tree.write(path, encoding="unicode", xml_declaration=False)
print(f"  ✅ Format-on-save enabled (Reformat code + Optimize imports on every ⌘S).")
PYEOF

echo ""
echo "  ⚠️  Restart Android Studio for the change to take effect."
echo ""
