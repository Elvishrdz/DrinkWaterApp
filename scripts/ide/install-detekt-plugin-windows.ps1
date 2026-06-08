# =============================================================================
# install-detekt-plugin-windows.ps1
#
# Downloads and installs the Detekt IntelliJ plugin for Android Studio
# on Windows. The plugin highlights Detekt violations inline as you type,
# using the same detekt.yml that the pre-commit hook enforces.
#
# No IDE interaction required — plugins are zip files extracted into the
# IDE's plugins directory. The plugin is active on the next IDE launch.
#
# Usage (PowerShell):
#   .\scripts\ide\install-detekt-plugin-windows.ps1
#
# Or from project root:
#   powershell -ExecutionPolicy Bypass -File scripts\ide\install-detekt-plugin-windows.ps1
#
# Idempotent: exits early if the plugin is already installed.
# Requires: Windows, PowerShell 5+, internet access
# =============================================================================

$ErrorActionPreference = "Stop"

$PASS  = "[OK]"
$SKIP  = "[--]"
$FAIL  = "[!!]"
$INFO  = "[..]"

# Numeric plugin ID from JetBrains Marketplace (plugins.jetbrains.com/plugin/10761-detekt)
$PLUGIN_NUMERIC_ID = "10761"
# Folder name the zip extracts to (as shipped by JetBrains)
$PLUGIN_DIR_NAME   = "Detekt IntelliJ Plugin"

# -----------------------------------------------------------------------------
# Locate the newest Android Studio plugins directory
# Plugin path on Windows: %APPDATA%\Google\AndroidStudio<version>\plugins
# -----------------------------------------------------------------------------
$AppData      = $env:APPDATA
$GoogleDir    = Join-Path $AppData "Google"

if (-not (Test-Path $GoogleDir)) {
    Write-Host "  $FAIL Android Studio support directory not found at:"
    Write-Host "     $GoogleDir"
    Write-Host "     Open Android Studio at least once, then re-run this script."
    exit 1
}

# Sort descending to pick the newest non-Preview version first
$PluginsDir = Get-ChildItem -Path $GoogleDir -Directory -Filter "AndroidStudio[0-9]*" |
    Sort-Object Name -Descending |
    Where-Object { Test-Path (Join-Path $_.FullName "plugins") } |
    Select-Object -First 1 |
    ForEach-Object { Join-Path $_.FullName "plugins" }

# Fallback: include Preview builds
if (-not $PluginsDir) {
    $PluginsDir = Get-ChildItem -Path $GoogleDir -Directory -Filter "AndroidStudio*" |
        Sort-Object Name -Descending |
        Where-Object { Test-Path (Join-Path $_.FullName "plugins") } |
        Select-Object -First 1 |
        ForEach-Object { Join-Path $_.FullName "plugins" }
}

if (-not $PluginsDir) {
    Write-Host "  $FAIL Could not find an Android Studio plugins directory under:"
    Write-Host "     $GoogleDir"
    exit 1
}

Write-Host "  $INFO Target plugins directory:"
Write-Host "       $PluginsDir"

# -----------------------------------------------------------------------------
# Idempotency check — already installed?
# -----------------------------------------------------------------------------
$PluginInstallDir = Join-Path $PluginsDir $PLUGIN_DIR_NAME
if (Test-Path $PluginInstallDir) {
    Write-Host "  $SKIP Detekt plugin already installed at:"
    Write-Host "       $PluginInstallDir"
    Write-Host "       To reinstall, run: scripts\ide\uninstall-detekt-plugin-windows.ps1"
    exit 0
}

# -----------------------------------------------------------------------------
# Resolve the latest stable release via JetBrains Marketplace JSON API
# -----------------------------------------------------------------------------
Write-Host ""
Write-Host "  Resolving latest stable release from JetBrains Marketplace..."

try {
    $ApiUrl  = "https://plugins.jetbrains.com/api/plugins/$PLUGIN_NUMERIC_ID/updates?size=1"
    $Updates = Invoke-RestMethod -Uri $ApiUrl -UseBasicParsing
} catch {
    Write-Host "  $FAIL Could not reach JetBrains Marketplace: $_"
    Write-Host "     Check your internet connection and try again."
    exit 1
}

if (-not $Updates -or -not $Updates[0].file) {
    Write-Host "  $FAIL No plugin updates returned from Marketplace."
    exit 1
}

$FilePath    = $Updates[0].file
$DownloadUrl = "https://plugins.jetbrains.com/files/$FilePath"
$ZipName     = Split-Path $FilePath -Leaf

Write-Host "  $INFO Version : $($Updates[0].version)"
Write-Host "  $INFO Download: $DownloadUrl"

# -----------------------------------------------------------------------------
# Download plugin zip to a temp directory
# -----------------------------------------------------------------------------
$TmpDir  = Join-Path $env:TEMP "detekt-plugin-install"
$ZipPath = Join-Path $TmpDir $ZipName

if (Test-Path $TmpDir) { Remove-Item $TmpDir -Recurse -Force }
New-Item -ItemType Directory -Path $TmpDir | Out-Null

Write-Host "  Downloading $ZipName..."
try {
    Invoke-WebRequest -Uri $DownloadUrl -OutFile $ZipPath -UseBasicParsing
} catch {
    Write-Host "  $FAIL Download failed: $_"
    Remove-Item $TmpDir -Recurse -Force
    exit 1
}

# Verify it is a valid zip
try {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::OpenRead($ZipPath).Dispose()
} catch {
    Write-Host "  $FAIL Downloaded file is not a valid zip archive."
    Remove-Item $TmpDir -Recurse -Force
    exit 1
}

# -----------------------------------------------------------------------------
# Extract into plugins directory
# -----------------------------------------------------------------------------
Write-Host "  Installing..."
[System.IO.Compression.ZipFile]::ExtractToDirectory($ZipPath, $PluginsDir)
Remove-Item $TmpDir -Recurse -Force

if (Test-Path $PluginInstallDir) {
    Write-Host "  $PASS Detekt plugin installed at:"
    Write-Host "       $PluginInstallDir"
} else {
    Write-Host "  $PASS Detekt plugin extracted into $PluginsDir"
}

Write-Host ""
Write-Host "  [!] Restart Android Studio to activate the plugin."
Write-Host "      Once active, Detekt violations will appear as inline underlines"
Write-Host "      using your project's detekt.yml rules."
Write-Host ""
