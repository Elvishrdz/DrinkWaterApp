/* eslint-disable */
/* Drink Water — Timeline view for the Reminders schedule.
   Vertical hour rail with draggable interval blocks.
   Long-press (220ms) to drag · tap to open editor. */

const { useState: tlState, useEffect: tlEffect, useMemo: tlMemo, useRef: tlRef } = React;

const HOUR_H = 60;       /* px per hour @ zoom 1x */
const BLOCK_GAP = 15;    /* min gap between reminders when pushing */
const MIN_DRAG_PX = 6;   /* movement threshold before drag is intentional */
const HOLD_MS = 220;     /* (unused, kept for ref) */
const ZOOM_MIN = 1;
const ZOOM_MAX = 5;

/* ---------- PUSH/COLLISION SOLVER ---------- */
function applyMove(schedule, id, newTime, push) {
  let s = schedule.map(r => r.id === id ? { ...r, time: newTime } : r);
  if (!push) return s;
  const moved = s.find(r => r.id === id);
  const later = s.filter(r => r.id !== id && r.time >= moved.time)
                  .sort((a, b) => a.time - b.time);
  let prev = moved.time;
  for (const r of later) {
    if (r.time - prev < BLOCK_GAP) r.time = Math.min(1439, prev + BLOCK_GAP);
    prev = r.time;
  }
  const earlier = s.filter(r => r.id !== id && r.time < moved.time)
                    .sort((a, b) => b.time - a.time);
  let next = moved.time;
  for (const r of earlier) {
    if (next - r.time < BLOCK_GAP) r.time = Math.max(0, next - BLOCK_GAP);
    next = r.time;
  }
  return s;
}

/* ---------- DRAG HOOK ----------
   Manual mode: drag is the primary verb. Any pointer move past the
   threshold begins a drag immediately; a short release without movement
   counts as a tap (opens the editor). No long-press required.
   `pxPerHour` is the live (zoomed) pixel-per-hour value. */
function useBlockDrag({ onTap, onCommit, manual, pinchActiveRef, pxPerHourRef }) {
  const [drag, setDrag] = tlState(null);
  const dragRef = tlRef(null);
  dragRef.current = drag;

  const start = (e, row) => {
    if (e.pointerType === "mouse" && e.button !== 0) return;
    if (pinchActiveRef?.current) return;          /* let pinch win */
    const target = e.currentTarget;
    try { target.setPointerCapture(e.pointerId); } catch (_) {}
    const startY = e.clientY;
    const startMin = row.time;
    let entered = false;
    let aborted = false;

    const onMove = (ev) => {
      if (aborted) return;
      if (pinchActiveRef?.current) {                /* 2nd finger arrived */
        aborted = true;
        cleanup();
        try { target.releasePointerCapture(ev.pointerId); } catch (_) {}
        setDrag(null);
        return;
      }
      const dy = ev.clientY - startY;
      if (!entered) {
        if (!manual) return;
        if (Math.abs(dy) < MIN_DRAG_PX) return;
        entered = true;
      }
      ev.preventDefault();
      const minutesDelta = (dy / (pxPerHourRef?.current || HOUR_H)) * 60;
      const raw = startMin + minutesDelta;
      const snapped = Math.max(0, Math.min(1439, Math.round(raw / 5) * 5));
      setDrag({ id: row.id, startMin, currentMin: snapped, dragging: true });
    };
    const onUp = (ev) => {
      cleanup();
      try { target.releasePointerCapture(ev.pointerId); } catch (_) {}
      if (aborted) return;
      const s = dragRef.current;
      if (entered && s && s.id === row.id) {
        onCommit(row.id, s.currentMin);
      } else {
        onTap(row.id);
      }
      setDrag(null);
    };
    const cleanup = () => {
      target.removeEventListener("pointermove", onMove);
      target.removeEventListener("pointerup", onUp);
      target.removeEventListener("pointercancel", onUp);
    };
    target.addEventListener("pointermove", onMove, { passive: false });
    target.addEventListener("pointerup", onUp);
    target.addEventListener("pointercancel", onUp);
  };

  return { drag, start };
}

/* ---------- PINCH-ZOOM HOOK ----------
   Tracks 2-finger pinch on the rail; updates a zoom value while
   keeping the pinch midpoint anchored to its minute by scroll-offsetting
   the nearest scrollable ancestor.
   Also handles Ctrl+wheel for desktop. */
