#!/usr/bin/env bash
# Trigger the Play Store listing update workflow (listing.yml).
#
# Scans the listing/ directory, runs pre-flight validations, shows a preview
# of what will be uploaded per locale, then prompts for upload mode and locale
# before triggering the GitHub Actions workflow.
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

# ── Validation config ──────────────────────────────────────────────────────────
#  Update these values when Google changes Play Store requirements.
#  Source: https://support.google.com/googleplay/android-developer/answer/9898842

# Text limits (characters, measured after stripping leading/trailing whitespace)
readonly V_APP_NAME_MAX=30
readonly V_SHORT_DESC_MAX=80
readonly V_FULL_DESC_MAX=4000

# App icon — PNG or JPEG, exactly 512×512, max 1 MB
readonly V_ICON_MAX_BYTES=$(( 1 * 1024 * 1024 ))
readonly V_ICON_W=512
readonly V_ICON_H=512

# Feature graphic — PNG or JPEG, exactly 1024×500, max 1 MB
readonly V_FEATURE_MAX_BYTES=$(( 1 * 1024 * 1024 ))
readonly V_FEATURE_W=1024
readonly V_FEATURE_H=500

# TV banner — PNG or JPEG, exactly 1280×720, max 1 MB
readonly V_TV_BANNER_MAX_BYTES=$(( 1 * 1024 * 1024 ))
readonly V_TV_BANNER_W=1280
readonly V_TV_BANNER_H=720

# Screenshots — PNG or JPEG, max 8 MB each
# Dimensions: each side must be between V_SS_MIN_PX and V_SS_MAX_PX
readonly V_SS_MAX_BYTES=$(( 8 * 1024 * 1024 ))
readonly V_SS_MIN_PX=320
readonly V_SS_MAX_PX=3840

# Screenshot counts per device type  (min=0 means the folder is optional)
readonly V_PHONE_SS_MIN=2
readonly V_PHONE_SS_MAX=8
readonly V_TABLET7_SS_MIN=0
readonly V_TABLET7_SS_MAX=8
readonly V_TABLET10_SS_MIN=0
readonly V_TABLET10_SS_MAX=8
readonly V_TV_SS_MIN=0
readonly V_TV_SS_MAX=8

# Video URL — must be a YouTube link
readonly V_VIDEO_URL_PATTERN='^https?://(www\.)?youtube\.com/|^https?://youtu\.be/'

# ── Scan helpers ───────────────────────────────────────────────────────────────

# Returns 0 (true) if the file exists and is not empty.
file_nonempty() { [[ -f "$1" ]] && [[ -s "$1" ]]; }

# Prints the number of image files in a directory (png/jpg/jpeg).
count_images() {
  find "$1" -maxdepth 1 -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) \
    2>/dev/null | wc -l | tr -d '[:space:]'
}

# ── Validation helpers ─────────────────────────────────────────────────────────

_val_ok=0
_val_fail=0
_val_warn=0

vpass() { printf "    ✓ %s\n" "$1"; _val_ok=$(( _val_ok + 1 )); }
vfail() { printf "    ✗ %s\n" "$1"; _val_fail=$(( _val_fail + 1 )); }
vwarn() { printf "    ⚠ %s\n" "$1"; _val_warn=$(( _val_warn + 1 )); }
vskip() { printf "    ○ %s\n" "$1"; }

# Unicode-correct character count (strips whitespace, same as upload_listing.py)
char_count() {
  python3 - "$1" <<'PYEOF' 2>/dev/null || echo 0
import sys
try:
    print(len(open(sys.argv[1], encoding='utf-8').read().strip()))
except Exception:
    print(0)
PYEOF
}

# File size in bytes (macOS stat -f, Linux stat -c)
file_bytes() {
  stat -f %z "$1" 2>/dev/null || stat -c %s "$1" 2>/dev/null || echo 0
}

