#!/usr/bin/env python3
"""Upload Play Store listing (text + images) from the listing/ directory.

Folder structure under listing/<locale>/graphics/  (legacy: images/):
  app-icon/                   → icon
  feature-graphics/           → featureGraphic
  screenshots-phone/          → phoneScreenshots
  screenshots-tablet-7-inch/  → sevenInchScreenshots
  screenshots-tablet-10-inch/ → tenInchScreenshots
  screenshots-tv/             → tvScreenshots
  banner-tv/                  → tvBanner

Text files under listing/<locale>/:
  app-name.txt           → title  (legacy alias: title.txt)
  short_description.txt  → shortDescription
  full_description.txt   → fullDescription

Video URL files under listing/<locale>/video/:
  video-url.txt  → video

UNSUPPORTED asset types (kept in folder structure for future use):
  screenshots-chromebook/     → 'chromebook' imageType is NOT in the official
                                AppImageType enum (Play Developer API v3).
                                Files are silently skipped with an info note.
  screenshots-xr/             → 'xrScreenshots' imageType is NOT in the official
                                AppImageType enum (Play Developer API v3).
                                Files are silently skipped with an info note.
  video/spatial-xr-video-url.txt     → 'spatialVideo' field is NOT exposed by
                                        the Listing resource in API v3.
  video/non-spatial-xr-video-url.txt → 'nonSpatialVideo' field is NOT exposed by
                                        the Listing resource in API v3.

  When Google adds these to the API the relevant entries just need to be moved
  from UNSUPPORTED_FOLDERS / UNSUPPORTED_VIDEO_FILES into the active mappings.

Files that are absent or empty are silently skipped and never uploaded.

Environment variables
---------------------
GOOGLE_PLAY_SERVICE_ACCOUNT_JSON  (required) – service-account JSON string
UPLOAD_MODE                        all | text | images  (default: all)
UPLOAD_LOCALE                      e.g. en-US  (default: empty → all locales)
"""

import json
import mimetypes
import os
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

PACKAGE_NAME = "com.eahm.drinkwaterapp"
LISTING_DIR = Path(__file__).parent.parent / "listing"
SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]

# Default locale in Google Play Console. Must be uploaded before any other
# locale. The Play API rejects secondary locales if the default is missing.
DEFAULT_LOCALE = "en-US"

# Maps folder names → Play Developer API imageType identifiers.
# Only imageTypes confirmed in the official AppImageType enum are listed here.
# Failures for any of these are FATAL and trigger a full rollback.
FOLDER_TO_IMAGE_TYPE = {
    # Standard types
    "app-icon":                   "icon",
    "feature-graphics":           "featureGraphic",
    "screenshots-phone":          "phoneScreenshots",
    "screenshots-tablet-7-inch":  "sevenInchScreenshots",
    "screenshots-tablet-10-inch": "tenInchScreenshots",
    "screenshots-tv":             "tvScreenshots",
    "banner-tv":                  "tvBanner",
    # Legacy folder names (kept for backward compatibility)
    "screenshots-7in":            "sevenInchScreenshots",
    "screenshots-10in":           "tenInchScreenshots",
    "screenshots-wear":           "wearScreenshots",
}

# Folders that exist in the repository for future use but CANNOT be uploaded
# via the current Play Developer API v3.
# Key   → folder name under graphics/
# Value → human-readable reason shown in the output
UNSUPPORTED_FOLDERS = {
    "screenshots-chromebook": (
        "The 'chromebook' imageType is not listed in the official AppImageType enum. "
        "Upload is not possible until Google adds it to the Play Developer API."
    ),
    "screenshots-xr": (
        "The 'xrScreenshots' imageType is not listed in the official AppImageType enum. "
        "Upload is not possible until Google adds it to the Play Developer API."
    ),
}

# Video file names that exist in the repository for future use but are NOT
# exposed as Listing resource fields in the current API v3.
UNSUPPORTED_VIDEO_FILES = {
    "spatial-xr-video-url.txt": (
        "The 'spatialVideo' field is not exposed by the Listing resource "
        "in the Play Developer API v3."
    ),
    "non-spatial-xr-video-url.txt": (
        "The 'nonSpatialVideo' field is not exposed by the Listing resource "
        "in the Play Developer API v3."
    ),
}

# ── Config from env ─────────────────────────────────────────────────────────────

UPLOAD_MODE = os.environ.get("UPLOAD_MODE", "all").strip().lower()
if UPLOAD_MODE not in {"all", "text", "images"}:
    sys.exit(f"Invalid UPLOAD_MODE '{UPLOAD_MODE}'. Must be: all | text | images")

UPLOAD_LOCALE = os.environ.get("UPLOAD_LOCALE", "").strip()

# ── Result tracking ─────────────────────────────────────────────────────────────

# Each entry: {"locale": str, "asset": str, "status": "ok"|"failed"|"skipped", "error": str}
_results: list[dict] = []


