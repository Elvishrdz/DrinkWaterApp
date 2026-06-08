# =============================================================================
# uninstall-detekt-plugin-windows.ps1
#
# Removes the Detekt IntelliJ plugin from Android Studio's plugins directory
# on Windows.
#
# Usage (PowerShell):
#   .\scripts\ide\uninstall-detekt-plugin-windows.ps1
#
# Or from project root:
#   powershell -ExecutionPolicy Bypass -File scripts\ide\uninstall-detekt-plugin-windows.ps1
#
# Idempotent: exits cleanly if the plugin is not installed.
# Requires: Windows, PowerShell 5+
# =============================================================================

$ErrorActionPreference = "Stop"

$PASS = "[OK]"
$SKIP = "[--]"
$FAIL = "[!!]"
$INFO = "[..]"

$PLUGIN_DIR_NAME = "Detekt IntelliJ Plugin"

# -----------------------------------------------------------------------------
# Locate the newest Android Studio plugins directory
# -----------------------------------------------------------------------------
$GoogleDir = Join-Path $env:APPDATA "Google"

$PluginsDir = Get-ChildItem -Path $GoogleDir -Directory -Filter "AndroidStudio[0-9]*" -ErrorAction SilentlyContinue |
    Sort-Object Name -Descending |
    Where-Object { Test-Path (Join-Path $_.FullName "plugins") } |
    Select-Object -First 1 |
    ForEach-Object { Join-Path $_.FullName "plugins" }

if (-not $PluginsDir) {
    $PluginsDir = Get-ChildItem -Path $GoogleDir -Directory -Filter "AndroidStudio*" -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Where-Object { Test-Path (Join-Path $_.FullName "plugins") } |
        Select-Object -First 1 |
        ForEach-Object { Join-Path $_.FullName "plugins" }
}

if (-not $PluginsDir) {
    Write-Host "  $SKIP No Android Studio plugins directory found — nothing to uninstall."
    exit 0
}

# -----------------------------------------------------------------------------
# Remove plugin directory if present
# -----------------------------------------------------------------------------
$PluginInstallDir = Join-Path $PluginsDir $PLUGIN_DIR_NAME

if (-not (Test-Path $PluginInstallDir)) {
    Write-Host "  $SKIP Detekt plugin not found at:"
    Write-Host "       $PluginInstallDir"
    Write-Host "       Nothing to remove."
    exit 0
}

Write-Host "  $INFO Removing: $PluginInstallDir"
Remove-Item $PluginInstallDir -Recurse -Force
Write-Host "  $PASS Detekt plugin uninstalled."
Write-Host ""
Write-Host "  [!] Restart Android Studio for the change to take effect."
Write-Host ""