# Format bytes as "X.X MB"
bytes_to_mb() {
  python3 -c "print(f'{$1 / 1048576:.1f} MB')" 2>/dev/null || echo "? MB"
}

# Image dimensions as "WxH" using sips (macOS); empty string if unavailable
img_dims() {
  if ! command -v sips &>/dev/null; then printf ""; return 0; fi
  local w h
  w=$(sips -g pixelWidth  "$1" 2>/dev/null | awk '/pixelWidth:/  {print $2}')
  h=$(sips -g pixelHeight "$1" 2>/dev/null | awk '/pixelHeight:/ {print $2}')
  if [[ -n "$w" && -n "$h" ]]; then
    printf '%sx%s' "$w" "$h"
  else
    printf ""
  fi
}

# Validate a text file against a character limit.
# Usage: validate_text <label> <path> <max_chars>
validate_text() {
  local label="$1" path="$2" max="$3"
  if ! file_nonempty "$path"; then return 0; fi
  local n
  n=$(char_count "$path")
  if [[ "$n" -le "$max" ]]; then
    vpass "$label: ${n}/${max} chars"
  else
    vfail "$label: ${n}/${max} chars — EXCEEDS LIMIT by $(( n - max ))"
  fi
}

# Validate that a non-empty file contains a YouTube URL.
# Usage: validate_url <label> <path>
validate_url() {
  local label="$1" path="$2"
  if ! file_nonempty "$path"; then return 0; fi
  local url
  url=$(tr -d '[:space:]' < "$path")
  if printf '%s' "$url" | grep -qE "$V_VIDEO_URL_PATTERN"; then
    vpass "$label: valid YouTube URL"
  else
    vfail "$label: '$url' — not a valid YouTube URL"
  fi
}

# Validate a single image file (icon, feature graphic, TV banner).
# Usage: validate_single_image <label> <path> <max_bytes> [<req_w> <req_h>]
validate_single_image() {
  local label="$1" path="$2" max_bytes="$3" req_w="${4:-}" req_h="${5:-}"
  if [[ ! -f "$path" ]]; then return 0; fi

  local name ext
  name="$(basename "$path")"
  ext="${name##*.}"; ext=$(printf '%s' "$ext" | tr '[:upper:]' '[:lower:]')

  if [[ "$ext" != "png" && "$ext" != "jpg" && "$ext" != "jpeg" ]]; then
    vfail "$label '$name': must be PNG or JPEG (found .$ext)"
    return 0
  fi

  local bytes
  bytes=$(file_bytes "$path")
  if [[ "$bytes" -gt "$max_bytes" ]]; then
    vfail "$label '$name': $(bytes_to_mb "$bytes") — max $(bytes_to_mb "$max_bytes")"
    return 0
  fi

  if [[ -n "$req_w" && -n "$req_h" ]]; then
    local dims w h
    dims=$(img_dims "$path")
    if [[ -n "$dims" ]]; then
      w="${dims%%x*}"; h="${dims##*x}"
      if [[ "$w" -eq "$req_w" && "$h" -eq "$req_h" ]]; then
        vpass "$label: ${req_w}×${req_h}, $(bytes_to_mb "$bytes")"
      else
        vfail "$label '$name': ${w}×${h} — expected ${req_w}×${req_h}"
      fi
    else
      vwarn "$label '$name': size OK but dimensions not verified (sips unavailable)"
    fi
  else
    vpass "$label: OK"
  fi
}