def _record(locale: str, asset: str, status: str, error: str = ""):
    _results.append({"locale": locale, "asset": asset, "status": status, "error": error})


def _print_summary():
    sep = "─" * 62
    print(f"\n{sep}")
    print("  UPLOAD SUMMARY")
    print(sep)
    for r in _results:
        if r["status"] == "ok":
            icon = "✓"
        elif r["status"] == "skipped":
            icon = "○"
        else:
            icon = "✗"
        print(f"  {icon}  [{r['locale']}]  {r['asset']}")
        if r["error"]:
            label = "skipped" if r["status"] == "skipped" else "error"
            print(f"       ({label}) {r['error']}")
    ok_count      = sum(1 for r in _results if r["status"] == "ok")
    failed_count  = sum(1 for r in _results if r["status"] == "failed")
    skipped_count = sum(1 for r in _results if r["status"] == "skipped")
    total = len(_results)
    parts = [f"  {ok_count}/{total} operations succeeded"]
    if failed_count:
        parts.append(f"{failed_count} failed")
    if skipped_count:
        parts.append(f"{skipped_count} skipped (unsupported API fields)")
    print(sep)
    print("  —  ".join(parts))
    print(sep)


# ── Google Play client ──────────────────────────────────────────────────────────


def build_service():
    raw = os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON")
    if not raw:
        sys.exit("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON env var is not set")
    try:
        info = json.loads(raw)
    except json.JSONDecodeError as e:
        sys.exit(f"GOOGLE_PLAY_SERVICE_ACCOUNT_JSON is not valid JSON: {e.msg} at line {e.lineno}")
    try:
        creds = service_account.Credentials.from_service_account_info(info, scopes=SCOPES)
        service = build("androidpublisher", "v3", credentials=creds)
        return service
    except Exception as e:
        sys.exit(f"Failed to build Google Play service: {type(e).__name__}")


def _api_revision(service) -> str:
    """Return the discovery-doc revision string, e.g. 'r20241010'."""
    try:
        return service._rootDesc.get("revision", "unknown")
    except Exception:
        return "unknown"


# ── Upload helpers ──────────────────────────────────────────────────────────────


def upload_listing(service, edit_id):
    if not LISTING_DIR.is_dir():
        print(f"listing/ directory not found at {LISTING_DIR}, skipping.")
        return

    locale_dirs = sorted(d for d in LISTING_DIR.iterdir() if d.is_dir())
    if not locale_dirs:
        print("No locale directories found under listing/, skipping.")
        return

    if UPLOAD_LOCALE:
        locale_dirs = [d for d in locale_dirs if d.name == UPLOAD_LOCALE]
        if not locale_dirs:
            sys.exit(f"Locale '{UPLOAD_LOCALE}' not found in listing/")
    else:
        # Always upload the default locale first so secondary locales don't
        # get rejected by the Play API when the default listing is absent.
        locale_dirs.sort(key=lambda d: (0 if d.name == DEFAULT_LOCALE else 1, d.name))

    for locale_dir in locale_dirs:
        locale = locale_dir.name
        print(f"\n--- Locale: {locale} ---")
        if UPLOAD_MODE in {"all", "text"}:
            upload_text(service, edit_id, locale, locale_dir)
        if UPLOAD_MODE in {"all", "images"}:
            upload_images(service, edit_id, locale, locale_dir)


def _read_if_nonempty(path: Path) -> str | None:
    """Return stripped file content, or None if the file is absent or blank."""
    if not path.exists():
        return None
    text = path.read_text(encoding="utf-8").strip()
    return text if text else None


def upload_text(service, edit_id, locale, locale_dir):
    # app-name.txt is the canonical name; title.txt is the legacy fallback.
    title_path = locale_dir / "app-name.txt"
    if not title_path.exists():
        title_path = locale_dir / "title.txt"

    text_fields: dict[str, Path] = {
        "title":            title_path,
        "shortDescription": locale_dir / "short_description.txt",
        "fullDescription":  locale_dir / "full_description.txt",
    }

    # Only "video" is a valid Listing field in the current API v3.
    video_dir = locale_dir / "video"
    video_fields: dict[str, Path] = {
        "video": video_dir / "video-url.txt",
    }

    body: dict[str, str] = {}
    for field, path in {**text_fields, **video_fields}.items():
        value = _read_if_nonempty(path)
        if value is not None:
            body[field] = value
            print(f"  {field}: {len(value)} chars")
        elif path.exists():
            print(f"  {field}: (empty — skipped)")

    # Inform about video files that exist but are not supported by the API yet.
    if video_dir.is_dir():
        rev = _api_revision(service)
        for filename, reason in UNSUPPORTED_VIDEO_FILES.items():
            fpath = video_dir / filename
            if fpath.exists() and fpath.stat().st_size > 0:
                print(f"  ○ video/{filename}: not uploaded")
                print(f"       (API {rev}) {reason}")
                _record(locale, f"video/{filename}", status="skipped",
                        error=f"(API {rev}) {reason}")

    if not body:
        print("  No text files found or all are empty, skipping text upload.")
        return

    try:
        service.edits().listings().patch(
            editId=edit_id,
            packageName=PACKAGE_NAME,
            language=locale,
            body=body,
        ).execute()
        print(f"  ✓ Text listing updated for {locale}.")
        _record(locale, "text", status="ok")
    except Exception as e:
        print(f"  ✗ Text listing failed for {locale}: {e}")
        _record(locale, "text", status="failed", error=str(e))


