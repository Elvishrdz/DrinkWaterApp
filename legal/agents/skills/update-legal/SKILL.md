---
name: update-legal
description: |
  Propagate a Privacy Policy / Terms of Use change across every place it lives in the DrinkWaterApp repo, from the single source of truth. Use whenever the legal text changes for a release: new tracking events, a new third-party SDK / data flow (e.g. cloud backup, a new analytics provider), a new permission, or a wording/date change that must force users to re-accept. Trigger on phrases like "update the legal", "new privacy policy", "update terms of use", "bump the legal/effective date", "force re-acceptance", "legal for the release", "propagate the legal", or "add <feature> to the privacy policy".
---

# Update Legal (Privacy Policy & Terms of Use)

The legal text lives in **four** places. They drift unless changed together. This skill keeps them
in lockstep, driven from one source of truth.

## The single source of truth

`legal/source/` is canonical. Everything else is a copy of it.

```
legal/source/
  legal-source-of-truth.md              # index / workflow note (read it first)
  privacypolicy/privacypolicy-en.md     # EN is the master; es/de are translations of it
  privacypolicy/privacypolicy-es.md
  privacypolicy/privacypolicy-de.md
  termsofservice/termsofservice-en.md
  termsofservice/termsofservice-es.md
  termsofservice/termsofservice-de.md
```

Heading levels in each file: `#` document title, `##` numbered sections, `###` sub-sections (e.g.
`### 2.7`). No "Part 1/2 —" prefixes (each policy is its own file).

## The four targets to keep in sync

1. **Source of truth** — `legal/source/...` (edit here FIRST).
2. **Website** — `legal/public/index.html`. One file, three language blocks selected by
   `data-lang="en|es|de"` (privacy block then terms block per language). HTML format: tables for
   permissions / sharing, `&mdash;`, `&rarr;`, `&nbsp;`, accented chars as entities in es, raw
   umlauts in de. Effective date is in a `<span>` near the top of each language's privacy AND terms
   block ("Effective date:" / "Vigente desde:" / "Gültig ab:").
3. **In-app strings** — `compose/src/main/res/values{,-es,-de}/strings_privacy_policy.xml`. The
   `:compose` module is the real application (`com.android.application`); the `:app` module's copies
   of this string are dead — **ignore `:app`**. Each file is ONE `privacy_policy_local_content`
   string: an HTML blob inside `<![CDATA["..."]]>`, blocks separated by `\n        `, rendered with
   `AnnotatedString.fromHtml`. Apostrophe escaping is inconsistent (`device\'s` vs raw `device's`) —
   match existing bytes when replacing; use raw apostrophes in NEW text. Privacy + Terms are both in
   this one string; the effective date appears twice (privacy header + terms header).
4. **Effective date constant** — `compose/.../core/config/PrivacyPolicyTermsOfServiceConfig.kt`
   `effectiveDate = LocalDate(year, month, day)`. **Bumping this is what forces users to
   re-accept.** Must equal the date shown on the website and in the in-app string.

## Steps

1. **Edit the source of truth first.** Apply the content change to the EN master files, then mirror
   it into the `-es` and `-de` translations (translate new/changed passages; keep section numbering
   identical across languages). If only the date changes, skip to step 4.
2. **Update the website** `legal/public/index.html` for all three language blocks (en, es, de) —
   privacy and terms. Keep section numbers stable: prefer adding a sub-section (e.g. `2.7`) over
   renumbering. Mirror new list items / table rows in every language. New third-party data flows
   need a row in the §5 sharing table and the §9 terms third-party list; Google API/Drive scopes
   need the "Limited Use" disclosure callout.
3. **Update the in-app strings** in the 3 compose `strings_privacy_policy.xml` files to match the
   website content (same sections, same wording), in the CDATA HTML format described above.
4. **Bump the effective date** to the release date in: the `effectiveDate` constant (step 4 target),
   AND every website `<span>` date (en/es/de × privacy/terms = 6 spans), AND the in-app strings
   (2 per file × 3 files). Date wording per locale: `Month DD, YYYY` (en), `DD de <mes> de YYYY`
   (es), `DD. <Monat> YYYY` (de).
5. **Validate** before declaring done:
    - `python3 -c "import xml.etree.ElementTree as ET; ET.parse('<file>')"` for each of the 3 XML
      files.
    - HTML well-formedness check on `index.html` (parse with `html.parser`, assert tag stack
      empties).
    - `./gradlew :compose:compileDebugKotlin -q` (the date constant compiles).
    - Grep that no stale old date remains in any of the four targets, and that new sections/anchors
      (e.g. `2.7`, the new event, the disclosure) appear in all three languages of web + app.
6. **Deploy the website on release day** (only when the user says to ship):
   `./legal/scripts/deploy-legal-web.sh` (runs `firebase deploy --only hosting`). The app strings
   ship with the normal app release.

## Guardrails

- Source of truth is authoritative: if web/app disagree with `legal/source/`, fix web/app, not the
  source.
- Adding a third-party SDK / data flow → it MUST appear in: privacy §2 (what's collected), §4 (how
  used), §5 (sharing), §6 (retention), §9 (security), and terms §9 (third-party). A Google API scope
  also requires the Google API Services User Data Policy "Limited Use" disclosure.
- The website header icon is the production launcher icon at `legal/public/app-icon.webp` (copied
  from `compose/src/main/res/mipmap-xxxhdpi/ic_launcher.webp`). Refresh it if the app icon changes.
- Translations: EN is the master. Keep es/de structurally identical (same section numbers, same
  list/table rows). Do not leave a language behind — a partial update is the failure mode this skill
  exists to prevent.
