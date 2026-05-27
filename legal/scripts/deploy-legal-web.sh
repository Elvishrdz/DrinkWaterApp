#!/usr/bin/env bash
# Deploys the legal static site (public/index.html) to Firebase Hosting.
# Run from anywhere: ./legal/scripts/deploy-legal-web.sh

# Exit immediately if any command fails, treat unset variables as errors,
# and propagate pipe failures.
set -euo pipefail

# - Paths
# Directory where this script lives (legal/scripts/).
# BASH_SOURCE[0] is more reliable than $0 inside bash.
SCRIPTS_DIR="$(dirname "${BASH_SOURCE[0]}")"

# 2>/dev/null suppresses the 'shell-init: error retrieving current directory'
# warning that bash emits when the caller's CWD is stale or no longer accessible.
PROJECT_ROOT="$(git -C "$SCRIPTS_DIR" rev-parse --show-toplevel)" 2>/dev/null

LEGAL_DIR="$PROJECT_ROOT/legal"

# - Validation
[[ -d "$LEGAL_DIR" ]] || { echo "Error: $LEGAL_DIR not found." >&2; exit 1; }

# - Deploy
echo "Deploying legal site to Firebase Hosting..."

# 2>/dev/null suppresses the 'chdir: error retrieving current directory'
# warning that bash prints when its internal CWD tracking is stale.
cd "$LEGAL_DIR" 2>/dev/null

firebase deploy --only hosting
