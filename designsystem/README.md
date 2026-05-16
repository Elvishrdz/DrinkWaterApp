# Drink Water — Design System

A modernized recreation of the **Drink Water** Android app
([Elvishrdz/android_drink_water_app](https://github.com/Elvishrdz/android_drink_water_app)) — a
hydration tracker that calculates a daily goal from your weight and reminds you
to drink at intervals throughout the day. The original is a 2018-era Android
app; this design system refreshes its visual language while staying faithful
to the product's tone, palette, and core flows.

---

## Product context

- **What it does.** Calculates a personal daily water goal from the user's
  weight (Pounds or Kilograms), then nudges them with notifications at a
  configurable interval to log drinks until they hit the goal.
- **Core flows.** Onboarding (welcome → weight → ready) · Home (drink
  CTA + progress + settings strip) · Records (today's log, with yesterday
  available) · Notifications (system push with Skip / Drink actions).
- **Voice.** Friendly, plain-spoken, slightly informal ("You are welcome",
  "false alarm", "It's your turn to hydrate"). The original is bilingual
  English/Spanish — this kit is presented in English but copy is short and
  swappable.

## Visual foundations

**Palette.** Two brand families:
- **Water blues** — `#0E5A82 → #1C8EBF → #4FB3DD` — used for hero
  backgrounds, the drink CTA, history accents.
- **Magenta** — `#A22C83 / #902178 / #D81B60` — used for the primary
  call-to-action, key stat values, and notification chrome. Lifted directly
  from the original `colors.xml` (`purple`, `dark_purple`, `colorAccent`).
- **Teal** — `#008577 / #00574B` — original `colorPrimary`, retained for
  legacy surfaces.
- **Cool-tinted neutrals** — ink scale 50→900, biased slightly blue so they
  read as part of the water family.

**Type.**
- *Plus Jakarta Sans* — display + body. Modern humanist sans, replaces the
  original's stock Roboto.
- *Nunito* — friendly rounded display, used for big numbers (goal, drink
  amounts) and uppercase pill CTAs. Echoes the soft, encouraging tone.
- *JetBrains Mono* — timestamps in the records list.

**Shape language.**
- Generous radii (12–28px) and full pills for actions.
- Concentric translucent rings for the hero **DRINK WATER** button —
  abstracts the original's bullseye target into a softer ripple.
- Magenta circular icon buttons with a tight drop shadow form the
  toolbar vocabulary across the app.

**Iconography.** Lucide-style 2px stroke icons for UI affordances; the
original PNG icons (drink glass, trophy, gallery, share) are preserved as
brand artifacts.

## Content fundamentals

- Numbers are central — show them large in the rounded display face.
- Microcopy is conversational and reassuring, never hectoring.
- Every reminder has a way out (**Skip**) right next to **Drink**.
- Avoid jargon: "Remember each 45 min", "Our goal", "You carry 1320 mL".

---

## Repository index

| Path | What it is |
|---|---|
| `colors_and_type.css` | All design tokens (CSS custom properties + utility type classes) |
| `assets/` | Original logo + PNG iconography from the Android app |
| `preview/` | Static cards for the Design System tab — colors, type, components, brand |
| `ui_kits/mobile/` | Working mobile UI kit — components + screens, Android-framed |
| `SKILL.md` | How to use this system in new designs |

### Mobile UI kit

`ui_kits/mobile/index.html` — toggleable nav across all 7 screens:
Splash, Welcome, Weight, Ready, Home (with Drink sheet overlay), Records,
Notification. Components live in `Components.jsx`; screen compositions in
`Screens.jsx`.

---

## Crediting

Source code, original iconography, and product behavior:
[github.com/Elvishrdz/android_drink_water_app](https://github.com/Elvishrdz/android_drink_water_app).
