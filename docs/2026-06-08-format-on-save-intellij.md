# Format-on-Save in IntelliJ / Android Studio

**Date:** 2026-06-08  
**Status:** Plan — approved for implementation

---

## Conclusion

**Yes — including the exact formatting you apply manually with `⌥⌘L`.**

IntelliJ IDEA 2021.2+ (and all recent Android Studio releases) have a built-in
**Actions on Save** feature. When enabled it fires automatically on every `⌘S`,
running the same operations as the keyboard shortcuts:

| Shortcut you press today | Actions on Save equivalent                          |
|--------------------------|-----------------------------------------------------|
| `⌥⌘L` — Reformat Code    | ✅ **Reformat code** — this is the main one you want |
| `⌃⌥O` — Optimize Imports | ✅ Optimize imports                                  |

So yes: saving a file will be identical to pressing `⌥⌘L` + `⌃⌥O` on it. You
will never need to format manually again.

The only friction is that these settings live in `.idea/workspace.xml`, which is
**gitignored** in this repo (and in most Android repos). A committed script is
therefore the right delivery mechanism.

---

## How It Works Internally

IntelliJ stores the Actions on Save state as an XML component inside
`.idea/workspace.xml`:

```xml

<component name="FormatOnSaveOptions">
    <option name="myRunOnSaveEnabled" value="true" />
    <option name="myFormatOnlyChangedLines" value="false" />
    <option name="myOptimizeImports" value="true" />
</component>
```

No plugin is required. The script injects or removes that block using a proper
XML parser — **not `sed` or line-matching** — so it is position-independent and
safe regardless of how the rest of the file changes over time. See the
*Script Robustness* section below for details.

---

## Proposed Deliverables

### 1. `scripts/install-format-on-save.sh`

- Detects whether `.idea/workspace.xml` exists; creates a minimal valid one if not.
- Parses the file as XML (via `python3 xml.etree.ElementTree` — stdlib, no extra deps).
- Searches for `<component name="FormatOnSaveOptions">` **by attribute**, not by line number.
- If found → exits with "already installed" message. No duplicate, no corruption.
- If not found → appends the component as a proper XML element and serialises the tree back to disk.
- Prints a restart reminder.

### 2. `scripts/uninstall-format-on-save.sh`

- Parses `workspace.xml` as XML.
- Finds `<component name="FormatOnSaveOptions">` **by attribute** and removes the element from the tree.
- If not found → exits with "not installed" message. No error, no side effects.
- Serialises the remaining tree back to disk untouched.
- Prints a restart reminder.

---

## Script Robustness

This is the most important implementation detail. The scripts **must use an XML
parser**, not `sed`, `grep`, or line-number tricks.

### Why `sed` would be fragile

`workspace.xml` is a living file — the IDE rewrites it on every launch, adding,
removing, and reordering `<component>` blocks freely. A `sed` pattern like
`/FormatOnSaveOptions/,/\/component>/d` would break if:

- The block moves to a different position in the file
- The IDE adds whitespace or attribute ordering changes
- A future IDE version renames or restructures the block

### Why an XML parser is safe

`python3`'s `xml.etree.ElementTree` is part of the standard library (no install
needed) and operates on the tree structure, not on text lines:

```python
import xml.etree.ElementTree as ET

tree = ET.parse(".idea/workspace.xml")
root = tree.getroot()

# Find by attribute — position-independent
existing = root.find(".//component[@name='FormatOnSaveOptions']")

# Install: only add if absent
if existing is None:
    component = ET.SubElement(root, "component", name="FormatOnSaveOptions")
    ET.SubElement(component, "option", name="myRunOnSaveEnabled",       value="true")
    ET.SubElement(component, "option", name="myFormatOnlyChangedLines", value="false")
    ET.SubElement(component, "option", name="myOptimizeImports",        value="true")
    tree.write(".idea/workspace.xml", encoding="utf-8", xml_declaration=True)

# Uninstall: only remove if present
if existing is not None:
    root.remove(existing)
    tree.write(".idea/workspace.xml", encoding="utf-8", xml_declaration=True)
```

This means:
- **Install is idempotent** — running it twice has no effect after the first run
- **Uninstall is safe** — only the target element is removed; nothing else is touched
- **Order doesn't matter** — the block can be anywhere in the file
- **Future-proof** — works regardless of what the IDE adds around it

### 3. Hook into the existing quality-setup script

The repo already has a Detekt pre-commit hook. The format-on-save install should
be offered (not forced) during the same onboarding flow — e.g.:

```bash
# inside scripts/setup-code-quality.sh (or equivalent)
echo ""
echo "Install format-on-save for IntelliJ/Android Studio? [y/N]"
read -r answer
if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
  bash "$(dirname "$0")/install-format-on-save.sh"
fi
```

---

## Scope & Behaviour

| Setting                   | Value      | Reason                                    |
|---------------------------|------------|-------------------------------------------|
| Run on save               | ✅ enabled  | core requirement                          |
| Format changed lines only | ❌ disabled | whole-file formatting keeps history clean |
| Optimize imports          | ✅ enabled  | removes unused imports on every save      |

---

## Caveats

| Concern                     | Detail                                                                                                                 |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------|
| IDE restart needed          | workspace.xml is read at startup; changes don't apply to a running IDE                                                 |
| Per-machine, not per-commit | each developer runs the install script once; it is not enforced by CI                                                  |
| Android Studio vs IntelliJ  | both use the same component name — script works for both                                                               |
| workspace.xml may not exist | the script must handle the case where `.idea/` exists but `workspace.xml` does not (fresh clone before first IDE open) |
| Conflict with Detekt        | Detekt runs on commit; format-on-save runs on every save — they complement each other, no conflict                     |

