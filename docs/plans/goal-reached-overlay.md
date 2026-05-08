# Plan: Goal Reached Celebration Overlay

## Context
The current `GoalReachedWidget` shows a basic `ButtonContent` card (trophy icon + static Spanish text + close button). The `Drink_Water_Design_System` defines 6 polished celebration variants that rotate deterministically by day-of-year so the moment never feels stale. Each variant has a themed hero gradient, ambient particle animations, a medallion, headline/sub copy, a 3-column stats strip, and CTA buttons.

**Two deliverables:**
1. Build all 6 variants as Compose composables (design parity + animations).
2. Wire one into the existing `GoalReachedWidget` service, replacing `ButtonContent` as the rendered content (file kept intact).

---

## Files to Create

### 1. `CelebrationVariant.kt`
**Path:** `compose/.../goalreachedwidget/CelebrationVariant.kt`

Data layer — no UI.

```kotlin
enum class DecorKind { BUBBLES, CONFETTI, SPARKLES, DRIPS, RAYS, RINGS }
sealed class CelebrationMark {
    data class Image(@DrawableRes val resId: Int) : CelebrationMark()
    data class Glyph(val text: String) : CelebrationMark()
}
data class CelebrationVariant(
    val id: String,
    val heroBrush: Brush,
    val heroOverlayBrush: Brush? = null,
    val decorKind: DecorKind,
    val mark: CelebrationMark,
    val eyebrow: String,
    val headline: String,
    val sub: String,
    val cta: String,
)
val CELEBRATIONS: List<CelebrationVariant>
fun todayVariant(): CelebrationVariant
```

**6 variants:**

| # | id | Gradient | Decor | Mark |
|---|----|----|----|----|
| 0 | ocean | `#4FB3DD → #1C8EBF → #0E5A82` (vertical) | BUBBLES | `icon_trophie_white` |
| 1 | carnival | `#B83A95 → #902178` (vertical) | CONFETTI | Glyph "🎉" |
| 2 | starlight | `#0E2235 → #0B1C2C` + radial `#1C8EBF` at 30%/20% | SPARKLES | `icon_trophie_yellow` |
| 3 | rain | `#2BA89A → #1C8EBF → #0E5A82` (vertical) | DRIPS | `icon_glass_of_water_01` |
| 4 | sunset | `#4FB3DD → #1C8EBF → #A22C83` (160° linear) | RAYS | Glyph "✦" |
| 5 | moonlit | `#008577 → #00574B` + radial white glow at 80%/15% | RINGS | Glyph "☾" |

---

### 2. `CelebrationDecor.kt`
**Path:** `compose/.../goalreachedwidget/CelebrationDecor.kt`

One `@Composable` per `DecorKind`. All use `rememberInfiniteTransition()`.

| Composable | Particles | Animation | Duration |
|---|---|---|---|
| `BubblesDecor` | 6 circles (10–26dp) | translateY –90dp + opacity in/out | 4 s |
| `ConfettiDecor` | 14 rectangles 8×12dp | translateY +180dp + rotate 0→360 | 2.6 s |
| `SparklesDecor` | 7 star paths (Canvas) | scale 0.6↔1.1 + opacity 0.3↔1.0 | 2.2 s |
| `DripsDecor` | 10 capsules 4×14dp | translateY +160dp + opacity in/out | 1.8 s |
| `RaysDecor` | 9 lines 2dp wide | opacity 0.35↔0.90 | 3 s |
| `RingsDecor` | 3 circles (border-only) | scale 0.95↔1.08 + opacity 0.5↔1.0 | 3.2 s |

---

### 3. `GoalCelebrationCard.kt`
**Path:** `compose/.../goalreachedwidget/GoalCelebrationCard.kt`

Main card composable with hero gradient, decor layer, medallion pop-in animation, stats strip, and action buttons. Includes 6 `@Preview` functions.

---

### 4. `GoalCelebrationOverlay.kt`
**Path:** `compose/.../goalreachedwidget/GoalCelebrationOverlay.kt`

Full-screen composable wrapping the card with an animated scrim.

---

## Files to Modify

### `GoalReachedWidget.kt`
- Add `onStartCommand` to read Intent extras: `goalMl`, `drinkCount`, `streakDays`
- Replace `ButtonContent(...)` with `GoalCelebrationOverlay(...)`

### `DrinkViewModel.kt`
- Update `displayGoalReachedOverlay()` to attach Intent extras

---

## Implementation Order

1. Create `docs/plans/goal-reached-overlay.md`
2. Create `CelebrationVariant.kt`
3. Create `CelebrationDecor.kt`
4. Create `GoalCelebrationCard.kt`
5. Create `GoalCelebrationOverlay.kt`
6. Modify `DrinkViewModel.kt`
7. Modify `GoalReachedWidget.kt`
