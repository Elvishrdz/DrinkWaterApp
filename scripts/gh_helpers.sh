#!/usr/bin/env bash
# GitHub CLI helper functions shared across DrinkWaterApp scripts.
# Source this file; do not execute it directly.
#
# Usage:
#   SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
#   source "$SCRIPT_DIR/gh_helpers.sh"

# Print the GitHub Actions URL for this repository.
print_actions_url() {
  local repo
  repo=$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)
  if [[ -n "$repo" ]]; then
    echo "  https://github.com/$repo/actions"
  fi
}
