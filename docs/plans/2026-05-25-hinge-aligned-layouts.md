# Plan: Hinge-aligned layouts using Compose `Layout` composable

## Context

The app uses `NavigationSuiteScaffold` that renders either a `NavigationBar` (portrait / table
top) or a `NavigationRail` (landscape / foldable) and constrains the content area accordingly.

Two alignment problems exist on foldable devices:

### Problem 1 — Book mode (FOLDABLE_BOOK)
`BookModeInfo.leftPanelWidth` is measured from the physical screen left edge, but the content
area starts after the nav rail. The current `weight(0.7f)` / `weight(1f)` split is arbitrary and
doesn't align with the physical hinge. The left column must span exactly from the content start
to the hinge.

### Problem 2 — Table top (TABLE_TOP)
The physical hinge divides the screen *horizontally* at the fold position. But the bottom
navigation bar and ad banner consume vertical space, shortening the content area. The current
`weight(1f)` / `weight(1f)` split lands above the physical hinge.

### Solution strategy: `Layout` composable (pixel-level, single pass)
Instead of `BoxWithConstraints` (which triggers a double measurement pass), we use Compose's
`Layout` composable. Its `MeasureScope` receives `constraints.maxWidth` / `constraints.maxHeight`
in pixels — the exact content area after the scaffold constrains it. This is where we compute
the hinge-aligned split and constrain each child precisely.

---

## Core math (all in pixels inside MeasureScope)

### Book mode
```
constraints.maxWidth  = content area width (nav rail already excluded by scaffold)
screenWidthPx         = LocalWindowInfo.current.containerSize.width  (captured before lambda)
navRailWidthPx        = screenWidthPx - constraints.maxWidth
leftWidthPx           = bookInfo.leftPanelWidthPx - navRailWidthPx   (coerced to [0, maxWidth])
rightWidthPx          = constraints.maxWidth - leftWidthPx

bookModeInfo == null  → leftWidthPx = constraints.maxWidth / 2  (equal split, LANDSCAPE / FOLDABLE_EXPANDED)
```

### Table top
```
topPanelHeightPx      = tableTopInfo.topPanelHeight in px   (captured before lambda)
topInsetHeightPx      = contentPadding.calculateTopPadding() in px   (captured before lambda)
topHeightPx           = topPanelHeightPx - topInsetHeightPx    (coerced to [0, maxHeight])
bottomHeightPx        = constraints.maxHeight - topHeightPx

tableTopInfo == null  → topHeightPx = constraints.maxHeight / 2  (equal split)
```

---

## Current workaround state (to be replaced)

The user added temporary workarounds visible in the modified `DrinkScreen.kt`:

- **Landscape** (lines 95-102): `leftContentWeight` = `0.8f` when NavigationRail + foldable,
  otherwise `1f` — a hardcoded float approximation. Passed to `DrinkLandscape` which uses it as
  `weight(leftContentWeight)` on the left column.
- **Table top** (lines 119-133): `bottomContentWeight` — four hardcoded values (0.4f / 0.7f / 1f)
  depending on NavigationType × isAdFree. Passed to `DrinkTableTop` which uses it as
  `weight(bottomContentWeight)` on the bottom row.
- `rememberBookModeInfo()` has been removed from DrinkScreen's imports and call sites.

The plan replaces both workaround float params with geometry-driven `Layout` composables.

---

## Files to change

| File | Change |
|---|---|
| `core/presentation/LayoutTypeExtension.kt` | Add `rememberTableTopInfo()` and `TableTopInfo` |
| **`core/presentation/FoldableLayouts.kt`** *(new)* | `FoldAwareTwoColumnRow` and `FoldAwareTwoRowColumn` layout composables |
| `feature/drink/layout/DrinkLandscape.kt` | Remove `leftContentWeight`, accept `bookModeInfo`, replace Row with `FoldAwareTwoColumnRow` |
| `feature/drink/layout/DrinkTableTop.kt` | Remove `bottomContentWeight`, accept `tableTopInfo`, replace outer Column with `FoldAwareTwoRowColumn` |
| `feature/drink/DrinkScreen.kt` | Remove hardcoded weight computations; add `rememberBookModeInfo()` back + new `rememberTableTopInfo()` |

---

## Step-by-step implementation

### 1. `LayoutTypeExtension.kt` — add table top utilities