# Validate a screenshots folder: count and per-file size.
# Usage: validate_screenshots <label> <folder_path> <min> <max> <max_bytes_each>
validate_screenshots() {
  local label="$1" folder_path="$2" min_count="$3" max_count="$4" max_bytes="$5"
  if [[ ! -d "$folder_path" ]]; then return 0; fi

  local count
  count=$(count_images "$folder_path")

  if [[ "$count" -eq 0 ]]; then
    if [[ "$min_count" -gt 0 ]]; then
      vwarn "$label: 0 screenshots (min ${min_count} required — OK if default locale covers this)"
    fi
    return 0
  fi

  if [[ "$count" -lt "$min_count" ]]; then
    vwarn "$label: ${count} screenshots (min ${min_count} required — OK if default locale covers this)"
  elif [[ "$count" -gt "$max_count" ]]; then
    vwarn "$label: ${count} screenshots — max is ${max_count}"
  else
    vpass "$label: ${count} / ${max_count} screenshots"
  fi

  while IFS= read -r -d '' img; do
    local name bytes dims w h
    name="$(basename "$img")"
    bytes=$(file_bytes "$img")
    if [[ "$bytes" -gt "$max_bytes" ]]; then
      vwarn "$label '$name': $(bytes_to_mb "$bytes") — max $(bytes_to_mb "$max_bytes")"
    fi
    dims=$(img_dims "$img")
    if [[ -n "$dims" ]]; then
      w="${dims%%x*}"; h="${dims##*x}"
      if [[ "$w" -lt "$V_SS_MIN_PX" || "$w" -gt "$V_SS_MAX_PX" \
         || "$h" -lt "$V_SS_MIN_PX" || "$h" -gt "$V_SS_MAX_PX" ]]; then
        vwarn "$label '$name': ${w}×${h} — each side must be ${V_SS_MIN_PX}–${V_SS_MAX_PX}px"
      fi
    fi
  done < <(find "$folder_path" -maxdepth 1 -type f \
    \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) -print0 2>/dev/null)
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

# ── Pre-flight validation ──────────────────────────────────────────────────────

echo "=== Pre-flight Validation ==="
echo ""

for locale in "${LOCALES[@]}"; do
  locale_dir="$LISTING_DIR/$locale"
  echo "  [$locale]"

  # ── Text ──────────────────────────────────────────────────────────────────

  name_file="$locale_dir/app-name.txt"
  if [[ ! -f "$name_file" ]]; then name_file="$locale_dir/title.txt"; fi
  validate_text "app-name"          "$name_file"                         "$V_APP_NAME_MAX"
  validate_text "short_description" "$locale_dir/short_description.txt"  "$V_SHORT_DESC_MAX"
  validate_text "full_description"  "$locale_dir/full_description.txt"   "$V_FULL_DESC_MAX"

  # ── Video URL ──────────────────────────────────────────────────────────────

  validate_url "video-url" "$locale_dir/video/video-url.txt"

  for vf in "spatial-xr-video-url.txt" "non-spatial-xr-video-url.txt"; do
    if file_nonempty "$locale_dir/video/$vf"; then
      vskip "video/$vf: present but NOT uploaded (API v3 does not expose this field)"
    fi
  done

  # ── Images ────────────────────────────────────────────────────────────────

  images_dir="$locale_dir/graphics"
  if [[ ! -d "$images_dir" ]]; then images_dir="$locale_dir/images"; fi

  if [[ -d "$images_dir" ]]; then

    # App icon (single file, exactly 512×512)
    first_icon=""
    first_icon=$(find "$images_dir/app-icon" -maxdepth 1 -type f \
      \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) 2>/dev/null | head -1 || true)
    if [[ -n "$first_icon" ]]; then
      validate_single_image "app-icon" "$first_icon" \
        "$V_ICON_MAX_BYTES" "$V_ICON_W" "$V_ICON_H"
    fi

    # Feature graphic (single file, exactly 1024×500)
    first_feat=""
    first_feat=$(find "$images_dir/feature-graphics" -maxdepth 1 -type f \
      \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) 2>/dev/null | head -1 || true)
    if [[ -n "$first_feat" ]]; then
      validate_single_image "feature-graphics" "$first_feat" \
        "$V_FEATURE_MAX_BYTES" "$V_FEATURE_W" "$V_FEATURE_H"
    fi

    # TV banner (single file, exactly 1280×720)
    first_banner=""
    first_banner=$(find "$images_dir/banner-tv" -maxdepth 1 -type f \
      \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) 2>/dev/null | head -1 || true)
    if [[ -n "$first_banner" ]]; then
      validate_single_image "banner-tv" "$first_banner" \
        "$V_TV_BANNER_MAX_BYTES" "$V_TV_BANNER_W" "$V_TV_BANNER_H"
    fi

    # Screenshots
    validate_screenshots "screenshots-phone" \
      "$images_dir/screenshots-phone" \
      "$V_PHONE_SS_MIN" "$V_PHONE_SS_MAX" "$V_SS_MAX_BYTES"

    validate_screenshots "screenshots-tablet-7-inch" \
      "$images_dir/screenshots-tablet-7-inch" \
      "$V_TABLET7_SS_MIN" "$V_TABLET7_SS_MAX" "$V_SS_MAX_BYTES"

    validate_screenshots "screenshots-tablet-10-inch" \
      "$images_dir/screenshots-tablet-10-inch" \
      "$V_TABLET10_SS_MIN" "$V_TABLET10_SS_MAX" "$V_SS_MAX_BYTES"

    validate_screenshots "screenshots-tv" \
      "$images_dir/screenshots-tv" \
      "$V_TV_SS_MIN" "$V_TV_SS_MAX" "$V_SS_MAX_BYTES"

    # Unsupported image folders — kept for future use, NOT uploaded
    for unsupported_folder in "screenshots-chromebook" "screenshots-xr"; do
      if [[ -d "$images_dir/$unsupported_folder" ]]; then
        n=$(count_images "$images_dir/$unsupported_folder")
        if [[ "$n" -gt 0 ]]; then
          vskip "$unsupported_folder: ${n} file(s) present but NOT uploaded (imageType not in API v3 enum)"
        fi
      fi
    done

  fi

  echo ""
