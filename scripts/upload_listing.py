#!/usr/bin/env python3
"""Upload Play Store listing (text + images) from the listing/ directory.

Folder structure under listing/<locale>/images/:
  app-icon/          → icon
  feature-graphics/  → featureGraphic
  screenshots-phone/ → phoneScreenshots
  screenshots-7in/   → sevenInchScreenshots
  screenshots-10in/  → tenInchScreenshots
  screenshots-tv/    → tvScreenshots
  screenshots-wear/  → wearScreenshots
  banner-tv/         → tvBanner

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

# Maps human-readable folder names → Play Developer API imageType identifiers
FOLDER_TO_IMAGE_TYPE = {
    "app-icon":           "icon",
    "feature-graphics":   "featureGraphic",
    "screenshots-phone":  "phoneScreenshots",
    "screenshots-7in":    "sevenInchScreenshots",
    "screenshots-10in":   "tenInchScreenshots",
    "screenshots-tv":     "tvScreenshots",
    "screenshots-wear":   "wearScreenshots",
    "banner-tv":          "tvBanner",
}

# ── Config from env ────────────────────────────────────────────────────────────

UPLOAD_MODE = os.environ.get("UPLOAD_MODE", "all").strip().lower()
if UPLOAD_MODE not in {"all", "text", "images"}:
    sys.exit(f"Invalid UPLOAD_MODE '{UPLOAD_MODE}'. Must be: all | text | images")

UPLOAD_LOCALE = os.environ.get("UPLOAD_LOCALE", "").strip()

# ── Result tracking ────────────────────────────────────────────────────────────

# Each entry: {"locale": str, "asset": str, "ok": bool, "error": str}
_results: list[dict] = []


def _record(locale: str, asset: str, ok: bool, error: str = ""):
    _results.append({"locale": locale, "asset": asset, "ok": ok, "error": error})


def _print_summary():
    sep = "─" * 62
    print(f"\n{sep}")
    print("  UPLOAD SUMMARY")
    print(sep)
    for r in _results:
        icon = "✓" if r["ok"] else "✗"
        print(f"  {icon}  [{r['locale']}]  {r['asset']}")
        if not r["ok"]:
            print(f"       {r['error']}")
    failures = sum(1 for r in _results if not r["ok"])
    total = len(_results)
    print(sep)
    print(f"  {total - failures}/{total} operations succeeded"
          + (f"  —  {failures} failed" if failures else ""))
    print(sep)


# ── Google Play client ─────────────────────────────────────────────────────────


def build_service():
    raw = os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON")
    if not raw:
        sys.exit("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON env var is not set")
    info = json.loads(raw)
    creds = service_account.Credentials.from_service_account_info(info, scopes=SCOPES)
    return build("androidpublisher", "v3", credentials=creds)


# ── Upload helpers ─────────────────────────────────────────────────────────────


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


def upload_text(service, edit_id, locale, locale_dir):
    fields = {
        "title":            locale_dir / "title.txt",
        "shortDescription": locale_dir / "short_description.txt",
        "fullDescription":  locale_dir / "full_description.txt",
    }

    body = {}
    for field, path in fields.items():
        if path.exists():
            text = path.read_text(encoding="utf-8").strip()
            body[field] = text
            print(f"  {field}: {len(text)} chars")

    if not body:
        print("  No text files found, skipping text upload.")
        return

    try:
        service.edits().listings().patch(
            editId=edit_id,
            packageName=PACKAGE_NAME,
            language=locale,
            body=body,
        ).execute()
        print(f"  ✓ Text listing updated for {locale}.")
        _record(locale, "text", ok=True)
    except Exception as e:
        print(f"  ✗ Text listing failed for {locale}: {e}")
        _record(locale, "text", ok=False, error=str(e))


def _image_sort_key(p: Path):
    """Numeric sort for filenames like 1.png, 2.png, 10.png; alphabetical fallback."""
    try:
        return (0, int(p.stem), "")
    except ValueError:
        return (1, 0, p.name.lower())


def upload_images(service, edit_id, locale, locale_dir):
    images_dir = locale_dir / "images"
    if not images_dir.is_dir():
        print(f"  No images/ directory — skipping all image types for {locale}.")
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

        # Folder present and has content → attempt upload
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

            _record(locale, image_type, ok=True)

        except Exception as e:
            print(f"  ✗ {image_type} failed: {e}")
            _record(locale, image_type, ok=False, error=str(e))


# ── Entry point ────────────────────────────────────────────────────────────────


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

    has_failures = any(not r["ok"] for r in _results)
    if has_failures:
        service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
        print("\nEdit deleted (rolled back) due to failures above.")
        sys.exit(1)
    else:
        service.edits().commit(editId=edit_id, packageName=PACKAGE_NAME).execute()
        print("\nEdit committed successfully.")


if __name__ == "__main__":
    main()
