#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# test_new_dev_feature.sh
# Unit + integration tests for new_dev_feature.sh
# Usage: ./scripts/test_new_dev_feature.sh
# ──────────────────────────────────────────────────────────────────────────────

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAIN="$SCRIPT_DIR/new_dev_feature.sh"

if [[ ! -f "$MAIN" ]]; then
    echo "ERROR: $MAIN not found" >&2
    exit 1
fi

# ── Test framework ─────────────────────────────────────────────────────────────
PASS=0; FAIL=0

_pass() { printf "  \033[0;32m✓\033[0m  %s\n" "$1"; PASS=$(( PASS + 1 )); }
_fail() { printf "  \033[0;31m✗\033[0m  %s  →  %s\n" "$1" "$2"; FAIL=$(( FAIL + 1 )); }

assert_true() {
    local d="$1"; shift
    if "$@"; then _pass "$d"; else _fail "$d" "expected exit 0"; fi
}

assert_false() {
    local d="$1"; shift
    if ! "$@"; then _pass "$d"; else _fail "$d" "expected non-zero exit"; fi
}

assert_eq() {
    if [[ "$2" == "$3" ]]; then _pass "$1"; else _fail "$1" "expected='$2'  actual='$3'"; fi
}

# ── Load functions without running main ────────────────────────────────────────
# shellcheck source=new_dev_feature.sh
source "$MAIN"
set +e  # main script enables -e; disable it so test failures don't abort the runner

# ──────────────────────────────────────────────────────────────────────────────
printf "\n\033[1mvalidate_name\033[0m\n"
assert_true  "Hello"               validate_name "Hello"
assert_true  "Hello123"            validate_name "Hello123"
assert_true  "HelloWorld"          validate_name "HelloWorld"
assert_false "hello (lowercase)"   validate_name "hello"
assert_false "1Hello (digit first)" validate_name "1Hello"
assert_false "empty string"         validate_name ""
assert_false "Hello-World (hyphen)" validate_name "Hello-World"
assert_false "Hello_World (under)" validate_name "Hello_World"
assert_false "Hello World (space)" validate_name "Hello World"

# ──────────────────────────────────────────────────────────────────────────────
printf "\n\033[1mlower\033[0m\n"
assert_eq "y stays y"   "y"   "$(lower 'y')"
assert_eq "Y → y"       "y"   "$(lower 'Y')"
assert_eq "YES → yes"   "yes" "$(lower 'YES')"
assert_eq "No → no"     "no"  "$(lower 'No')"

# ──────────────────────────────────────────────────────────────────────────────
printf "\n\033[1mask_yn\033[0m\n"
# Pipe each test input into a subshell so read gets it from stdin
yn() { printf '%s\n' "$1" | (source "$MAIN"; ask_yn "prompt" "${2:-y}") 2>/dev/null; }

assert_true  "y → true"          yn "y"
assert_true  "Y → true"          yn "Y"
assert_true  "yes → true"        yn "yes"
assert_true  "Yes → true"        yn "Yes"
assert_true  "YES → true"        yn "YES"
assert_false "n → false"         yn "n"
assert_false "N → false"         yn "N"
assert_false "no → false"        yn "no"
assert_false "No → false"        yn "No"
assert_true  "empty default=y"   yn "" "y"
assert_false "empty default=n"   yn "" "n"

# ──────────────────────────────────────────────────────────────────────────────
printf "\n\033[1mpick_icon\033[0m\n"
# tail -1 isolates the returned icon name from the printed menu
icon_pick() { printf '%s\n' "$@" | (source "$MAIN"; pick_icon) 2>/dev/null | tail -1; }

assert_eq "number 1 → Drop"        "Drop"     "$(icon_pick 1)"
assert_eq "number 5 → Star"        "Star"     "$(icon_pick 5)"
assert_eq "name bell → Bell"       "Bell"     "$(icon_pick bell)"
assert_eq "name CLOCK → Clock"     "Clock"    "$(icon_pick CLOCK)"
assert_eq "bad input retries → 2"  "Wave"     "$(icon_pick notanicon 2)"
assert_eq "bad input retries name" "Drop"     "$(icon_pick '???' drop)"

# ──────────────────────────────────────────────────────────────────────────────
# Integration tests: feed all prompts via stdin, answer n at final confirm
# so no files are ever created on disk.
printf "\n\033[1mintegration\033[0m\n"

_check() {
    local desc="$1" pattern="$2" output="$3"
    if printf '%s' "$output" | grep -q "$pattern"; then _pass "$desc"
    else _fail "$desc" "pattern '$pattern' not found"; fi
}

# Test 1: valid inputs, abort at final confirm
out=$(printf '%s\n' \
    "TestFeature" \
    "Test Label"  \
    "1"           \
    ""            \
    "n"           \
    "n"           \
    | bash "$MAIN" 2>&1) || true
_check "valid flow reaches Preview" "Preview" "$out"
_check "valid flow aborts cleanly"  "Aborted" "$out"

# Test 2: invalid name given twice, retries without exiting, then valid
out=$(printf '%s\n' \
    "bad-name"    \
    "123Bad"      \
    "TestFeature" \
    "Test Label"  \
    "1"           \
    ""            \
    "n"           \
    "n"           \
    | bash "$MAIN" 2>&1) || true
_check "invalid name shows warning"   "uppercase" "$out"
_check "retried valid name → Preview" "Preview"   "$out"
_check "aborts cleanly after retry"   "Aborted"   "$out"

# Test 3: invalid icon retries without exiting, then valid
out=$(printf '%s\n' \
    "TestFeature" \
    "Test Label"  \
    "notvalid"    \
    "bell"        \
    ""            \
    "n"           \
    "n"           \
    | bash "$MAIN" 2>&1) || true
_check "invalid icon triggers warning" "Not found" "$out"
_check "valid icon → aborts cleanly"   "Aborted"   "$out"

# ── Summary ────────────────────────────────────────────────────────────────────
printf "\n\033[2m──────────────────────────────────\033[0m\n"
printf "  %d passed  %d failed\n" "$PASS" "$FAIL"
printf "\033[2m──────────────────────────────────\033[0m\n\n"
(( FAIL == 0 )) || exit 1
