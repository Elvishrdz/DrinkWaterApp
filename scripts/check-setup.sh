#!/usr/bin/env bash
# =============================================================================
# check-setup.sh — DrinkWaterApp project analyzer
#
# Verifies that all required development tools and project configurations are
# in place before starting work on the repository. Run this after cloning.
#
# Usage:
#   ./scripts/check-setup.sh
#
# Exit code:
#   0 — all checks passed
#   1 — one or more checks failed
# =============================================================================

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PASS="✅"
FAIL="❌"
WARN="⚠️ "
errors=0

info()  { echo "   $*"; }
ok()    { echo "  $PASS $*"; }
warn()  { echo "  $WARN $*"; }
fail()  { echo "  $FAIL $*"; errors=$((errors + 1)); }

section() {
    echo ""
    echo "▸ $*"
}

# -----------------------------------------------------------------------------
section "Git"
# -----------------------------------------------------------------------------

if git -C "$ROOT" rev-parse --git-dir &>/dev/null; then
    ok "Inside a git repository"
else
    fail "Not a git repository — run 'git init' or clone the repo first"
fi

# Git hooks
HOOKS_DIR="$ROOT/.git/hooks"
HOOKS_SRC="$ROOT/scripts/git/hooks"

check_hook() {
    local name="$1"
    local target="$HOOKS_DIR/$name"
    if [[ -x "$target" ]]; then
        if [[ -L "$target" ]]; then
            ok "git hook '$name' installed (symlink → $(readlink "$target"))"
        else
            ok "git hook '$name' installed (standalone)"
        fi
    else
        fail "git hook '$name' not found or not executable — run: scripts/git/install_git_hooks.sh"
    fi
}

check_hook "pre-commit"

# -----------------------------------------------------------------------------
section "JDK"
# -----------------------------------------------------------------------------

if command -v java &>/dev/null; then
    java_ver=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [[ "$java_ver" -ge 17 ]]; then
        ok "Java $java_ver found"
    else
        fail "Java 17+ required, found Java $java_ver"
    fi
else
    fail "java not found — install JDK 17 (e.g. temurin via sdkman or homebrew)"
fi

# -----------------------------------------------------------------------------
section "Android SDK / Gradle"
# -----------------------------------------------------------------------------

if [[ -f "$ROOT/gradlew" ]]; then
    ok "gradlew wrapper present"
else
    fail "gradlew not found — is this the correct repository root?"
fi

if [[ -f "$ROOT/local.properties" ]]; then
    if grep -q "sdk.dir" "$ROOT/local.properties"; then
        ok "local.properties with sdk.dir found"
    else
        warn "local.properties exists but sdk.dir not set — Android Studio should set this automatically"
    fi
else
    warn "local.properties missing — Android Studio will create it on first open"
fi

# -----------------------------------------------------------------------------
section "Detekt"
# -----------------------------------------------------------------------------

if grep -r "detekt" "$ROOT/build.gradle.kts" &>/dev/null 2>&1 || \
   grep -r "detekt" "$ROOT/gradle/libs.versions.toml" &>/dev/null 2>&1; then
    ok "Detekt dependency configured"
else
    fail "Detekt not found in build files"
fi

if [[ -f "$ROOT/detekt.yml" ]] || [[ -f "$ROOT/config/detekt/detekt.yml" ]]; then
    ok "Detekt config file present"
else
    warn "No detekt.yml found at root or config/detekt/ — detekt will use defaults"
fi