Below the existing `BookModeInfo` block, add:

```kotlin
data class TableTopInfo(
    /** Height from physical top edge to the hinge start. */
    val topPanelHeight: Dp,
    /** Height from hinge end to physical bottom edge. */
    val bottomPanelHeight: Dp,
    val hingeHeight: Dp,
    val isSeparating: Boolean,
)

/**
 * Returns hinge geometry when the device is in tabletop posture
 * (half-open with a horizontal hinge), or null in any other layout.
 * Mirrors [rememberBookModeInfo] for the horizontal axis.
 */
@SuppressLint("RestrictedApi")
@Composable
fun rememberTableTopInfo(): TableTopInfo? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val windowHeightPx = LocalWindowInfo.current.containerSize.height

    val initial = WindowLayoutInfo(emptyList())
    val windowLayoutInfo by remember(context) {
        WindowInfoTracker.getOrCreate(context).windowLayoutInfo(context)
    }.collectAsStateWithLifecycle(initial)

    val feature = windowLayoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull {
            it.orientation == FoldingFeature.Orientation.HORIZONTAL &&
                    it.state == FoldingFeature.State.HALF_OPENED
        } ?: return null

    return with(density) {
        TableTopInfo(
            topPanelHeight = feature.bounds.top.toDp(),
            bottomPanelHeight = (windowHeightPx - feature.bounds.bottom).toDp(),
            hingeHeight = (feature.bounds.bottom - feature.bounds.top).toDp(),
            isSeparating = feature.isSeparating,
        )
    }
}
```

---

### 2. `core/presentation/FoldableLayouts.kt` *(new file)*

Two reusable layout composables. All fold-split math lives here.

```kotlin
/**
 * Two-column row where the left column is sized to the left display panel in book mode.
 * Falls back to an equal 50/50 split when [bookModeInfo] is null.
 *
 * The nav-rail width is derived internally from the difference between the full window width
 * and the available content width (constraints.maxWidth).
 */
@Composable
fun FoldAwareTwoColumnRow(
    bookModeInfo: BookModeInfo?,
    modifier: Modifier = Modifier,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val screenWidthPx = LocalWindowInfo.current.containerSize.width
    val leftPanelWidthPx = with(density) { bookModeInfo?.leftPanelWidth?.roundToPx() }

    Layout(
        content = { leftContent(); rightContent() },
        modifier = modifier,
    ) { measurables, constraints ->
        val totalWidth = constraints.maxWidth
        val navRailWidthPx = screenWidthPx - totalWidth
        val leftWidthPx = leftPanelWidthPx
            ?.minus(navRailWidthPx)
            ?.coerceIn(0, totalWidth)
            ?: (totalWidth / 2)
        val rightWidthPx = totalWidth - leftWidthPx

        val leftPlaceable = measurables[0].measure(
            constraints.copy(minWidth = 0, maxWidth = leftWidthPx)
        )
        val rightPlaceable = measurables[1].measure(
            constraints.copy(minWidth = 0, maxWidth = rightWidthPx)
        )

        layout(totalWidth, constraints.maxHeight) {
            leftPlaceable.placeRelative(0, 0)
            rightPlaceable.placeRelative(leftWidthPx, 0)
        }
    }
}

/**
 * Two-row column where the top row is sized to the top display panel in tabletop mode.
 * Falls back to an equal 50/50 split when [tableTopInfo] is null.
 *
 * [topInsetHeight] removes the status-bar height from the top panel so the split lands
 * exactly at the physical hinge even when a banner or bottom nav bar is present.
 */
@Composable
fun FoldAwareTwoRowColumn(
    tableTopInfo: TableTopInfo?,
    topInsetHeight: Dp = 0.dp,
    modifier: Modifier = Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val topPanelHeightPx = with(density) { tableTopInfo?.topPanelHeight?.roundToPx() }
    val topInsetHeightPx = with(density) { topInsetHeight.roundToPx() }

    Layout(
        content = { topContent(); bottomContent() },
        modifier = modifier,
    ) { measurables, constraints ->
        val totalHeight = constraints.maxHeight
        val topHeightPx = topPanelHeightPx
            ?.minus(topInsetHeightPx)
            ?.coerceIn(0, totalHeight)
            ?: (totalHeight / 2)
        val bottomHeightPx = totalHeight - topHeightPx

        val topPlaceable = measurables[0].measure(
            constraints.copy(minHeight = 0, maxHeight = topHeightPx)
        )
        val bottomPlaceable = measurables[1].measure(
            constraints.copy(minHeight = 0, maxHeight = bottomHeightPx)
        )

        layout(constraints.maxWidth, totalHeight) {
            topPlaceable.placeRelative(0, 0)
            bottomPlaceable.placeRelative(0, topHeightPx)
        }
    }
}
```

