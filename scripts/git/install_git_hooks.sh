#!/bin/bash
# install_git_hooks.sh
# Symlinks every hook in scripts/git/hooks/ into .git/hooks/.
# Run once after cloning: bash scripts/git/install_git_hooks.sh

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HOOKS_SOURCE="$REPO_ROOT/scripts/git/hooks"
HOOKS_TARGET="$REPO_ROOT/.git/hooks"

if [ ! -d "$HOOKS_SOURCE" ]; then
  echo "ERROR: hooks directory not found at $HOOKS_SOURCE"
  exit 1
fi

if [ ! -d "$HOOKS_TARGET" ]; then
  echo "ERROR: .git/hooks directory not found. Is this a git repository?"
  exit 1
fi

echo "Installing git hooks from $HOOKS_SOURCE ..."
echo ""

for hook_file in "$HOOKS_SOURCE"/*; do
  hook_name="$(basename "$hook_file")"
  target="$HOOKS_TARGET/$hook_name"

  # Remove stale symlink or file if it already exists.
  if [ -L "$target" ] || [ -f "$target" ]; then
    rm "$target"
  fi

  ln -s "$hook_file" "$target"
  chmod +x "$hook_file"
  echo "  Linked: .git/hooks/$hook_name -> $hook_file"
done

echo ""
echo "Done. Git hooks are active."
echo ""
echo "Tip: skip Detekt on a single commit with  SKIP_DETEKT=1 git commit ..."
