/* eslint-disable */
/* Drink Water — HISTORY screen
   - Day filter (horizontal day strip + arrows)
   - Records grouped by hour
   - Time-of-day icon per group (sunrise / sun / sunset / moon)
   - Long-press / tap a row to expose Edit / Delete
   Matches the rest of the design system: water-blue + magenta accents,
   Plus Jakarta Sans display, Nunito for big numbers, JetBrains Mono for times. */

const { useState: useStateHistory, useMemo } = React;

/* ---------- Time-of-day icon (per hour) ---------- */
const TimeOfDayIcon = ({ hour, size = 18, color = "#fff" }) => {
  const props = {
    width: size, height: size, viewBox: "0 0 24 24", fill: "none",
    stroke: color, strokeWidth: 2, strokeLinecap: "round", strokeLinejoin: "round",
  };
  // 5–8 sunrise · 8–17 sun · 17–20 sunset · else moon
  if (hour >= 5 && hour < 8) {
    // sunrise — half sun + horizon arrow up
    return (
      <svg {...props}>
        <path d="M3 18h18"/>
        <path d="M5 14a7 7 0 0 1 14 0"/>
        <path d="M12 3v3M5.6 6.6l1.4 1.4M18.4 6.6L17 8M2 11h2M20 11h2"/>
        <path d="M9 21l3-3 3 3"/>
      </svg>
    );
  }
  if (hour >= 8 && hour < 17) {
    // sun
    return (
      <svg {...props}>
        <circle cx="12" cy="12" r="4"/>
        <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/>
      </svg>
    );
  }
  if (hour >= 17 && hour < 20) {
    // sunset — half sun + horizon arrow down
    return (
      <svg {...props}>
        <path d="M3 18h18"/>
        <path d="M5 14a7 7 0 0 1 14 0"/>
        <path d="M12 3v3M5.6 6.6l1.4 1.4M18.4 6.6L17 8M2 11h2M20 11h2"/>
        <path d="M9 18l3 3 3-3"/>
      </svg>
    );
  }
  // moon
  return (
    <svg {...props}>
      <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/>
    </svg>
  );
};

const partOfDay = (h) => {
  if (h >= 5 && h < 12)  return "Morning";
  if (h >= 12 && h < 17) return "Afternoon";
  if (h >= 17 && h < 21) return "Evening";
  return "Night";
};

/* tone per time-of-day for the hour pill */
const toneForHour = (h) => {
  if (h >= 5 && h < 8)  return "sunrise"; // warm magenta-ish
  if (h >= 8 && h < 17) return "day";     // water blue
  if (h >= 17 && h < 20) return "dusk";   // magenta
  return "night";                          // ink
};

/* ---------- Day strip ---------- */
const DayStrip = ({ days, activeKey, onPick, onPrev, onNext }) => (
  <div className="dw-history-daystrip">
    <button className="dw-history-arrow" onClick={onPrev} aria-label="Previous">
      <Icon name="chevronLeft" size={16} color="var(--fg-2)"/>
    </button>
    <div className="dw-history-days">
      {days.map(d => (
        <button key={d.key}
          className={`dw-history-day ${d.key === activeKey ? "is-active" : ""} ${d.isToday ? "is-today" : ""}`}
          onClick={() => onPick(d.key)}>
          <span className="dw-history-day-w">{d.weekday}</span>
          <span className="dw-history-day-n">{d.dayNum}</span>
          {d.met && <span className="dw-history-day-dot" aria-hidden="true"/>}
        </button>
      ))}
    </div>
    <button className="dw-history-arrow" onClick={onNext} aria-label="Next">
      <Icon name="chevronRight" size={16} color="var(--fg-2)"/>
    </button>
  </div>
);