function usePinchZoom({ railRef, zoom, setZoom, viewStart }) {
  const pinchActiveRef = tlRef(false);
  const pointersRef = tlRef(new Map()); /* pointerId -> {x,y} */
  const stateRef = tlRef(null);

  tlEffect(() => {
    const rail = railRef.current;
    if (!rail) return;

    const getScrollEl = () => {
      let el = rail.parentElement;
      while (el) {
        const s = getComputedStyle(el).overflowY;
        if (s === "auto" || s === "scroll") return el;
        el = el.parentElement;
      }
      return document.scrollingElement || document.documentElement;
    };

    const onPointerDown = (e) => {
      if (e.pointerType !== "touch") return;
      pointersRef.current.set(e.pointerId, { x: e.clientX, y: e.clientY });
      if (pointersRef.current.size === 2) {
        const pts = [...pointersRef.current.values()];
        const dist = Math.hypot(pts[0].x - pts[1].x, pts[0].y - pts[1].y);
        const midY = (pts[0].y + pts[1].y) / 2;
        const railRect = rail.getBoundingClientRect();
        const scrollEl = getScrollEl();
        const yInRail = midY - railRect.top;
        const minAtMid = viewStart + (yInRail / (HOUR_H * zoom)) * 60;
        stateRef.current = { startDist: dist, startZoom: zoom, minAtMid, midY, scrollEl };
        pinchActiveRef.current = true;
      }
    };
    const onPointerMove = (e) => {
      if (e.pointerType !== "touch") return;
      if (!pointersRef.current.has(e.pointerId)) return;
      pointersRef.current.set(e.pointerId, { x: e.clientX, y: e.clientY });
      if (pinchActiveRef.current && pointersRef.current.size >= 2 && stateRef.current) {
        e.preventDefault();
        const pts = [...pointersRef.current.values()].slice(0, 2);
        const dist = Math.hypot(pts[0].x - pts[1].x, pts[0].y - pts[1].y);
        const s = stateRef.current;
        const ratio = dist / Math.max(1, s.startDist);
        const next = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, s.startZoom * ratio));
        setZoom(next);
        /* anchor: keep s.minAtMid under s.midY */
        requestAnimationFrame(() => {
          const railRect = rail.getBoundingClientRect();
          const yForMin = (s.minAtMid - viewStart) / 60 * (HOUR_H * next);
          const desired = railRect.top + yForMin;
          const delta = desired - s.midY;
          if (s.scrollEl) s.scrollEl.scrollTop += delta;
        });
      }
    };
    const onPointerEnd = (e) => {
      if (e.pointerType !== "touch") return;
      pointersRef.current.delete(e.pointerId);
      if (pointersRef.current.size < 2) {
        pinchActiveRef.current = false;
        stateRef.current = null;
      }
    };
    const onWheel = (e) => {
      if (!(e.ctrlKey || e.metaKey)) return;
      e.preventDefault();
      const railRect = rail.getBoundingClientRect();
      const yInRail = e.clientY - railRect.top;
      const minAtCursor = viewStart + (yInRail / (HOUR_H * zoom)) * 60;
      const scrollEl = getScrollEl();
      const factor = Math.exp(-e.deltaY * 0.002);
      const next = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom * factor));
      setZoom(next);
      requestAnimationFrame(() => {
        const r2 = rail.getBoundingClientRect();
        const yForMin = (minAtCursor - viewStart) / 60 * (HOUR_H * next);
        const desired = r2.top + yForMin;
        const delta = desired - e.clientY;
        if (scrollEl) scrollEl.scrollTop += delta;
      });
    };

    rail.addEventListener("pointerdown", onPointerDown);
    rail.addEventListener("pointermove", onPointerMove, { passive: false });
    rail.addEventListener("pointerup", onPointerEnd);
    rail.addEventListener("pointercancel", onPointerEnd);
    rail.addEventListener("wheel", onWheel, { passive: false });
    return () => {
      rail.removeEventListener("pointerdown", onPointerDown);
      rail.removeEventListener("pointermove", onPointerMove);
      rail.removeEventListener("pointerup", onPointerEnd);
      rail.removeEventListener("pointercancel", onPointerEnd);
      rail.removeEventListener("wheel", onWheel);
    };
  }, [zoom, viewStart, setZoom]);

  return { pinchActiveRef };
}

