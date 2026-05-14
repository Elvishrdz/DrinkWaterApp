#!/usr/bin/env bash
set -euo pipefail

read -rp "Path to .jks file: " JKS_PATH

if [[ ! -f "$JKS_PATH" ]]; then
  echo "Error: file not found: $JKS_PATH"
  exit 1
fi

read -rp "Output file (leave empty to print to stdout): " OUTPUT_FILE

RESULT=$(base64 -i "$JKS_PATH")

if [[ -n "$OUTPUT_FILE" ]]; then
  echo "$RESULT" > "$OUTPUT_FILE"
  echo "Base64 written to: $OUTPUT_FILE"
else
  echo "$RESULT"
fi
