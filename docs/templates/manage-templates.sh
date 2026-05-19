#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# manage-templates.sh
# List, inspect, and delete Live Templates from DrinkWaterApp.xml.
# Any deletion is automatically propagated to all installed Android Studio
# versions by re-running install-templates.sh.
#
# Usage (run from project root):
#   bash docs/templates/manage-templates.sh                      # list all
#   bash docs/templates/manage-templates.sh list                 # same
#   bash docs/templates/manage-templates.sh delete               # interactive
#   bash docs/templates/manage-templates.sh delete dwcomp        # by name
#   bash docs/templates/manage-templates.sh delete dwcomp dwprev # multiple
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_FILE="$SCRIPT_DIR/DrinkWaterApp.xml"
INSTALL_SCRIPT="$SCRIPT_DIR/install-templates.sh"

# ── Guards ────────────────────────────────────────────────────────────────────
if [[ ! -f "$TEMPLATE_FILE" ]]; then
  echo "✗  DrinkWaterApp.xml not found at: $TEMPLATE_FILE"
  exit 1
fi
if ! command -v python3 &>/dev/null; then
  echo "✗  python3 is required but not found."
  exit 1
fi

CMD="${1:-list}"

# ─────────────────────────────────────────────────────────────────────────────
# list_templates — print a numbered table of all templates
# ─────────────────────────────────────────────────────────────────────────────
list_templates() {
  TEMPLATE_FILE="$TEMPLATE_FILE" python3 - << 'PYEOF'
import os, sys
import xml.etree.ElementTree as ET

xml_path = os.environ['TEMPLATE_FILE']
try:
    templates = ET.parse(xml_path).getroot().findall('template')
except Exception as e:
    print(f"✗  Could not parse XML: {e}", file=sys.stderr)
    sys.exit(1)

if not templates:
    print("\n  (no templates found)\n")
    sys.exit(0)

W = 20
print()
print(f"  {'#':<4} {'Abbreviation':<{W}} Description")
print(f"  {'─'*4} {'─'*W} {'─'*48}")
for i, t in enumerate(templates, 1):
    name = t.get('name', '?')
    desc = t.get('description', '')
    print(f"  {i:<4} {name:<{W}} {desc}")
print()
print(f"  {len(templates)} template(s) in DrinkWaterApp.xml")
print()
PYEOF
}

