#!/bin/bash
# uninstall_git_hooks.sh
# Removes every symlink in .git/hooks/ that points into scripts/git/hooks/.
# Run to detach the project hooks without touching the source files.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HOOKS_SOURCE="$REPO_ROOT/scripts/git/hooks"
HOOKS_TARGET="$REPO_ROOT/.git/hooks"

if [ ! -d "$HOOKS_TARGET" ]; then
  echo "ERROR: .git/hooks directory not found. Is this a git repository?"
  exit 1
fi

echo "Removing git hooks installed from $HOOKS_SOURCE ..."
echo ""

removed=0

for hook_file in "$HOOKS_SOURCE"/*; do
  hook_name="$(basename "$hook_file")"
  target="$HOOKS_TARGET/$hook_name"

  if [ -L "$target" ] && [ "$(readlink "$target")" = "$hook_file" ]; then
    rm "$target"
    echo "  Removed: .git/hooks/$hook_name"
    removed=$((removed + 1))
  else
    echo "  Skipped: .git/hooks/$hook_name (not a managed symlink)"
  fi
done

echo ""
if [ "$removed" -eq 0 ]; then
  echo "No managed hooks found. Nothing was removed."
else
  echo "Done. $removed hook(s) removed. Git hooks are no longer active."
fi
echo ""
