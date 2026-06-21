# Branching Strategy

DrinkWaterApp follows a **Gitflow**-inspired workflow simplified for a solo/small-team Android project.

Versioning: https://semver.org/

---

## Branch Types

| Branch | Pattern | Purpose |
|--------|---------|---------|
| `main` | `main` | Production-ready code. Tagged with every release. |
| `develop` | `develop` | Integration branch. All features merge here first. |
| `feature/*` | `feature/<ticket-or-short-desc>` | New features and non-trivial changes. |
| `fix/*` | `fix/<ticket-or-short-desc>` | Bug fixes during active development. |
| `hotfix/*` | `hotfix/<ticket-or-short-desc>` | Emergency fixes on a released version. |
| `release/*` | `release/<version>` | Release stabilisation. Only bug fixes allowed. |
| `chore/*` | `chore/<short-desc>` | Housekeeping (deps, CI, docs) — no production logic. |

---

## Typical Feature Workflow

```
develop
  └── feature/alarm-exact-schedule   ← branch off develop
         │   (work, commits, local tests)
         └── PR → develop            ← squash or merge commit
```

1. **Branch off `develop`**
   ```bash
   git fetch origin
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Work in small, focused commits** following [Conventional Commits](https://www.conventionalcommits.org/).

3. **Run local checks before pushing**
   ```bash
   ./scripts/check-setup.sh         # verify toolchain
   ./gradlew detekt testDebugUnitTest
   ```

4. **Open a PR to `develop`**
   - Title: `type(scope): short description`
   - Fill in the PR template (see `.github/PULL_REQUEST_TEMPLATE.md` if present)
   - CI (`pr-check.yml`) runs automatically — all checks must pass

5. **Merge** — prefer "Squash and merge" to keep `develop` history clean.

---

## Release Workflow

See [release-process.md](release-process.md) for the full end-to-end guide.

```
develop ──── release/2.1.0 ──── main (tag v2.1.0)
                    │
              (bug fixes only)
                    │
              back-merge → develop
```

---

## Hotfix Workflow

For critical production bugs discovered after a release:

```bash
git checkout main
git pull origin main
git checkout -b hotfix/crash-on-startup
# … fix …
# PR → main  AND  PR → develop
```

After merging to `main`, tag the new patch release (`v2.0.1`) and back-merge to `develop`.

---

## Commit Message Convention

```
<type>(<scope>): <short summary>

[optional body]

[optional footer: Co-Authored-By, Fixes #123, etc.]
```

**Types:** `feat` · `fix` · `refactor` · `chore` · `test` · `docs` · `ci` · `perf`

**Scopes** (examples): `alarm` · `backup` · `sync` · `stats` · `history` · `today` · `kmp` · `di`

---

## Branch Protection Rules (recommended GitHub settings)

| Rule | `main` | `develop` |
|------|--------|-----------|
| Require PR before merging | ✅ | ✅ |
| Require status checks (CI) | ✅ | ✅ |
| Dismiss stale reviews on new push | ✅ | ✅ |
| Restrict direct push | ✅ | — |

---

## Naming Cheat Sheet

```
feature/stats-realtime-updates
feature/ios-ktor-client
fix/history-day-rollover
hotfix/alarm-crash-api34
release/2.1.0
chore/update-compose-bom
chore/renovate-deps
```