# Check each module has a baseline (warn if missing, not error)
for module_dir in "$ROOT"/*/; do
    module=$(basename "$module_dir")
    baseline="$module_dir/detekt-baseline.xml"
    if [[ -f "$baseline" ]]; then
        ok "Detekt baseline present for :$module"
    else
        # Only warn for modules that have kotlin sources
        if find "$module_dir" -name "*.kt" -maxdepth 5 | grep -q .; then
            warn "No detekt-baseline.xml for :$module (run: ./gradlew :${module}:detektBaseline)"
        fi
    fi
done

# -----------------------------------------------------------------------------
section "IDE Configuration"
# -----------------------------------------------------------------------------

EDITORCONFIG="$ROOT/.editorconfig"
if [[ -f "$EDITORCONFIG" ]]; then
    ok ".editorconfig present"
else
    warn ".editorconfig not found — consider adding one for consistent formatting across IDEs"
fi

IDEA_DIR="$ROOT/.idea"
if [[ -d "$IDEA_DIR" ]]; then
    ok ".idea directory present"
    # Check code style config
    if [[ -f "$IDEA_DIR/codeStyles/codeStyleConfig.xml" ]] || \
       [[ -f "$IDEA_DIR/codeStyle.xml" ]]; then
        ok "IntelliJ/AS code style config found"
    else
        warn "No IntelliJ/AS code style config in .idea/ — import code style from project settings"
    fi
else
    warn ".idea directory not found — open project in Android Studio first"
fi

# -----------------------------------------------------------------------------
section "IDE Tools (format-on-save & Detekt plugin)"
# -----------------------------------------------------------------------------

# Format-on-save: check workspace.xml for FormatOnSaveOptions component
WORKSPACE="$ROOT/.idea/workspace.xml"
if [[ -f "$WORKSPACE" ]] && grep -q "FormatOnSaveOptions" "$WORKSPACE" 2>/dev/null; then
    ok "Format-on-save enabled (Reformat code + Optimize imports on ⌘S)"
else
    warn "Format-on-save not configured"
    info "Run: bash scripts/ide/install-format-on-save.sh"
fi

# Detekt IntelliJ plugin: check user plugins directory (macOS + Windows via Git Bash/WSL)
OS="$(uname)"
DETEKT_FOUND=false
DETEKT_INSTALL_CMD=""

if [[ "$OS" == "Darwin" ]]; then
    AS_SUPPORT_DIR="$HOME/Library/Application Support/Google"
    DETEKT_INSTALL_CMD="bash scripts/ide/install-detekt-plugin.sh"
    while IFS= read -r dir; do
        if [[ -d "$dir/plugins/Detekt IntelliJ Plugin" ]]; then
            DETEKT_FOUND=true
            break
        fi
    done < <(ls -dt "$AS_SUPPORT_DIR"/AndroidStudio* 2>/dev/null)
elif [[ "$OS" == MINGW* || "$OS" == CYGWIN* || "$OS" == MSYS* ]]; then
    # Git Bash / WSL on Windows — APPDATA is set by Git Bash
    AS_SUPPORT_DIR="${APPDATA}/Google"
    DETEKT_INSTALL_CMD="powershell -ExecutionPolicy Bypass -File scripts\\ide\\install-detekt-plugin-windows.ps1"
    while IFS= read -r dir; do
        if [[ -d "$dir/plugins/Detekt IntelliJ Plugin" ]]; then
            DETEKT_FOUND=true
            break
        fi
    done < <(ls -dt "$AS_SUPPORT_DIR"/AndroidStudio* 2>/dev/null)
fi

if [[ -n "$DETEKT_INSTALL_CMD" ]]; then
    if $DETEKT_FOUND; then
        ok "Detekt IntelliJ plugin installed (inline violation highlighting)"
    else
        warn "Detekt IntelliJ plugin not found"
        info "Run: $DETEKT_INSTALL_CMD"
    fi
fi

# -----------------------------------------------------------------------------
section "Scripts"
# -----------------------------------------------------------------------------

if [[ -x "$ROOT/scripts/git/install_git_hooks.sh" ]]; then
    ok "install_git_hooks.sh is executable"
else
    warn "scripts/git/install_git_hooks.sh not executable or missing"
fi

# -----------------------------------------------------------------------------
section "CI/CD Files"
# -----------------------------------------------------------------------------

for wf in pr-check.yml release-check.yml; do
    if [[ -f "$ROOT/.github/workflows/$wf" ]]; then
        ok "GitHub Actions workflow: $wf"
    else
        fail "Missing workflow: .github/workflows/$wf"
    fi
done

if [[ -f "$ROOT/.github/CODEOWNERS" ]]; then
    ok "CODEOWNERS file present"
else
    warn "CODEOWNERS missing — consider adding .github/CODEOWNERS"
fi

# -----------------------------------------------------------------------------
section "Summary"
# -----------------------------------------------------------------------------
echo ""
if [[ $errors -eq 0 ]]; then
    echo "  $PASS All checks passed — you're good to go!"
else
    echo "  $FAIL $errors check(s) failed — fix the issues above before starting work."
    exit 1
fi