done

# ── Validation summary ─────────────────────────────────────────────────────────

echo "  ────────────────────────────────────────"
printf "  Validation: %d passed" "$_val_ok"
if [[ "$_val_warn" -gt 0 ]]; then printf ",  %d warning(s)" "$_val_warn"; fi
if [[ "$_val_fail" -gt 0 ]]; then printf ",  %d FAILED" "$_val_fail"; fi
printf "\n"
echo "  ────────────────────────────────────────"
echo ""

if [[ "$_val_fail" -gt 0 ]]; then
  echo "  ⚠ Pre-flight validation has failures — review the issues above before uploading."
  echo ""
elif [[ "$_val_warn" -gt 0 ]]; then
  echo "  ⚠ Pre-flight validation has warnings — review above if needed."
  echo ""
else
  echo "  ✓ Pre-flight validation passed."
fi
echo ""

# ── Play Store Listing Preview ─────────────────────────────────────────────────

echo "=== Play Store Listing Preview ==="
echo ""

for locale in "${LOCALES[@]}"; do
  locale_dir="$LISTING_DIR/$locale"
  text_count=0
  image_count=0

  echo "  [$locale]"

  # ── Text files ────────────────────────────────────────────────────────────

  # app-name.txt is canonical; title.txt is the legacy fallback.
  name_file="$locale_dir/app-name.txt"
  if [[ ! -f "$name_file" ]]; then name_file="$locale_dir/title.txt"; fi
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

  # ── Video URLs ────────────────────────────────────────────────────────────

  video_dir="$locale_dir/video"
  if [[ -d "$video_dir" ]]; then
    for vf in "video-url.txt" "spatial-xr-video-url.txt" "non-spatial-xr-video-url.txt"; do
      if file_nonempty "$video_dir/$vf"; then
        echo "    + video/$vf"
        text_count=$(( text_count + 1 ))
      fi
    done
  fi

  # ── Image folders ─────────────────────────────────────────────────────────

  # graphics/ is canonical; images/ is the legacy fallback.
  images_dir="$locale_dir/graphics"
  if [[ ! -d "$images_dir" ]]; then images_dir="$locale_dir/images"; fi

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
