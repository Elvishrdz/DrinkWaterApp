#!/usr/bin/env bash
# Trigger the deployment workflow to the given track in Google Developer Console.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/gh_helpers.sh"

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
read -rp "Update Play Store listing? (y/n) [default: n]: " listing_choice
listing_choice=${listing_choice:-n}

UPDATE_LISTING="false"
if [[ "$listing_choice" =~ ^[Yy]$ ]]; then
  UPDATE_LISTING="true"
fi

echo ""
echo "Triggering release workflow → track: $TRACK, update_listing: $UPDATE_LISTING"
gh workflow run release.yml --field "track=$TRACK" --field "update_listing=$UPDATE_LISTING"

echo ""
echo "Workflow triggered. Monitor progress at:"
print_actions_url
