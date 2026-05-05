# Drink Water Design System — Skill

Use this system when designing **anything for the Drink Water hydration app**
or for a new product that wants the same friendly, water-forward identity.

## Always start by

1. Importing the tokens: `<link rel="stylesheet" href="/colors_and_type.css"/>`.
2. Pulling the components/screens from `ui_kits/mobile/` rather than building
   from scratch — the magenta CTA pill, ripple-ring drink button, magenta
   icon buttons, and bordered record row are the system's signatures.
3. Choosing one hero gradient: **water** (`--grad-water-hero`) for primary
   surfaces, **magenta** (`--grad-magenta`) for stat callouts and CTAs.

## Color rules

- Primary action → `--grad-magenta` pill, white text, `--shadow-magenta`.
- Hero background → water gradient with a faint `welcome_wave.jpg` overlay
  at ~18% opacity for texture.
- Stat values → magenta. Body copy → `--fg-1` (ink-900).
- Never put magenta on water gradient or vice-versa as a fill — they only
  meet at button + chip level, on white surfaces.

## Type rules

- Big numbers (goal, amounts, timer minutes) → **Nunito 800**.
- Headings → **Plus Jakarta Sans 700**.
- CTAs → **Nunito 700 uppercase**, letter-spacing 0.06em.
- Timestamps → **JetBrains Mono 600**, in magenta.

## Component picks

| Need | Use |
|---|---|
| Primary CTA | `<Button variant="primary">` |
| Toolbar/utility action | `<IconButton name="bell"/>` |
| Stat/KPI card | `<StatCard tone="magenta\|paper">` |
| Hydration progress | `<HydrationBar percent={…}/>` + `<CarryPill/>` |
| Drink log row | `<RecordRow time amount/>` |
| Hero hydration tap | `<DrinkButton onTap/>` |
| Onboarding step | `<WelcomeCard title>` with back/next |

## Don'ts

- **Don't** introduce new accent colors — magenta is the only accent.
- **Don't** use square buttons. Pills + circles only.
- **Don't** use Inter, Roboto, or system-ui — the rounded Nunito is
  load-bearing for the brand voice.
- **Don't** redraw the water-glass icon in SVG — use the original PNG
  (`assets/ic_drink_blue.png` / `_holo_dark.png`) as the brand mark.

## Extension targets

If asked to design a screen this kit doesn't cover (e.g. settings, history
calendar, achievements / trophies), reuse:
- The wave-header layout from `WelcomeCard` for any modal/full-screen.
- The magenta `TimeRow` pattern for any "tap to change" setting.
- The `assets/ic_trophy_*` PNGs for any reward / streak surface.
