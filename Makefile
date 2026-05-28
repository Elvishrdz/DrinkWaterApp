# ─────────────────────────────────────────────
# DrinkWaterApp – Makefile
# ─────────────────────────────────────────────
# Usage: make <target>
# Run   make help   to see all available targets
# ─────────────────────────────────────────────

APP_ID        := com.eahm.drinkwaterapp
APP_ID_DEBUG  := com.eahm.drinkwaterapp.dev
APP_ID_STAGING:= com.eahm.drinkwaterapp.staging
APP_ID_QA     := com.eahm.drinkwaterapp.qa
MODULE        := :compose
GRADLEW       := ./gradlew

.PHONY: help \
        build build-debug build-staging build-qa build-release bundle-release \
        install install-staging install-qa \
        clean clean-build \
        test test-all \
        open-debug open-staging open-qa open-release \
        clear-debug clear-staging clear-qa clear-release \
        uninstall-debug uninstall-staging uninstall-qa uninstall-release \
        logcat logcat-debug screenshot devices \
        new-feature update-listing deploy

# ─────────────────────────────────────────────
# Default
# ─────────────────────────────────────────────

help:
	@echo ""
	@echo "DrinkWaterApp – Available targets"
	@echo "──────────────────────────────────────────────────────"
	@echo ""
	@echo "  BUILD"
	@echo "    build             Build all variants"
	@echo "    build-debug       Assemble debug APK"
	@echo "    build-staging     Assemble staging APK"
	@echo "    build-qa          Assemble QA APK"
	@echo "    build-release     Assemble release APK"
	@echo "    bundle-release    Build release AAB (for Play Store)"
	@echo ""
	@echo "  INSTALL"
	@echo "    install           Install debug APK on connected device"
	@echo "    install-staging   Install staging APK on connected device"
	@echo "    install-qa        Install QA APK on connected device"
	@echo ""
	@echo "  CLEAN"
	@echo "    clean             Clean all build outputs"
	@echo "    clean-build       Clean then build debug"
	@echo ""
	@echo "  TESTING"
	@echo "    test              Run unit tests (debug)"
	@echo "    test-all          Run unit tests for all modules"
	@echo ""
	@echo "  ADB – LAUNCH"
	@echo "    open-debug        Launch debug app on device"
	@echo "    open-staging      Launch staging app on device"
	@echo "    open-qa           Launch QA app on device"
	@echo "    open-release      Launch release app on device"
	@echo ""
	@echo "  ADB – DATA"
	@echo "    clear-debug       Clear data for debug app"
	@echo "    clear-staging     Clear data for staging app"
	@echo "    clear-qa          Clear data for QA app"
	@echo "    clear-release     Clear data for release app"
	@echo ""
	@echo "  ADB – UNINSTALL"
	@echo "    uninstall-debug   Uninstall debug app"
	@echo "    uninstall-staging Uninstall staging app"
	@echo "    uninstall-qa      Uninstall QA app"
	@echo "    uninstall-release Uninstall release app"
	@echo ""
	@echo "  ADB – UTILITIES"
	@echo "    logcat            Show full logcat"
	@echo "    logcat-debug      Show logcat filtered by app tag"
	@echo "    screenshot        Capture screenshot from device"
	@echo "    devices           List connected ADB devices"
	@echo ""
	@echo "  SCRIPTS"
	@echo "    new-feature       Start a new dev feature branch"
	@echo "    update-listing    Update Play Store listing assets"
	@echo "    deploy            Deploy develop build to Play Store"
	@echo ""

# ─────────────────────────────────────────────
# Build
# ─────────────────────────────────────────────

build:
	$(GRADLEW) $(MODULE):assemble

build-debug:
	$(GRADLEW) $(MODULE):assembleDebug

build-staging:
	$(GRADLEW) $(MODULE):assembleStaging

build-qa:
	$(GRADLEW) $(MODULE):assembleQa

build-release:
	$(GRADLEW) $(MODULE):assembleRelease

bundle-release:
	$(GRADLEW) $(MODULE):bundleRelease

# ─────────────────────────────────────────────
# Install
# ─────────────────────────────────────────────

install:
	$(GRADLEW) $(MODULE):installDebug

install-staging:
	$(GRADLEW) $(MODULE):installStaging

install-qa:
	$(GRADLEW) $(MODULE):installQa

# ─────────────────────────────────────────────
# Clean
# ─────────────────────────────────────────────

clean:
	$(GRADLEW) clean

clean-build: clean build-debug

# ─────────────────────────────────────────────
# Testing
# ─────────────────────────────────────────────

test:
	$(GRADLEW) $(MODULE):testDebugUnitTest

test-all:
	$(GRADLEW) testDebugUnitTest

# ─────────────────────────────────────────────
# ADB – Launch app
# ─────────────────────────────────────────────

open-debug:
	adb shell monkey -p $(APP_ID_DEBUG) -c android.intent.category.LAUNCHER 1

open-staging:
	adb shell monkey -p $(APP_ID_STAGING) -c android.intent.category.LAUNCHER 1

open-qa:
	adb shell monkey -p $(APP_ID_QA) -c android.intent.category.LAUNCHER 1

open-release:
	adb shell monkey -p $(APP_ID) -c android.intent.category.LAUNCHER 1

# ─────────────────────────────────────────────
# ADB – Clear app data
# ─────────────────────────────────────────────

clear-debug:
	adb shell pm clear $(APP_ID_DEBUG)

clear-staging:
	adb shell pm clear $(APP_ID_STAGING)

clear-qa:
	adb shell pm clear $(APP_ID_QA)

clear-release:
	adb shell pm clear $(APP_ID)

# ─────────────────────────────────────────────
# ADB – Uninstall
# ─────────────────────────────────────────────

uninstall-debug:
	adb uninstall $(APP_ID_DEBUG)

uninstall-staging:
	adb uninstall $(APP_ID_STAGING)

uninstall-qa:
	adb uninstall $(APP_ID_QA)

uninstall-release:
	adb uninstall $(APP_ID)

# ─────────────────────────────────────────────
# ADB – Utilities
# ─────────────────────────────────────────────

logcat:
	adb logcat

logcat-debug:
	adb logcat --pid=$$(adb shell pidof -s $(APP_ID_DEBUG))

screenshot:
	adb exec-out screencap -p > screenshot_$$(date +%Y%m%d_%H%M%S).png
	@echo "Screenshot saved."

devices:
	adb devices -l

# ─────────────────────────────────────────────
# Scripts
# ─────────────────────────────────────────────

new-feature:
	@bash scripts/new_dev_feature.sh

update-listing:
	@bash scripts/update_listing.sh

deploy:
	@bash scripts/deploy_develop_to_play_store.sh