# ─────────────────────────────────────────────────────────────────────────────
# remove_from_xml  <abbr1> [abbr2 ...]
# Removes <template> blocks — and their directly preceding comments — from
# DrinkWaterApp.xml.  Prints "REMOVED:<abbr>" or "NOTFOUND:<abbr>" per entry.
#
# Key regex detail:
#   XML comments cannot legally contain "--". We exploit this to write a comment
#   pattern that is guaranteed NOT to cross comment boundaries:
#     <!--(?:[^-]|-(?!-))*-->
#   [^-]       → any char except "-"  (matches newlines — char classes do)
#   -(?!-)     → a "-" NOT followed by another "-"
#   Combined: matches any content that never produces "--", then -->
#   This stops at the FIRST "-->" it finds, unlike <!--.*?--> with DOTALL
#   which can stretch across multiple template blocks.
# ─────────────────────────────────────────────────────────────────────────────
remove_from_xml() {
  TEMPLATE_FILE="$TEMPLATE_FILE" ABBRS="$*" python3 - << 'PYEOF'
import os, re

xml_path = os.environ['TEMPLATE_FILE']
abbrs    = os.environ['ABBRS'].split()

with open(xml_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Safe XML comment: stops at the first --> it encounters (never crosses boundaries)
COMMENT = r'<!--(?:[^-]|-(?!-))*-->'

for abbr in abbrs:
    escaped = re.escape(abbr)

    # Pattern 1: preceding section comment + template block
    #   \n                  — newline before the comment
    #   [ \t]*COMMENT       — indented comment (e.g.  <!-- ── TYPE | abbr ── -->)
    #   \n+                 — newlines between comment and template
    #   [ \t]*<template ... name="abbr" ... >...</template>
    pat_commented = (
        r'\n[ \t]*' + COMMENT +
        r'\n+[ \t]*<template\s+name="' + escaped + r'".*?</template>'
    )
    new_content = re.sub(pat_commented, '', content, flags=re.DOTALL)

    if new_content == content:
        # Pattern 2: no comment (manually added template without section header)
        pat_plain = r'\n+[ \t]*<template\s+name="' + escaped + r'".*?</template>'
        new_content = re.sub(pat_plain, '', content, flags=re.DOTALL)

    if new_content == content:
        print(f"NOTFOUND:{abbr}")
    else:
        content = new_content
        print(f"REMOVED:{abbr}")

# Collapse any triple-newlines left after removal (keep at most one blank line)
content = re.sub(r'\n{3,}', '\n\n', content)

with open(xml_path, 'w', encoding='utf-8') as f:
    f.write(content)
PYEOF
}

# ─────────────────────────────────────────────────────────────────────────────
# resolve_selection <raw-input>
# Maps "1 3 dwprev" → abbreviation names.
# User-visible warnings go to stderr; resolved names go to stdout.
# ─────────────────────────────────────────────────────────────────────────────
resolve_selection() {
  TEMPLATE_FILE="$TEMPLATE_FILE" SELECTION="$1" python3 - << 'PYEOF'
import os, sys
import xml.etree.ElementTree as ET

xml_path  = os.environ['TEMPLATE_FILE']
selection = os.environ['SELECTION'].split()
names     = [t.get('name', '') for t in ET.parse(xml_path).getroot().findall('template')]

resolved = []
for s in selection:
    if s.isdigit():
        idx = int(s) - 1
        if 0 <= idx < len(names):
            resolved.append(names[idx])
        else:
            print(f"  ⚠  Index {s} out of range (1–{len(names)})", file=sys.stderr)
    elif s in names:
        resolved.append(s)
    else:
        print(f"  ⚠  '{s}' not found — skipping", file=sys.stderr)

print(' '.join(resolved))
PYEOF
}

# ─────────────────────────────────────────────────────────────────────────────
# interactive_delete
# Shows the list + prompt on stderr (/dev/tty), outputs resolved names on stdout
# so the caller can capture them cleanly with $().
# ─────────────────────────────────────────────────────────────────────────────
interactive_delete() {
  # Print the numbered list to stderr so it appears in the terminal even when
  # stdout is captured by the caller.
  TEMPLATE_FILE="$TEMPLATE_FILE" python3 - >&2 << 'PYEOF'
import os
import xml.etree.ElementTree as ET

templates = ET.parse(os.environ['TEMPLATE_FILE']).getroot().findall('template')
W = 20
print()
print("  Select templates to delete.")
print("  Enter index numbers, abbreviation names, or a mix — space-separated.")
print()
print(f"  {'#':<4} {'Abbreviation':<{W}} Description")
print(f"  {'─'*4} {'─'*W} {'─'*48}")
for i, t in enumerate(templates, 1):
    print(f"  {i:<4} {t.get('name','?'):<{W}} {t.get('description','')}")
print()
PYEOF

  printf "  Selection (or Enter to cancel): " >&2
  read -r SELECTION < /dev/tty

  if [[ -z "$SELECTION" || "$SELECTION" == "q" || "$SELECTION" == "Q" ]]; then
    echo "  Cancelled." >&2
    echo ""   # empty stdout → caller sees no names, exits cleanly
    return 0
  fi

  resolve_selection "$SELECTION"
}

# ─────────────────────────────────────────────────────────────────────────────
# do_delete  <abbr1> [abbr2 ...]
# Calls remove_from_xml, prints results, reinstalls if anything changed.
# ─────────────────────────────────────────────────────────────────────────────
do_delete() {
  if [[ $# -eq 0 ]]; then
    echo "  Nothing to delete."
    return 0
  fi

  echo ""
  local any_removed=false

  while IFS= read -r line; do
    if   [[ "$line" == REMOVED:*  ]]; then
      echo "  ✓  Deleted:   ${line#REMOVED:}"
      any_removed=true
    elif [[ "$line" == NOTFOUND:* ]]; then
      echo "  ✗  Not found: ${line#NOTFOUND:}"
    fi
  done < <(remove_from_xml "$@")

  if $any_removed; then
    echo ""
    echo "  Propagating changes to Android Studio..."
    bash "$INSTALL_SCRIPT"
    echo ""
    echo "  Remaining templates:"
    list_templates
  else
    echo ""
  fi
}

# ─────────────────────────────────────────────────────────────────────────────
# Main dispatch
# ─────────────────────────────────────────────────────────────────────────────
case "$CMD" in
  list)
    list_templates
    ;;

  delete)
    if [[ $# -gt 1 ]]; then
      # Direct: manage-templates.sh delete dwcomp dwprev
      do_delete "${@:2}"
    else
      # Interactive: show list, read selection, delete
      NAMES="$(interactive_delete)"
      if [[ -n "$NAMES" ]]; then
        do_delete $NAMES
      fi
    fi
    ;;

  *)
    echo ""
    echo "Usage:"
    echo "  bash docs/templates/manage-templates.sh              # list all"
    echo "  bash docs/templates/manage-templates.sh list         # same"
    echo "  bash docs/templates/manage-templates.sh delete       # interactive"
    echo "  bash docs/templates/manage-templates.sh delete abbr  # by name"
    echo ""
    ;;
esac