/* ---------- Single record row (with swipe-out actions) ---------- */
const HistoryRecord = ({ rec, expanded, onToggle, onEdit, onDelete }) => (
  <div className={`dw-history-rec ${expanded ? "is-open" : ""}`}>
    <button className="dw-history-rec-main" onClick={onToggle}>
      <div className="dw-history-rec-icon"><Icon name="glass" size={14} color="#fff"/></div>
      <div className="dw-history-rec-body">
        <div className="dw-history-rec-amount">{rec.amount} <small>mL</small></div>
        <div className="dw-history-rec-time">{rec.time}</div>
      </div>
      <Icon name={expanded ? "chevronDown" : "chevronRight"} size={14} color="var(--ink-300)"/>
    </button>
    {expanded && (
      <div className="dw-history-rec-actions">
        <button className="dw-history-action edit" onClick={() => onEdit(rec)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z"/>
          </svg>
          <span>Edit</span>
        </button>
        <button className="dw-history-action delete" onClick={() => onDelete(rec)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
            <path d="M10 11v6M14 11v6"/>
            <path d="M9 6V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/>
          </svg>
          <span>Delete</span>
        </button>
      </div>
    )}
  </div>
);

/* ---------- Hour group ---------- */
const HourGroup = ({ hour, records, expandedId, setExpandedId, onEdit, onDelete }) => {
  const tone = toneForHour(hour);
  const total = records.reduce((s, r) => s + r.amount, 0);
  const hourLabel = ((hour + 11) % 12 + 1) + (hour < 12 ? " a.m." : " p.m.");
  return (
    <div className="dw-history-group">
      <div className="dw-history-grouphead">
        <div className={`dw-history-hour-pill is-${tone}`}>
          <TimeOfDayIcon hour={hour} size={14}/>
          <span className="dw-history-hour-num">{hourLabel}</span>
        </div>
        <div className="dw-history-grouphead-meta">
          <span className="dw-history-grouphead-part">{partOfDay(hour)}</span>
          <span className="dw-history-grouphead-dot">·</span>
          <span className="dw-history-grouphead-total">{total} mL</span>
        </div>
      </div>
      <div className="dw-history-group-body">
        {records.map(r => (
          <HistoryRecord
            key={r.id}
            rec={r}
            expanded={expandedId === r.id}
            onToggle={() => setExpandedId(expandedId === r.id ? null : r.id)}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </div>
    </div>
  );
};

/* ---------- Edit modal ---------- */
const EditRecordModal = ({ rec, onCancel, onSave }) => {
  const [amount, setAmount] = useStateHistory(String(rec.amount));
  const [time, setTime] = useStateHistory(rec.time);
  return (
    <div className="dw-history-modal-overlay" onClick={onCancel}>
      <div className="dw-history-modal" onClick={e => e.stopPropagation()}>
        <div className="dw-history-modal-head">
          <h3>Edit drink</h3>
          <button className="dw-sheet-close" onClick={onCancel}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--ink-500)" strokeWidth="2" strokeLinecap="round"><path d="M6 6l12 12M18 6l-12 12"/></svg>
          </button>
        </div>
        <label className="dw-history-modal-field">
          <span>Amount</span>
          <div className="dw-sheet-custom-input">
            <input value={amount} onChange={e => setAmount(e.target.value.replace(/\D/g,""))} inputMode="numeric"/>
            <span className="dw-sheet-unit">mL</span>
          </div>
        </label>
        <label className="dw-history-modal-field">
          <span>Time</span>
          <div className="dw-sheet-custom-input">
            <input value={time} onChange={e => setTime(e.target.value)}/>
          </div>
        </label>
        <div className="dw-history-modal-actions">
          <button className="dw-history-action edit ghost" onClick={onCancel}>Cancel</button>
          <button className="dw-btn dw-btn-primary" onClick={() => onSave({ ...rec, amount: +amount || rec.amount, time })}>Save</button>
        </div>
      </div>
    </div>
  );
};

/* ---------- Calendar picker modal ---------- */
const CalendarPicker = ({ initialKey, daysWithData, onCancel, onPick }) => {
  const [view, setView] = useStateHistory(() => {
    const [y, m] = initialKey.split("-").map(Number);
    return { year: y, month: m };
  });

  const monthNames = ["January","February","March","April","May","June","July","August","September","October","November","December"];
  const wkdNames = ["S","M","T","W","T","F","S"];

  const firstDay = new Date(view.year, view.month - 1, 1).getDay();
  const daysInMonth = new Date(view.year, view.month, 0).getDate();

  const cells = [];
  for (let i = 0; i < firstDay; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);

  const fmt = (d) => `${view.year}-${String(view.month).padStart(2,"0")}-${String(d).padStart(2,"0")}`;
  const goPrevMonth = () => {
    if (view.month === 1) setView({ year: view.year - 1, month: 12 });
    else setView({ year: view.year, month: view.month - 1 });
  };
  const goNextMonth = () => {
    if (view.month === 12) setView({ year: view.year + 1, month: 1 });
    else setView({ year: view.year, month: view.month + 1 });
  };

  return (
    <div className="dw-history-modal-overlay" onClick={onCancel}>
      <div className="dw-cal" onClick={e => e.stopPropagation()}>
        <div className="dw-cal-head">
          <button className="dw-cal-navbtn" onClick={goPrevMonth} aria-label="Previous month">
            <Icon name="chevronLeft" size={16} color="var(--fg-1)"/>
          </button>
          <div className="dw-cal-month">{monthNames[view.month - 1]} {view.year}</div>
          <button className="dw-cal-navbtn" onClick={goNextMonth} aria-label="Next month">
            <Icon name="chevronRight" size={16} color="var(--fg-1)"/>
          </button>
        </div>
        <div className="dw-cal-wkds">
          {wkdNames.map((w, i) => <div key={i} className="dw-cal-wkd">{w}</div>)}
        </div>
        <div className="dw-cal-grid">
          {cells.map((d, i) => {
            if (d === null) return <div key={i} className="dw-cal-cell is-empty"/>;
            const k = fmt(d);
            const hasData = daysWithData.has(k);
            const isSelected = k === initialKey;
            return (
              <button key={i}
                className={`dw-cal-cell ${isSelected ? "is-selected" : ""} ${hasData ? "has-data" : ""}`}
                onClick={() => onPick(k)}>
                <span>{d}</span>
                {hasData && <span className="dw-cal-dot" aria-hidden="true"/>}
              </button>
            );
          })}
        </div>
        <div className="dw-history-modal-actions">
          <button className="dw-history-action edit ghost" onClick={onCancel}>Cancel</button>
          <button className="dw-btn dw-btn-primary" onClick={() => onPick(initialKey)}>Done</button>
        </div>
      </div>
    </div>
  );
};

/* ---------- HISTORY SCREEN ---------- */
const HistoryScreen = ({ days, activeKey, onPickDay, onPrev, onNext, records, goal, onUpdateRecord, onDeleteRecord, allHistory }) => {
  const [expandedId, setExpandedId] = useStateHistory(null);
  const [editingRec, setEditingRec] = useStateHistory(null);
  const [showCal, setShowCal] = useStateHistory(false);

  // Group records by hour (uses rec.hour, expects 0–23)
  const groups = useMemo(() => {
    const m = new Map();
    for (const r of records) {
      if (!m.has(r.hour)) m.set(r.hour, []);
      m.get(r.hour).push(r);
    }
    return [...m.entries()]
      .sort((a, b) => a[0] - b[0])
      .map(([hour, recs]) => ({ hour, records: recs.sort((a, b) => a.time.localeCompare(b.time)) }));
  }, [records]);

  const total = records.reduce((s, r) => s + r.amount, 0);
  const pct = Math.min(100, Math.round((total / goal) * 100));
  const activeDay = days.find(d => d.key === activeKey);

  return (
    <div className="dw-screen dw-screen-history">
      <div className="dw-history-head">
        <h1>History</h1>
        <button className="dw-history-export" aria-label="Pick a date" onClick={() => setShowCal(true)}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--brand-magenta)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2"/>
            <path d="M16 2v4M8 2v4M3 10h18"/>
          </svg>
        </button>
      </div>

      <DayStrip days={days} activeKey={activeKey} onPick={onPickDay} onPrev={onPrev} onNext={onNext}/>

      <div className="dw-history-summary">
        <div className="dw-history-summary-left">
          <div className="dw-history-summary-day">{activeDay ? activeDay.fullLabel : ""}</div>
          <div className="dw-history-summary-amount">
            {total.toLocaleString()} <small>mL</small>
          </div>
          <div className="dw-history-summary-meta">
            {records.length} drink{records.length === 1 ? "" : "s"} · {pct}% of goal
          </div>
        </div>
        <div className="dw-history-summary-mini">
          <svg viewBox="0 0 60 60" width="56" height="56">
            <circle cx="30" cy="30" r="24" stroke="rgba(28,142,191,0.18)" strokeWidth="6" fill="none"/>
            <circle cx="30" cy="30" r="24"
              stroke="url(#histGrad)" strokeWidth="6" fill="none" strokeLinecap="round"
              strokeDasharray={`${(pct/100) * 2 * Math.PI * 24} ${2 * Math.PI * 24}`}
              transform="rotate(-90 30 30)"/>
            <defs>
              <linearGradient id="histGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stopColor="#4FB3DD"/>
                <stop offset="1" stopColor="#0E5A82"/>
              </linearGradient>
            </defs>
          </svg>
          <div className="dw-history-summary-mini-pct">{pct}%</div>
        </div>
      </div>

      <div className="dw-history-list">
        {groups.length === 0 ? (
          <div className="dw-history-empty">
            <div className="dw-history-empty-glyph">
              <Icon name="glass" size={28} color="var(--ink-300)"/>
            </div>
            <div className="dw-history-empty-title">No drinks logged</div>
            <div className="dw-history-empty-sub">Pick another day, or head to Today to add one.</div>
          </div>
        ) : (
          groups.map(g => (
            <HourGroup
              key={g.hour}
              hour={g.hour}
              records={g.records}
              expandedId={expandedId}
              setExpandedId={setExpandedId}
              onEdit={setEditingRec}
              onDelete={(r) => { onDeleteRecord(r); setExpandedId(null); }}
            />
          ))
        )}
      </div>

      {editingRec && (
        <EditRecordModal
          rec={editingRec}
          onCancel={() => setEditingRec(null)}
          onSave={(updated) => { onUpdateRecord(updated); setEditingRec(null); setExpandedId(null); }}
        />
      )}

      {showCal && (
        <CalendarPicker
          initialKey={activeKey}
          daysWithData={new Set(Object.keys(allHistory || {}).filter(k => (allHistory[k] || []).length > 0))}
          onCancel={() => setShowCal(false)}
          onPick={(k) => { onPickDay(k); setShowCal(false); }}
        />
      )}
    </div>
  );
};

Object.assign(window, {
  HistoryScreen, TimeOfDayIcon, HourGroup, HistoryRecord, DayStrip, EditRecordModal, CalendarPicker,
});