---

## Files to Create / Modify

```
scripts/
  install-format-on-save.sh     ← new
  uninstall-format-on-save.sh   ← new
  install-detekt-plugin.sh      ← new
  uninstall-detekt-plugin.sh    ← new
  setup-code-quality.sh         ← modify: add optional prompts for both installs
```

---

## Can Detekt Run on Save?

**Yes — but through a different mechanism, not Actions on Save.**

Running `./gradlew detekt` on every save would be too slow (Gradle startup alone
takes several seconds). The correct tool is the
**[Detekt IntelliJ Plugin](https://plugins.jetbrains.com/plugin/10802-detekt)**,
which embeds the Detekt engine directly into the IDE and highlights violations
**inline as you type** — the same way the IDE shows red/yellow underlines for
compiler errors.

### How it works

- Violations appear as squiggly underlines in the editor, with the rule name in
  the gutter.
- Hovering shows the description and a quick-fix if one is available.
- No Gradle run needed — analysis is near-instant.
- It reads your existing `config/detekt/detekt.yml` automatically, so the rules
  match what the pre-commit hook enforces.

### Can this be scripted? Yes — plugins are just zip files

JetBrains plugins are zip archives extracted into a known directory. No IDE
interaction is required — the script downloads, extracts, and the plugin is
active on the next IDE launch.

**Plugin directory on macOS** (confirmed for this machine):
```
~/Library/Application Support/Google/AndroidStudio<version>/plugins/
```

The script needs to:
1. Find the latest `AndroidStudio*` folder under `~/Library/Application Support/Google/`
2. Read the IDE build number from `~/Applications/Android Studio.app/Contents/Resources/product-info.json`
3. Call the JetBrains Marketplace API to get the correct plugin download URL for that build
4. Download + unzip into the plugins directory
5. Print a restart reminder

**Marketplace API pattern:**
```
https://plugins.jetbrains.com/pluginManager?action=download&id=detekt-intellij-plugin&build=AI-<buildNumber>
```

**Uninstall:** delete the extracted plugin folder (`plugins/detekt-intellij-plugin/`).

#### Caveats

| Concern | Detail |
|---|---|
| IDE must be closed | Installing while Android Studio is open won't error, but the plugin won't load until restart |
| Build number matching | The Marketplace API picks the newest plugin compatible with the given build — generally safe, but pin a version if stability matters |
| Multiple AS versions | Script should target the *newest* installed version; older ones can be ignored |
| macOS only | Plugin dir path differs on Linux/Windows — script should guard with `uname` check |

### What to add to the setup script

```bash
echo ""
echo "Install Detekt IntelliJ plugin for real-time violation highlighting? [y/N]"
read -r answer
if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
  bash "$(dirname "$0")/install-detekt-plugin.sh"
fi
```

### Plugin vs pre-commit hook — complementary roles

| When | Tool | Purpose |
|---|---|---|
| While typing / on save | Detekt IntelliJ Plugin | See violations immediately, fix before committing |
| On `git commit` | Detekt pre-commit hook | Hard gate — blocks bad code from entering history |
| On CI | `./gradlew detekt` | Source of truth for the whole project |

**Recommendation:** add the plugin install reminder to the setup script, and add
it to the team's onboarding docs. It dramatically reduces pre-commit hook
failures since developers see issues the moment they write them.

---

## Additional Actions on Save — Suggestions

IntelliJ's Actions on Save panel offers more than just format + imports.
Below is a curated list of what's worth enabling for an Android/Kotlin project,
ordered from highest to lowest recommendation.

| Action                        | Recommendation       | Notes                                                                                                                                                                            |
|-------------------------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Reformat code**             | ✅ Enable (core goal) | Equivalent to `⌥⌘L`; keeps every saved file consistently formatted                                                                                                               |
| **Optimize imports**          | ✅ Enable (core goal) | Equivalent to `⌃⌥O`; removes unused imports automatically                                                                                                                        |
| **Run code cleanup**          | ⚠️ Optional          | Applies the IDE's configured inspections with auto-fix (e.g. replace `it` with explicit param, remove redundant `return`). More aggressive — review the inspection profile first |
| **Rearrange code**            | ⚠️ Optional          | Reorders class members according to the arrangement rules in Settings → Code Style. Useful for consistency but creates noisy diffs if the team hasn't agreed on an order yet     |
| **Run eslint/prettier** (N/A) | ❌ Skip               | JS/TS only; not applicable here                                                                                                                                                  |

### What's handled better elsewhere

| Concern                            | Better tool                                 |
|------------------------------------|---------------------------------------------|
| Catching real bugs / anti-patterns | Detekt (already configured, runs on commit) |
| Enforcing naming / style rules     | Detekt rules + `.editorconfig`              |
| Null-safety & coroutine misuse     | Kotlin compiler + lint                      |

**Recommended starting config:** enable only *Reformat code* + *Optimize imports*.
Add *Run code cleanup* later once the team has reviewed and agreed on the
inspection profile — it can change logic, not just style.

---

## Out of Scope

- Enforcing formatting via CI (Detekt + `--auto-correct` covers that separately)
- Configuring code style rules (`.editorconfig` / `codeStyleConfig.xml`) — separate concern
