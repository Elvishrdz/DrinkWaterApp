# Legal scripts — guide

Helper scripts for maintaining and shipping the app's legal documents (Privacy Policy & Terms of
Use). All scripts can be run from anywhere in the repo; each resolves the project root via git.

| Script                     | What it does                                                                               |
|----------------------------|--------------------------------------------------------------------------------------------|
| `deploy-legal-web.sh`      | Deploys the legal website (`legal/public/`) to Firebase Hosting.                           |
| `install-legal-skill.sh`   | Installs the `update-legal` Claude Code skill so the AI agent can propagate legal changes. |
| `uninstall-legal-skill.sh` | Removes the installed `update-legal` skill.                                                |

---

## `deploy-legal-web.sh`

Publishes the static legal site to Firebase Hosting (`firebase deploy --only hosting`).

```bash
./legal/scripts/deploy-legal-web.sh
```

Run this **on release day**, after the legal content + effective date have been updated and
reviewed. Requires the Firebase CLI to be installed and authenticated (`firebase login`).

---

## `install-legal-skill.sh`

Installs the **`update-legal`** Claude Code skill into the project.

```bash
./legal/scripts/install-legal-skill.sh
```

What it does:

- **Source of record:** `legal/agents/skills/update-legal/` (version-controlled, edit it here).
- **Install target:** `.claude/skills/update-legal/` — the directory Claude Code scans to discover
  project skills.
- The script copies the skill from the source of record into the install target (creating it if
  needed and overwriting any previous install).

After installing, **start a new Claude Code session** so the skill is picked up.

> Why a separate source-of-record copy? `.claude/skills/` is the runtime location Claude Code reads.
> Keeping the canonical copy under `legal/agents/skills/` makes the skill part of the legal tooling,
> reviewable in PRs, and re-installable on any machine/checkout with one command.

## `uninstall-legal-skill.sh`

Removes the installed skill from `.claude/skills/update-legal/`. The source of record under
`legal/agents/skills/` is left in place, so you can re-install at any time.

```bash
./legal/scripts/uninstall-legal-skill.sh
```

---

## Using the `update-legal` skill

Once installed, the skill teaches the AI agent how to roll out a legal change across **every** place
the policy lives, from one source of truth — so nothing drifts between releases.

**Trigger it** by describing the change in natural language, e.g.:

- "Update the legal for the release — we added a new analytics provider."
- "Bump the legal effective date to force re-acceptance."
- "Add the cloud backup section to the privacy policy and terms."
- Or invoke it directly: `/update-legal`.

**What the agent will do** (full detail in the skill itself):

1. Edit the **source of truth** first — `legal/source/privacypolicy/*` and
   `legal/source/termsofservice/*` (English is the master; Spanish & German are translations).
2. Propagate the change to the **website** `legal/public/index.html` (en/es/de blocks).
3. Propagate to the **in-app strings** —
   `compose/src/main/res/values{,-es,-de}/strings_privacy_policy.xml`.
4. Bump the **effective date** in `PrivacyPolicyTermsOfServiceConfig.kt` (this is what forces users
   to re-accept) and everywhere it is displayed.
5. **Validate** (XML well-formed, HTML well-formed, `:compose` compiles, no stale dates left).
6. On release day, **deploy** the website with `deploy-legal-web.sh`.

To change the skill's instructions, edit `legal/agents/skills/update-legal/SKILL.md`, then re-run
`install-legal-skill.sh` to push the update into `.claude/skills/`.
