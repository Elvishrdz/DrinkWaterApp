/* eslint-disable */
/* Drink Water — Blocks view for the Reminders schedule.
   Horizontal strip of interval cards.
   • Tap a block to edit (uses the editor card below)
   • Drag a block left/right to reorder (other blocks shuffle to make room)
   • Delete from the editor card (or from the row's × shortcut in manual mode)
*/

const { useState: bState, useMemo: bMemo, useRef: bRef } = React;

const BX_BLOCK_W = 116;     /* px — block width */
const BX_GAP = 10;          /* px — gap between blocks */
const BX_STRIDE = BX_BLOCK_W + BX_GAP;
const BX_MIN_DRAG_PX = 6;

/* When the user drags block at fromIdx → currentIdx, every other block in
   between shifts one slot to fill the gap. */
function bxOffsetFor(i, fromIdx, currentIdx) {
  if (fromIdx === currentIdx) return 0;
  if (fromIdx < currentIdx) {
    return (i > fromIdx && i <= currentIdx) ? -BX_STRIDE : 0;
  }
  return (i >= currentIdx && i < fromIdx) ? BX_STRIDE : 0;
}

/* Reorder by re-assigning chronological time slots to the new sequence of
   (id, amount) pairs. The reminders themselves keep their amount; only the
   slot they occupy in the day changes. */
function bxReorder(schedule, sorted, fromIdx, toIdx) {
  if (fromIdx === toIdx) return schedule;
  const rows = [...sorted];
  const [moved] = rows.splice(fromIdx, 1);
  rows.splice(toIdx, 0, moved);
  const times = sorted.map(r => r.time);   /* already chronological */
  const reordered = rows.map((r, i) => ({ ...r, time: times[i] }));
  const byId = new Map(reordered.map(r => [r.id, r]));
  return schedule.map(r => byId.get(r.id) || r);
}

const BlocksView = ({
  schedule, setSchedule,
  mode, now,
  selectedId, setSelectedId,
  nextIdx,
  onDelete,
}) => {
  const sorted = bMemo(() => [...schedule].sort((a, b) => a.time - b.time), [schedule]);
  const [drag, setDrag] = bState(null);
  const dragRef = bRef(null);
  dragRef.current = drag;
  const stripRef = bRef(null);

  const onPointerDown = (e, r, idx) => {
    if (e.pointerType === "mouse" && e.button !== 0) return;
    const target = e.currentTarget;
    try { target.setPointerCapture(e.pointerId); } catch (_) {}
    const startX = e.clientX;
    const startY = e.clientY;
    let entered = false;
    let aborted = false;

    const onMove = (ev) => {
      if (aborted) return;
      const dx = ev.clientX - startX;
      const dy = ev.clientY - startY;
      if (!entered) {
        /* Drag only enters in manual mode + once horizontal movement exceeds threshold. */
        if (mode !== "manual") return;
        if (Math.abs(dx) < BX_MIN_DRAG_PX) return;
        /* If user is moving mostly vertically, let the page scroll instead. */
        if (Math.abs(dy) > Math.abs(dx) * 1.2) { aborted = true; cleanup(); return; }
        entered = true;
      }
      ev.preventDefault();
      const rawShift = Math.round(dx / BX_STRIDE);
      const currentIdx = Math.max(0, Math.min(sorted.length - 1, idx + rawShift));
      setDrag({ id: r.id, fromIdx: idx, dx, currentIdx });
    };
    const cleanup = () => {
      target.removeEventListener("pointermove", onMove);
      target.removeEventListener("pointerup", onUp);
      target.removeEventListener("pointercancel", onUp);
    };
    const onUp = (ev) => {
      cleanup();
      try { target.releasePointerCapture(ev.pointerId); } catch (_) {}
      if (aborted) { setDrag(null); return; }
      const s = dragRef.current;
      if (entered && s) {
        setSchedule(bxReorder(schedule, sorted, s.fromIdx, s.currentIdx));
        setSelectedId(s.id);
      } else {
        setSelectedId(r.id === selectedId ? null : r.id);
      }
      setDrag(null);
    };
    target.addEventListener("pointermove", onMove, { passive: false });
    target.addEventListener("pointerup", onUp);
    target.addEventListener("pointercancel", onUp);
  };

  const handleDelete = (e, id) => {
    e.stopPropagation();
    onDelete && onDelete(id);
  };

  return (
    <div className="rem-bx-wrap">
      <div className="rem-bx-tools">
        <span className="rem-bx-tools-hint">
          {mode === "manual" ? "Tap to edit · Drag to reorder" : "Tap to inspect"}
        </span>
        <span className="rem-bx-tools-count">
          {sorted.length} reminder{sorted.length === 1 ? "" : "s"}
        </span>
      </div>

      <div className="rem-bx-strip" ref={stripRef}>
        {sorted.map((r, i) => {
          const isDragging = drag?.id === r.id;
          const offsetX = isDragging ? drag.dx : bxOffsetFor(i, drag?.fromIdx ?? -1, drag?.currentIdx ?? -1);
          const isPast = r.time < now;
          const isNext = i === nextIdx;
          const isSelected = selectedId === r.id;
          const isManual = mode === "manual";
          return (
            <div
              key={r.id}
              className={`rem-bx-block
                ${isPast ? "is-past" : ""}
                ${isNext ? "is-next" : ""}
                ${isSelected ? "is-selected" : ""}
                ${isDragging ? "is-dragging" : ""}`}
              style={{
                transform: `translateX(${offsetX}px)${isDragging ? " scale(1.06)" : ""}`,
                transition: isDragging ? "none" : "transform 220ms var(--ease-out)",
                touchAction: isManual ? "pan-y" : "auto",
              }}
              onPointerDown={(e) => onPointerDown(e, r, i)}
            >
              <span className="rem-bx-block-idx">#{i + 1}</span>
              {isNext && <span className="rem-bx-block-badge">NEXT</span>}
              {isPast && !isNext && <span className="rem-bx-block-check" aria-hidden>✓</span>}

              <div className="rem-bx-block-time">{fmtTime12(r.time)}</div>
              <div className="rem-bx-block-amt"><b>{r.amount}</b> mL</div>

              {isManual && (
                <button
                  className="rem-bx-block-x"
                  aria-label="Delete reminder"
                  onPointerDown={(e) => e.stopPropagation()}
                  onClick={(e) => handleDelete(e, r.id)}>
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round">
                    <line x1="6" y1="6" x2="18" y2="18"/>
                    <line x1="18" y1="6" x2="6" y2="18"/>
                  </svg>
                </button>
              )}
              {isManual && (
                <span className="rem-bx-block-grip" aria-hidden>
                  <span/><span/><span/><span/>
                </span>
              )}
            </div>
          );
        })}

        {/* trailing spacer so the last block isn't flush against the right edge */}
        <div className="rem-bx-strip-tail" aria-hidden/>
      </div>
    </div>
  );
};

Object.assign(window, { BlocksView, bxReorder, bxOffsetFor });
