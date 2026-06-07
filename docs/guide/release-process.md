# Release Process

End-to-end guide for cutting a production release of DrinkWaterApp.

---

## Overview

```
develop  →  release/X.Y.Z  →  main (tag vX.Y.Z)  →  Play Store
                │
          (only bug fixes)
                │
          back-merge → develop
```

---

## 1. Create the Release Branch

Start from an up-to-date `develop`:

```bash
git fetch origin
git checkout develop && git pull origin develop

# Use semantic versioning: MAJOR.MINOR.PATCH
git checkout -b release/2.1.0
git push -u origin release/2.1.0
```

CI (`release-check.yml`) fires automatically on push to `release/*`.

---

## 2. Stabilise the Release

- **Only bug fixes** go on the release branch — no new features.
- Branch fix commits off `release/X.Y.Z`, not `develop`:
  ```bash
  git checkout -b fix/crash-on-samsung release/2.1.0
  # … fix, PR back into release/X.Y.Z …
  ```
- Re-run CI checks after every fix commit.

---

## 3. Update Version Numbers

In `compose/build.gradle.kts` (or the root `build.gradle.kts` if version is defined there):

```kotlin
android {
    defaultConfig {
        versionCode = 210          // increment by 1 for every Play Store upload
        versionName = "2.1.0"
    }
}
```

Commit the bump:
```bash
git add compose/build.gradle.kts
git commit -m "chore(release): bump version to 2.1.0 (versionCode 210)"
```

---

## 4. Pre-Release Checklist

Run through [`docs/release/2.1.0.md`](../release/2.1.0.md) (or the current version's checklist):

- [ ] All CI checks green on `release/X.Y.Z`
- [ ] Unit tests pass (`./gradlew testDebugUnitTest --continue`)
- [ ] Detekt clean (`./gradlew detekt`)
- [ ] Manual smoke-test on device (install debug build, verify core flows)
- [ ] Verify translations — no missing strings
- [ ] Screenshots updated in Play Console if UI changed
- [ ] Release notes (`docs/release/X.Y.Z.md`) written in EN, DE, ES
- [ ] Play Console: internal track upload → promote to production

---

## 5. Merge to `main` and Tag

```bash
# PR: release/2.1.0 → main
# After merge:
git checkout main && git pull origin main
git tag -a v2.1.0 -m "Release 2.1.0"
git push origin v2.1.0
```

---

## 6. Back-Merge to `develop`

```bash
# PR: release/2.1.0 → develop
# Or direct merge if no conflicts:
git checkout develop && git pull origin develop
git merge --no-ff release/2.1.0 -m "chore: back-merge release/2.1.0 into develop"
git push origin develop
```

---

## 7. Play Console Publish Steps

1. Build a **signed release AAB**:
   ```bash
   ./gradlew :compose:bundleRelease
   # Output: compose/build/outputs/bundle/release/compose-release.aab
   ```
2. Upload to **Internal Testing** track → install and smoke-test on a real device.
3. Promote to **Production** (staged rollout: 20% → 50% → 100%).
4. Monitor crash-free sessions and ANR rate in Play Console for 24 h after each rollout stage.

---

## 8. Hotfix Process (post-release)

If a critical bug is found after the release tag:

```bash
git checkout -b hotfix/2.1.1 v2.1.0   # branch from the tag
# fix, bump versionCode/versionName, PR → main, then PR → develop
git tag -a v2.1.1 -m "Hotfix 2.1.1"
```

---

## Naming & Version Conventions

| Scenario | `versionName` | `versionCode` |
|----------|--------------|--------------|
| Feature release | `2.1.0` | +10 (e.g. 210) |
| Hotfix | `2.1.1` | +1 (e.g. 211) |
| Major | `3.0.0` | +100 (e.g. 300) |

> Keep `versionCode` strictly increasing — Play Store rejects uploads with a lower or equal code.

---

## Related Docs

- [Branching Strategy](branching-strategy.md)
- [Detekt Guide](detekt-guide.md)
- [Engineering Practices](engineering-practices.md)
