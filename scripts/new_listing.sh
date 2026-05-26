#!/usr/bin/env bash
# Scaffold a new Play Store listing locale under listing/.
#
# Mirrors the en-US folder structure exactly: text files, video URL files,
# and all graphics sub-folders. Files are created empty; graphics folders
# contain a .gitkeep so they are tracked by git until real assets are added.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LISTING_DIR="$(cd "$SCRIPT_DIR/.." && pwd)/listing"
TEMPLATE_LOCALE="en-US"

# ── Graphics sub-folders (mirrors update_listing.sh / upload_listing.py) ──────

GRAPHICS_FOLDERS=(
  "app-icon"
  "feature-graphics"
  "screenshots-phone"
  "screenshots-tablet-7-inch"
  "screenshots-tablet-10-inch"
  "screenshots-chromebook"
  "banner-tv"
  "screenshots-tv"
  "screenshots-xr"
)

# ── Text / video files ─────────────────────────────────────────────────────────

TEXT_FILES=(
  "app-name.txt"
  "short_description.txt"
  "full_description.txt"
)

VIDEO_FILES=(
  "video/video-url.txt"
  "video/spatial-xr-video-url.txt"
  "video/non-spatial-xr-video-url.txt"
)

# ── Prompt ─────────────────────────────────────────────────────────────────────

echo "=== New Play Store Listing Scaffold ==="
echo ""
echo "Existing locales:"
for d in "$LISTING_DIR"/*/; do
  echo "  • $(basename "$d")"
done
echo ""
read -rp "Enter new locale code (e.g. fr-FR, pt-BR, ja-JP): " LOCALE
LOCALE="${LOCALE// /}"   # strip accidental spaces

if [[ -z "$LOCALE" ]]; then
  echo "Error: locale cannot be empty."
  exit 1
fi

DEST="$LISTING_DIR/$LOCALE"

if [[ -d "$DEST" ]]; then
  echo "Error: '$LOCALE' already exists at $DEST"
  exit 1
fi

# ── Create structure ───────────────────────────────────────────────────────────

echo ""
echo "Creating listing/$LOCALE/ ..."
echo ""

# Text files
for f in "${TEXT_FILES[@]}"; do
  path="$DEST/$f"
  mkdir -p "$(dirname "$path")"
  touch "$path"
  echo "  + $f"
done

# Video URL files
for f in "${VIDEO_FILES[@]}"; do
  path="$DEST/$f"
  mkdir -p "$(dirname "$path")"
  touch "$path"
  echo "  + $f"
done

# Graphics folders
for folder in "${GRAPHICS_FOLDERS[@]}"; do
  dir="$DEST/graphics/$folder"
  mkdir -p "$dir"
  touch "$dir/.gitkeep"
  echo "  + graphics/$folder/"
done

# ── Summary ────────────────────────────────────────────────────────────────────

echo ""
echo "Done. Scaffold created at:"
echo "  listing/$LOCALE/"
echo ""
echo "Next steps:"
echo "  1. Add translated text to:"
echo "     • listing/$LOCALE/app-name.txt          (max 30 chars)"
echo "     • listing/$LOCALE/short_description.txt  (max 80 chars)"
echo "     • listing/$LOCALE/full_description.txt   (max 4000 chars)"
echo "  2. Drop screenshots and graphics into listing/$LOCALE/graphics/"
echo "     (remove the .gitkeep files when you add real assets)"
echo "  3. Run ./scripts/update_listing.sh to upload when ready."
