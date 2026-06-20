#!/usr/bin/env bash
# Installs the "update-legal" Claude Code skill into the project.
#
# Source of record:   legal/agents/skills/update-legal/
# Install target:      .claude/skills/update-legal/   (where Claude Code discovers skills)
#
# Run from anywhere: ./legal/scripts/install-legal-skill.sh

set -euo pipefail

# - Paths
SCRIPTS_DIR="$(dirname "${BASH_SOURCE[0]}")"
# 2>/dev/null suppresses the stale-CWD warning bash can emit here.
PROJECT_ROOT="$(git -C "$SCRIPTS_DIR" rev-parse --show-toplevel)" 2>/dev/null

SKILL_NAME="update-legal"
SRC_DIR="$PROJECT_ROOT/legal/agents/skills/$SKILL_NAME"
DEST_DIR="$PROJECT_ROOT/.claude/skills/$SKILL_NAME"

# - Validation
[[ -f "$SRC_DIR/SKILL.md" ]] || {
  echo "Error: skill source not found at $SRC_DIR/SKILL.md" >&2
  exit 1
}

# - Install (copy source -> discovery location, overwriting any previous install)
echo "Installing '$SKILL_NAME' skill..."
echo "  from: $SRC_DIR"
echo "  to:   $DEST_DIR"

mkdir -p "$DEST_DIR"
# Trailing slash + '.' copies the directory CONTENTS (incl. any assets next to SKILL.md).
cp -R "$SRC_DIR/." "$DEST_DIR/"

echo "Done. Restart Claude Code (or start a new session) so it picks up the skill."
echo "Use it by saying \"update the legal\" / \"/update-legal\"."
