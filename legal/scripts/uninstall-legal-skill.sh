#!/usr/bin/env bash
# Uninstalls the "update-legal" Claude Code skill from the project.
#
# Removes the installed copy at .claude/skills/update-legal/. The source of record at
# legal/agents/skills/update-legal/ is left untouched, so you can re-install at any time with
# ./legal/scripts/install-legal-skill.sh
#
# Run from anywhere: ./legal/scripts/uninstall-legal-skill.sh

set -euo pipefail

# - Paths
SCRIPTS_DIR="$(dirname "${BASH_SOURCE[0]}")"
# 2>/dev/null suppresses the stale-CWD warning bash can emit here.
PROJECT_ROOT="$(git -C "$SCRIPTS_DIR" rev-parse --show-toplevel)" 2>/dev/null

SKILL_NAME="update-legal"
DEST_DIR="$PROJECT_ROOT/.claude/skills/$SKILL_NAME"

# - Uninstall
if [[ -d "$DEST_DIR" ]]; then
  echo "Removing installed skill at $DEST_DIR ..."
  rm -rf "$DEST_DIR"
  echo "Done. Restart Claude Code (or start a new session) so it stops offering the skill."
  echo "Source of record kept at legal/agents/skills/$SKILL_NAME (re-install anytime)."
else
  echo "Nothing to do: '$SKILL_NAME' is not installed at $DEST_DIR."
fi