---

### 3. `DrinkLandscape.kt` — use `FoldAwareTwoColumnRow`

1. Add `bookModeInfo: BookModeInfo? = null` parameter.
2. Replace the top-level `Row(modifier = modifier, ...)` with:
   ```kotlin
   FoldAwareTwoColumnRow(
       bookModeInfo = bookModeInfo,
       modifier = modifier.padding(horizontal = 8.dp),
       leftContent = {
           Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState()), ...) {
               // existing ring content
           }
       },
       rightContent = {
           Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
               // existing actions + alarm + log content
           }
       },
   )
   ```
3. Remove the old `Row` wrapper and the `weight()` modifiers on both columns — width is now
   controlled by the Layout composable.

---

### 4. `DrinkTableTop.kt` — use `FoldAwareTwoRowColumn`

1. Add `tableTopInfo: TableTopInfo? = null` parameter.
2. Replace the outer `Column(modifier = modifier.padding(horizontal = borderPadding))` with:
   ```kotlin
   FoldAwareTwoRowColumn(
       tableTopInfo = tableTopInfo,
       topInsetHeight = contentPadding.calculateTopPadding(),
       modifier = modifier.padding(horizontal = borderPadding),
       topContent = {
           Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(borderPadding)) {
               Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), ...) {
                   // ring content
               }
               Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), ...) {
                   // alarm content
               }
           }
       },
       bottomContent = {
           Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(borderPadding)) {
               Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), ...) {
                   // actions content
               }
               Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), ...) {
                   // log content
               }
           }
       },
   )
   ```
3. Remove the old `weight(1f)` on both Rows — height is controlled by `FoldAwareTwoRowColumn`.

---

### 5. `DrinkScreen.kt` — remove workarounds and wire up both

In the `LANDSCAPE / FOLDABLE_BOOK / FOLDABLE_EXPANDED` branch:
- Remove the `leftContentWeight` calculation block (lines 95-102)
- Re-add `rememberBookModeInfo()` (it was removed with the workaround)
- Remove `leftContentWeight = leftContentWeight` from the call site
- Add `bookModeInfo = bookInfo`

```kotlin
val bookInfo = rememberBookModeInfo()
DrinkLandscape(
    ...
    bookModeInfo = bookInfo,
)
```

In the `TABLE_TOP` branch:
- Remove the `bottomContentWeight` calculation block (lines 119-133)
- Add `rememberTableTopInfo()`
- Remove `bottomContentWeight = bottomContentWeight` from the call site
- Add `tableTopInfo = tableTopInfo`

```kotlin
val tableTopInfo = rememberTableTopInfo()
DrinkTableTop(
    ...
    tableTopInfo = tableTopInfo,
)
```

---

## Reuse on other screens

Any landscape screen (book mode):
```kotlin
val bookInfo = rememberBookModeInfo()   // already in LayoutTypeExtension.kt

FoldAwareTwoColumnRow(
    bookModeInfo = bookInfo,
    modifier = modifier,
    leftContent = { /* left panel */ },
    rightContent = { /* right panel */ },
)
```

Any tabletop screen:
```kotlin
val tableTopInfo = rememberTableTopInfo()

FoldAwareTwoRowColumn(
    tableTopInfo = tableTopInfo,
    topInsetHeight = contentPadding.calculateTopPadding(),
    modifier = modifier,
    topContent = { /* top panel */ },
    bottomContent = { /* bottom panel */ },
)
```

---

## Verification

1. **LANDSCAPE / FOLDABLE_EXPANDED** (`bookModeInfo = null`): equal 50/50 split (no regression).
2. **FOLDABLE_BOOK**: left column aligns to the hinge. Right column fills the remainder.
3. **TABLE_TOP with ad banner**: top row ends exactly at the physical hinge even with bottom
   chrome consuming space.
4. **TABLE_TOP without banner (ad-free)**: same formula, same result.
5. **Previews** compile and render with equal splits (fold info not active in preview tool).