/* ---------- TIMELINE VIEW ---------- */
const TimelineView = ({
  schedule, setSchedule, mode, now,
  viewStart, viewEnd,
  selectedId, setSelectedId,
  collisionMode, setCollisionMode,
  nextIdx,
}) => {
  const sorted = tlMemo(() => [...schedule].sort((a, b) => a.time - b.time), [schedule]);
  const [zoom, setZoom] = tlState(1);
  const pxPerHour = HOUR_H * zoom;
  const pxPerHourRef = tlRef(pxPerHour); pxPerHourRef.current = pxPerHour;
  const railRef = tlRef(null);
  const railH = ((viewEnd - viewStart) / 60) * pxPerHour;
  const yForMin = (m) => ((m - viewStart) / 60) * pxPerHour;
  const inRange = (m) => m >= viewStart && m <= viewEnd;

  const { pinchActiveRef } = usePinchZoom({ railRef, zoom, setZoom, viewStart });

  const { drag, start: startDrag } = useBlockDrag({
    manual: mode === "manual",
    pinchActiveRef,
    pxPerHourRef,
    onTap: (id) => setSelectedId(id === selectedId ? null : id),
    onCommit: (id, time) => {
      setSchedule(applyMove(schedule, id, time, collisionMode === "push"));
      setSelectedId(id);
    },
  });
  const draggedId = drag?.id;

  const zoomIn  = () => setZoom(z => Math.min(ZOOM_MAX, Math.round((z + 0.5) * 2) / 2));
  const zoomOut = () => setZoom(z => Math.max(ZOOM_MIN, Math.round((z - 0.5) * 2) / 2));

  /* hour rows */
  const hours = [];
  const h0 = Math.floor(viewStart / 60);
  const h1 = Math.ceil(viewEnd / 60);
  for (let h = h0; h <= h1; h++) hours.push(h);

  return (
    <div className="rem-tl-wrap">
      {/* Toolbar: hint + push toggle + zoom */}
      <div className="rem-tl-tools">
        {mode === "manual" ? (
          <span className="rem-tl-tools-hint">Drag the grip ⋮⋮ to move</span>
        ) : (
          <span className="rem-tl-tools-hint">Pinch · ⌘+wheel to zoom</span>
        )}
        <div className="rem-tl-tools-right">
          {mode === "manual" && (
            <label className="rem-tl-tools-toggle">
              <input type="checkbox"
                checked={collisionMode === "push"}
                onChange={(e) => setCollisionMode(e.target.checked ? "push" : "free")}/>
              <span>Push others</span>
            </label>
          )}
          <div className="rem-tl-zoom">
            <button onClick={zoomOut} disabled={zoom <= ZOOM_MIN} aria-label="Zoom out">−</button>
            <span className="rem-tl-zoom-val">{zoom.toFixed(1)}×</span>
            <button onClick={zoomIn}  disabled={zoom >= ZOOM_MAX} aria-label="Zoom in">+</button>
          </div>
        </div>
      </div>

      <div className="rem-tl" ref={railRef} style={{ height: railH + 24 }}>
        {/* Hour rail */}
        {hours.map(h => {
          const y = yForMin(h * 60);
          return (
            <div key={h} className="rem-tl-hour" style={{ top: y }}>
              <span className="rem-tl-hour-label">{fmtTimeParts(h * 60).hm}<small>{fmtTimeParts(h * 60).p}</small></span>
              <span className="rem-tl-hour-line"/>
            </div>
          );
        })}

        {/* Active-window shading */}
        <div className="rem-tl-window" style={{ top: 0, height: railH }}/>

        {/* Now indicator */}
        {inRange(now) && (
          <div className="rem-tl-now" style={{ top: yForMin(now) }}>
            <span className="rem-tl-now-dot"/>
            <span className="rem-tl-now-line"/>
            <span className="rem-tl-now-label">NOW {fmtTime12(now)}</span>
          </div>
        )}

        {/* Ghost position while dragging */}
        {drag?.dragging && (
          <div className="rem-tl-ghost" style={{ top: yForMin(drag.currentMin) }}>
            <span className="rem-tl-ghost-line"/>
            <span className="rem-tl-ghost-label">{fmtTime12(drag.currentMin)}</span>
          </div>
        )}

        {/* Blocks */}
        {sorted.map((r, i) => {
          const isDragging = drag?.dragging && draggedId === r.id;
          const blockMin = isDragging ? drag.currentMin : r.time;
          const top = yForMin(blockMin);
          const isPast = r.time < now;
          const isNext = i === nextIdx;
          const isSelected = selectedId === r.id;
          const offRange = !inRange(r.time);
          return (
            <div
              key={r.id}
              className={`rem-tl-block 
                ${isPast ? "is-past" : ""} 
                ${isNext ? "is-next" : ""} 
                ${isDragging ? "is-dragging" : ""} 
                ${isSelected ? "is-selected" : ""}
                ${offRange ? "is-off-range" : ""}`}
              style={{
                top,
                transition: isDragging ? "none" : "top 220ms var(--ease-out)",
              }}
              onPointerDown={(e) => startDrag(e, r)}
            >
              <span className="rem-tl-block-time">{fmtTime12(r.time)}</span>
              <span className="rem-tl-block-amt"><b>{r.amount}</b> mL</span>
              {isNext && <span className="rem-tl-block-badge">Next</span>}
              {mode === "manual" && (
                <span className="rem-tl-block-grip" aria-hidden>
                  <span/><span/><span/>
                </span>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

Object.assign(window, { TimelineView, applyMove });
