#!/usr/bin/env python3
"""Upload Play Store listing (text + images) from the listing/ directory.

Folder structure under listing/<locale>/graphics/  (legacy: images/):
  app-icon/                   → icon
  feature-graphics/           → featureGraphic
  screenshots-phone/          → phoneScreenshots
  screenshots-tablet-7-inch/  → sevenInchScreenshots
  screenshots-tablet-10-inch/ → tenInchScreenshots
  screenshots-tv/             → tvScreenshots
  screenshots-xr/             → xrScreenshots  [EXPERIMENTAL — see note below]
  screenshots-chromebook/     → chromebook  [EXPERIMENTAL — see note below]
  banner-tv/                  → tvBanner

Text files under listing/<locale>/:
  app-name.txt           → title  (legacy alias: title.txt)
  short_description.txt  → shortDescription
  full_description.txt   → fullDescription

Video URL files under listing/<locale>/video/:
  video-url.txt  → video
  (spatial-xr-video-url.txt / non-spatial-xr-video-url.txt are kept in the
   folder structure for future use but are not sent — the Listing API v3 does
   not expose spatialVideo / nonSpatialVideo fields yet.)

EXPERIMENTAL image types:
  The official AppImageType enum (discovery doc) lists only 9 types and does
  not include "chromebook" or "xrScreenshots". The googleapiclient library
  would reject them client-side, so experimental types are uploaded via a
  direct authenticated HTTP call that bypasses client validation.
  If the API server rejects them, the error is recorded as a WARNING (⚠) and
  does NOT trigger a rollback — only text failures and standard image failures
  cause rollbacks.

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

import requests as _requests
from google.auth.transport.requests import AuthorizedSession
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

PACKAGE_NAME = "com.eahm.drinkwaterapp"
LISTING_DIR = Path(__file__).parent.parent / "listing"
SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
PUBLISHER_BASE = "https://androidpublisher.googleapis.com/androidpublisher/v3"
UPLOAD_BASE = "https://androidpublisher.googleapis.com/upload/androidpublisher/v3"

# Default locale in Google Play Console. Must be uploaded before any other
# locale. The Play API rejects secondary locales if the default is missing.
DEFAULT_LOCALE = "en-US"

# imageTypes confirmed in the official AppImageType enum (discovery doc).
# Failures for these are FATAL and trigger a full rollback.
OFFICIAL_IMAGE_TYPES = {
    "appImageTypeUnspecified",
    "phoneScreenshots",
    "sevenInchScreenshots",
    "tenInchScreenshots",
    "tvScreenshots",
    "wearScreenshots",
    "icon",
    "featureGraphic",
    "tvBanner",
}

# Maps folder names → Play Developer API imageType identifiers.
# Types not in OFFICIAL_IMAGE_TYPES are uploaded via direct HTTP (bypasses
# client-side enum validation) and recorded as warnings on failure.
FOLDER_TO_IMAGE_TYPE = {
    # Standard types
    "app-icon":                   "icon",
    "feature-graphics":           "featureGraphic",
    "screenshots-phone":          "phoneScreenshots",
    "screenshots-tablet-7-inch":  "sevenInchScreenshots",
    "screenshots-tablet-10-inch": "tenInchScreenshots",
    "screenshots-tv":             "tvScreenshots",
    "banner-tv":                  "tvBanner",
    # Experimental — not in official enum; uploaded via raw HTTP to test.
    # Failure is a warning (⚠), not a fatal error.
    "screenshots-chromebook":     "chromebook",
    "screenshots-xr":             "xrScreenshots",
    # Legacy folder names (kept for backward compatibility)
    "screenshots-7in":            "sevenInchScreenshots",
    "screenshots-10in":           "tenInchScreenshots",
    "screenshots-wear":           "wearScreenshots",
}

# ── Config from env ────────────────────────────────────────────────────────────

UPLOAD_MODE = os.environ.get("UPLOAD_MODE", "all").strip().lower()
if UPLOAD_MODE not in {"all", "text", "images"}:
    sys.exit(f"Invalid UPLOAD_MODE '{UPLOAD_MODE}'. Must be: all | text | images")

UPLOAD_LOCALE = os.environ.get("UPLOAD_LOCALE", "").strip()

# ── Result tracking ────────────────────────────────────────────────────────────

# Each entry: {"locale": str, "asset": str, "ok": bool, "fatal": bool, "error": str}
_results: list[dict] = []


def _record(locale: str, asset: str, ok: bool, error: str = "", fatal: bool = True):
    _results.append({"locale": locale, "asset": asset, "ok": ok,
                      "fatal": fatal, "error": error})


def _print_summary():
    sep = "─" * 62
    print(f"\n{sep}")
    print("  UPLOAD SUMMARY")
    print(sep)
    for r in _results:
        if r["ok"]:
            icon = "✓"
        elif r["fatal"]:
            icon = "✗"
        else:
            icon = "⚠"
        print(f"  {icon}  [{r['locale']}]  {r['asset']}")
        if not r["ok"]:
            label = "error" if r["fatal"] else "warning"
            print(f"       ({label}) {r['error']}")
    failures = sum(1 for r in _results if not r["ok"] and r["fatal"])
    warnings = sum(1 for r in _results if not r["ok"] and not r["fatal"])
    total = len(_results)
    ok_count = total - failures - warnings
    parts = [f"  {ok_count}/{total} operations succeeded"]
    if failures:
        parts.append(f"{failures} failed")
    if warnings:
        parts.append(f"{warnings} warning(s)")
    print(sep)
    print("  —  ".join(parts))
    print(sep)


# ── Google Play client ─────────────────────────────────────────────────────────


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
        return service, creds
    except Exception as e:
        sys.exit(f"Failed to build Google Play service: {type(e).__name__}")


# ── Upload helpers ─────────────────────────────────────────────────────────────


def upload_listing(service, creds, edit_id):
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
            upload_images(service, creds, edit_id, locale, locale_dir)


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
    # spatial-xr-video-url.txt / non-spatial-xr-video-url.txt are kept in the
    # folder structure for future use but intentionally not sent.
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
        _record(locale, "text", ok=True)
    except Exception as e:
        print(f"  ✗ Text listing failed for {locale}: {e}")
        _record(locale, "text", ok=False, error=str(e), fatal=True)


def _image_sort_key(p: Path):
    """Numeric sort for filenames like 1.png, 2.png, 10.png; alphabetical fallback."""
    try:
        return (0, int(p.stem), "")
    except ValueError:
        return (1, 0, p.name.lower())


def _upload_images_experimental(session: AuthorizedSession, edit_id: str,
                                 locale: str, image_type: str,
                                 image_files: list[Path]):
    """Upload images for non-official imageTypes via raw HTTP, bypassing
    client-side enum validation. Returns (success: bool, error: str)."""
    # Delete existing images for this type
    del_url = (f"{PUBLISHER_BASE}/applications/{PACKAGE_NAME}"
               f"/edits/{edit_id}/listings/{locale}/{image_type}")
    resp = session.delete(del_url)
    if resp.status_code not in (200, 204):
        return False, f"deleteall returned HTTP {resp.status_code}: {resp.text[:200]}"

    # Upload each image
    for image_file in image_files:
        mime_type, _ = mimetypes.guess_type(str(image_file))
        if not mime_type:
            mime_type = "image/png"
        upload_url = (f"{UPLOAD_BASE}/applications/{PACKAGE_NAME}"
                      f"/edits/{edit_id}/listings/{locale}/{image_type}"
                      f"?uploadType=media")
        with open(image_file, "rb") as f:
            resp = session.post(upload_url,
                                headers={"Content-Type": mime_type},
                                data=f)
        if resp.status_code not in (200, 201):
            return False, (f"{image_file.name} returned HTTP {resp.status_code}: "
                           f"{resp.text[:200]}")
        print(f"    ✓ {image_file.name}")

    return True, ""


def upload_images(service, creds, edit_id, locale, locale_dir):
    # graphics/ is the canonical folder name; images/ is the legacy fallback.
    images_dir = locale_dir / "graphics"
    if not images_dir.is_dir():
        images_dir = locale_dir / "images"
    if not images_dir.is_dir():
        print(f"  No graphics/ or images/ directory — skipping all image types for {locale}.")
        return

    # Lazy-init a session for experimental types only when needed.
    _session: AuthorizedSession | None = None

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

        is_experimental = image_type not in OFFICIAL_IMAGE_TYPES

        print(f"  [{folder_name} → {image_type}]"
              + (" [experimental]" if is_experimental else "")
              + f" {len(image_files)} file(s) — upload order:")
        for i, f in enumerate(image_files, 1):
            print(f"    {i}. {f.name}")

        if is_experimental:
            # Use raw HTTP to bypass client-side enum validation.
            if _session is None:
                _requests.packages.urllib3.disable_warnings()  # suppress noise
                _session = AuthorizedSession(creds)
            ok, err = _upload_images_experimental(
                _session, edit_id, locale, image_type, image_files)
            if ok:
                print(f"  ✓ {image_type} uploaded (experimental).")
                _record(locale, image_type, ok=True)
            else:
                print(f"  ⚠ {image_type} failed (experimental — warning only): {err}")
                _record(locale, image_type, ok=False, error=err, fatal=False)
        else:
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
                _record(locale, image_type, ok=False, error=str(e), fatal=True)


# ── Entry point ────────────────────────────────────────────────────────────────


def main():
    print(f"Mode     : {UPLOAD_MODE}")
    print(f"Locale   : {UPLOAD_LOCALE or '(all)'}")
    print(f"Package  : {PACKAGE_NAME}")

    service, creds = build_service()
    edit = service.edits().insert(packageName=PACKAGE_NAME, body={}).execute()
    edit_id = edit["id"]
    print(f"\nOpened edit: {edit_id}")

    upload_listing(service, creds, edit_id)

    _print_summary()

    has_fatal_failures = any(not r["ok"] and r["fatal"] for r in _results)
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
