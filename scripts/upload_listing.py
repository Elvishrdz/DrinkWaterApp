#!/usr/bin/env python3
"""Upload Play Store listing (text + images) from the listing/ directory."""

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

IMAGE_TYPES = {
    "phoneScreenshots",
    "sevenInchScreenshots",
    "tenInchScreenshots",
    "tvScreenshots",
    "wearScreenshots",
    "icon",
    "featureGraphic",
    "tvBanner",
}


def build_service():
    raw = os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON")
    if not raw:
        sys.exit("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON env var is not set")
    info = json.loads(raw)
    creds = service_account.Credentials.from_service_account_info(info, scopes=SCOPES)
    return build("androidpublisher", "v3", credentials=creds)


def upload_listing(service, edit_id):
    if not LISTING_DIR.is_dir():
        print(f"listing/ directory not found at {LISTING_DIR}, skipping.")
        return

    locales = [d for d in LISTING_DIR.iterdir() if d.is_dir()]
    if not locales:
        print("No locale directories found under listing/, skipping.")
        return

    for locale_dir in sorted(locales):
        locale = locale_dir.name
        print(f"\n--- Locale: {locale} ---")
        upload_text(service, edit_id, locale, locale_dir)
        upload_images(service, edit_id, locale, locale_dir)


def upload_text(service, edit_id, locale, locale_dir):
    fields = {
        "title": locale_dir / "title.txt",
        "shortDescription": locale_dir / "short_description.txt",
        "fullDescription": locale_dir / "full_description.txt",
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

    service.edits().listings().patch(
        editId=edit_id,
        packageName=PACKAGE_NAME,
        language=locale,
        body=body,
    ).execute()
    print(f"  Text listing updated for {locale}.")


def upload_images(service, edit_id, locale, locale_dir):
    images_dir = locale_dir / "images"
    if not images_dir.is_dir():
        return

    for image_type_dir in sorted(images_dir.iterdir()):
        if not image_type_dir.is_dir():
            continue
        image_type = image_type_dir.name
        if image_type not in IMAGE_TYPES:
            print(f"  Skipping unknown image type: {image_type}")
            continue

        image_files = sorted(
            f for f in image_type_dir.iterdir()
            if f.is_file() and f.suffix.lower() in {".png", ".jpg", ".jpeg"}
        )
        if not image_files:
            continue

        print(f"  Uploading {len(image_files)} image(s) for {image_type}...")

        # Delete existing images before uploading new ones
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
            print(f"    Uploaded: {image_file.name}")


def main():
    service = build_service()
    edit = service.edits().insert(packageName=PACKAGE_NAME, body={}).execute()
    edit_id = edit["id"]
    print(f"Opened edit: {edit_id}")

    try:
        upload_listing(service, edit_id)
        service.edits().commit(editId=edit_id, packageName=PACKAGE_NAME).execute()
        print("\nEdit committed successfully.")
    except Exception:
        service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
        raise


if __name__ == "__main__":
    main()