def _image_sort_key(p: Path):
    """Numeric sort for filenames like 1.png, 2.png, 10.png; alphabetical fallback."""
    try:
        return (0, int(p.stem), "")
    except ValueError:
        return (1, 0, p.name.lower())


def upload_images(service, edit_id, locale, locale_dir):
    # graphics/ is the canonical folder name; images/ is the legacy fallback.
    images_dir = locale_dir / "graphics"
    if not images_dir.is_dir():
        images_dir = locale_dir / "images"
    if not images_dir.is_dir():
        print(f"  No graphics/ or images/ directory — skipping all image types for {locale}.")
        return

    for folder_name, image_type in FOLDER_TO_IMAGE_TYPE.items():
        folder = images_dir / folder_name

        # Folder absent → skip silently, not a failure
        if not folder.is_dir():
            continue

        image_files = sorted(
            (f for f in folder.iterdir()
             if f.is_file() and f.suffix.lower() in {".png", ".jpg", ".jpeg"}),
            key=_image_sort_key,
        )

        # Folder present but empty → skip silently, not a failure
        if not image_files:
            print(f"  [{folder_name}] empty — skipped.")
            continue

        print(f"  [{folder_name} → {image_type}] {len(image_files)} file(s) — upload order:")
        for i, f in enumerate(image_files, 1):
            print(f"    {i}. {f.name}")

        try:
            service.edits().images().deleteall(
                editId=edit_id,
                packageName=PACKAGE_NAME,
                language=locale,
                imageType=image_type,
            ).execute()

            for image_file in image_files:
                mime_type, _ = mimetypes.guess_type(str(image_file))
                if not mime_type:
                    mime_type = "image/png"
                media = MediaFileUpload(str(image_file), mimetype=mime_type)
                service.edits().images().upload(
                    editId=edit_id,
                    packageName=PACKAGE_NAME,
                    language=locale,
                    imageType=image_type,
                    media_body=media,
                ).execute()
                print(f"    ✓ {image_file.name}")

            _record(locale, image_type, status="ok")

        except Exception as e:
            print(f"  ✗ {image_type} failed: {e}")
            _record(locale, image_type, status="failed", error=str(e))

    # Inform about unsupported image folders that contain files.
    rev = _api_revision(service)
    for folder_name, reason in UNSUPPORTED_FOLDERS.items():
        folder = images_dir / folder_name
        if not folder.is_dir():
            continue
        image_files = [
            f for f in folder.iterdir()
            if f.is_file() and f.suffix.lower() in {".png", ".jpg", ".jpeg"}
        ]
        if not image_files:
            continue
        print(f"  ○ [{folder_name}] {len(image_files)} file(s) found — not uploaded")
        print(f"       (API {rev}) {reason}")
        _record(locale, folder_name, status="skipped",
                error=f"(API {rev}) {reason}")


# ── Entry point ─────────────────────────────────────────────────────────────────


def main():
    print(f"Mode     : {UPLOAD_MODE}")
    print(f"Locale   : {UPLOAD_LOCALE or '(all)'}")
    print(f"Package  : {PACKAGE_NAME}")

    service = build_service()
    edit = service.edits().insert(packageName=PACKAGE_NAME, body={}).execute()
    edit_id = edit["id"]
    print(f"\nOpened edit: {edit_id}")

    upload_listing(service, edit_id)

    _print_summary()

    has_fatal_failures = any(r["status"] == "failed" for r in _results)
    if has_fatal_failures:
        service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
        print("\nEdit deleted (rolled back) due to fatal failures above.")
        sys.exit(1)

    try:
        service.edits().commit(editId=edit_id, packageName=PACKAGE_NAME).execute()
        print("\nEdit committed successfully.")
    except Exception as e:
        error_str = str(e)
        print(f"\n✗ Commit failed: {error_str}")
        if "403" in error_str or "does not have permission" in error_str.lower():
            print("""
──────────────────────────────────────────────────────────────
  PERMISSION ERROR — HOW TO FIX
──────────────────────────────────────────────────────────────
  The service account could open and upload the edit but does
  not have permission to commit it. Set the right permission for
  your service account in the Play Console user management
  configuration.
──────────────────────────────────────────────────────────────
""")
        sys.exit(1)


if __name__ == "__main__":
    main()
