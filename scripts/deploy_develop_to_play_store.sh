#!/usr/bin/env bash
set -euo pipefail

TRACKS=("internal" "alpha" "beta" "production")

echo "Select deployment track:"
for i in "${!TRACKS[@]}"; do
  echo "  $((i + 1))) ${TRACKS[$i]}"
done

read -rp "Enter number [1-4] (default: 1 = internal): " choice
choice=${choice:-1}

if ! [[ "$choice" =~ ^[1-4]$ ]]; then
  echo "Invalid selection. Aborting."
  exit 1
fi

TRACK="${TRACKS[$((choice - 1))]}"

echo ""
echo "Triggering release workflow → track: $TRACK"
gh workflow run release.yml --field "track=$TRACK"

echo ""
echo "Workflow triggered. Monitor progress at:"
echo "  https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions"
