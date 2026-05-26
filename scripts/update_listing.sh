#!/usr/bin/env bash
# Trigger the Play Store listing update workflow (listing.yml).
#
# Scans the listing/ directory, shows a preview of what will be uploaded
# per locale, then prompts for upload mode and locale before triggering
# the GitHub Actions workflow.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/gh_helpers.sh"

LISTING_DIR="$(cd "$SCRIPT_DIR/.." && pwd)/listing"
MODES=("all" "text" "images")

# Canonical graphics sub-folder names (mirrors upload_listing.py)
IMAGE_FOLDERS=(
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

# ── Helpers ────────────────────────────────────────────────────────────────────

# Returns 0 (true) if the file exists and is not empty.
file_nonempty() { [[ -f "$1" ]] && [[ -s "$1" ]]; }

# Prints the number of image files in a directory (png/jpg/jpeg).
count_images() {
  find "$1" -maxdepth 1 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) \
    2>/dev/null | wc -l | tr -d '[:space:]'
}

# ── Scan listing/ ──────────────────────────────────────────────────────────────

if [[ ! -d "$LISTING_DIR" ]]; then
  echo "Error: listing/ directory not found at $LISTING_DIR"
  exit 1
fi

LOCALES=()
while IFS= read -r dir; do
  LOCALES+=("$(basename "$dir")")
done < <(find "$LISTING_DIR" -mindepth 1 -maxdepth 1 -type d | sort)

if [[ ${#LOCALES[@]} -eq 0 ]]; then
  echo "Error: No locale directories found in listing/"
  exit 1
fi

echo "=== Play Store Listing Preview ==="
echo ""

for locale in "${LOCALES[@]}"; do
  locale_dir="$LISTING_DIR/$locale"
  text_count=0
  image_count=0

  echo "  [$locale]"

  # ── Text files ──────────────────────────────────────────────────────────────

  # app-name.txt is canonical; title.txt is the legacy fallback.
  name_file="$locale_dir/app-name.txt"
  [[ ! -f "$name_file" ]] && name_file="$locale_dir/title.txt"
  if file_nonempty "$name_file"; then
    echo "    + app-name"
    text_count=$(( text_count + 1 ))
  fi

  for tf in "short_description.txt" "full_description.txt"; do
    if file_nonempty "$locale_dir/$tf"; then
      echo "    + ${tf%.txt}"
      text_count=$(( text_count + 1 ))
    fi
  done

  # ── Video URLs ──────────────────────────────────────────────────────────────

  video_dir="$locale_dir/video"
  if [[ -d "$video_dir" ]]; then
    for vf in "video-url.txt" "spatial-xr-video-url.txt" "non-spatial-xr-video-url.txt"; do
      if file_nonempty "$video_dir/$vf"; then
        echo "    + video/$vf"
        text_count=$(( text_count + 1 ))
      fi
    done
  fi

  # ── Image folders ───────────────────────────────────────────────────────────

  # graphics/ is canonical; images/ is the legacy fallback.
  images_dir="$locale_dir/graphics"
  [[ ! -d "$images_dir" ]] && images_dir="$locale_dir/images"

  if [[ -d "$images_dir" ]]; then
    for folder in "${IMAGE_FOLDERS[@]}"; do
      folder_path="$images_dir/$folder"
      if [[ -d "$folder_path" ]]; then
        n=$(count_images "$folder_path")
        if [[ "$n" -gt 0 ]]; then
          echo "    + $folder ($n image(s))"
          image_count=$(( image_count + 1 ))
        else
          echo "    - $folder (empty — skipped)"
        fi
      fi
    done
  fi

  if [[ $text_count -eq 0 && $image_count -eq 0 ]]; then
    echo "    (nothing to upload)"
  else
    echo "    → $text_count text field(s), $image_count image folder(s) ready"
  fi
  echo ""
done

# ── Mode selection ─────────────────────────────────────────────────────────────

echo "Select upload mode:"
echo "  1) all    — text + images"
echo "  2) text   — titles, descriptions, video URLs"
echo "  3) images — icons, screenshots, banners"
echo ""
read -rp "Enter number [1-3] (default: 1 = all): " mode_choice
mode_choice=${mode_choice:-1}

if ! [[ "$mode_choice" =~ ^[1-3]$ ]]; then
  echo "Invalid selection. Aborting."
  exit 1
fi

MODE="${MODES[$((mode_choice - 1))]}"

# ── Locale selection ───────────────────────────────────────────────────────────

echo ""
echo "Available locales: ${LOCALES[*]}"
read -rp "Enter locale to update (leave empty for all): " UPLOAD_LOCALE
UPLOAD_LOCALE=${UPLOAD_LOCALE:-""}

if [[ -n "$UPLOAD_LOCALE" ]]; then
  found=false
  for l in "${LOCALES[@]}"; do
    if [[ "$l" == "$UPLOAD_LOCALE" ]]; then
      found=true
      break
    fi
  done
  if [[ "$found" != "true" ]]; then
    echo "Error: Locale '$UPLOAD_LOCALE' not found in listing/"
    exit 1
  fi
fi

# ── Trigger workflow ───────────────────────────────────────────────────────────

echo ""
locale_label="${UPLOAD_LOCALE:-(all)}"
echo "Triggering listing workflow → mode: $MODE, locale: $locale_label"

if [[ -n "$UPLOAD_LOCALE" ]]; then
  gh workflow run listing.yml \
    --field "mode=$MODE" \
    --field "locale=$UPLOAD_LOCALE"
else
  gh workflow run listing.yml \
    --field "mode=$MODE"
fi

echo ""
echo "Workflow triggered. Monitor progress at:"
print_actions_url
