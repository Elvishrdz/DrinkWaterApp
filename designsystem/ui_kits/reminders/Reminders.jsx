/* eslint-disable */
/* Drink Water — REMINDERS kit
   New screen integrated into the bottom menu (5 tabs).
   Self-contained: bring your own colors_and_type.css.
*/

const { useState: rState, useEffect: rEffect, useMemo: rMemo, useRef: rRef } = React;

/* ============================================================
   TIME HELPERS
   ============================================================ */
function fmtTime12(min) {
  const m = ((min % 1440) + 1440) % 1440;
  let h = Math.floor(m / 60);
  const mm = m % 60;
  const period = h >= 12 ? "p.m." : "a.m.";
  if (h === 0) h = 12;
  else if (h > 12) h -= 12;
  return `${h}:${String(mm).padStart(2, "0")} ${period}`;
}
function fmtTimeParts(min) {
  const m = ((min % 1440) + 1440) % 1440;
  let h = Math.floor(m / 60);
  const mm = m % 60;
  const period = h >= 12 ? "PM" : "AM";
  if (h === 0) h = 12;
  else if (h > 12) h -= 12;
  return { hm: `${h}:${String(mm).padStart(2, "0")}`, p: period };
}
function fmtDur(min) {
  if (min <= 0) return "now";
  if (min < 60) return `in ${min} min`;
  const h = Math.floor(min / 60);
  const mm = min % 60;
  if (h < 24) return mm ? `in ${h}h ${mm}m` : `in ${h}h`;
  return "tomorrow";
}
function snap5(n) { return Math.round(n / 5) * 5; }
function roundML(n) { return Math.max(10, Math.round(n / 10) * 10); }

/* ============================================================
   SCHEDULE GENERATION
   ============================================================ */
function genByInterval(start, end, interval, goalMl) {
  const out = [];
  for (let t = start; t <= end; t += interval) out.push(t);
  const n = out.length;
  if (n === 0) return [];
  const per = roundML(goalMl / n);
  return out.map((t, i) => ({ id: `g-${i}-${t}`, time: t, amount: per }));
}
function genByAmount(start, end, amount, goalMl) {
  const n = Math.max(1, Math.ceil(goalMl / amount));
  if (n === 1) return [{ id: "g-0", time: start, amount: Math.min(amount, goalMl) }];
  const step = (end - start) / (n - 1);
  return Array.from({ length: n }, (_, i) => {
    const t = i === 0 ? start : i === n - 1 ? end : snap5(start + step * i);
    const a = i === n - 1
      ? (goalMl - amount * (n - 1)) || amount
      : amount;
    return { id: `g-${i}-${t}`, time: t, amount: Math.max(10, a) };
  });
}

/* ============================================================
   VALIDATION
   ============================================================ */
function validate({ mode, autoMode, start, end, interval, amount, goalMl, schedule, activeDays }) {
  const errs = [];
  if (!activeDays.some(Boolean)) {
    errs.push({ level: "error", msg: "Pick at least one active day for reminders to fire." });
  }
  if (mode === "auto") {
    if (end - start < 60) {
      errs.push({ level: "error", msg: "Active window must be at least 1 hour (start to end)." });
    }
    if (autoMode === "interval") {
      if (interval < 5) errs.push({ level: "error", msg: "Interval must be at least 5 minutes." });
      if (interval > end - start) {
        errs.push({ level: "error", msg: "Interval is longer than the active window — no reminders will trigger." });
      } else if (interval < 15) {
        errs.push({ level: "warn", msg: "Very frequent — try 15+ min between reminders to avoid fatigue." });
      }
    }
    if (autoMode === "amount") {
      if (amount > goalMl) errs.push({ level: "error", msg: "Per-drink amount can't exceed your daily goal." });
      if (amount < 50) errs.push({ level: "warn", msg: "Tiny drinks mean lots of notifications. Try 100 mL or more." });
      const need = Math.ceil(goalMl / amount);
      if (need > (end - start) / 5) {
        errs.push({ level: "error", msg: `Window too short for ${need} reminders at 5 min spacing. Increase the window or the per-drink amount.` });
      }
    }
  } else {
    if (schedule.length === 0) {
      errs.push({ level: "warn", msg: "No reminders set yet. Add one to start." });
    }
    const sum = schedule.reduce((s, r) => s + r.amount, 0);
    if (sum < goalMl * 0.9) {
      errs.push({ level: "warn", msg: `Reminders only cover ${sum} mL of your ${goalMl} mL goal.` });
    }
    const sorted = [...schedule].sort((a, b) => a.time - b.time);
    for (let i = 1; i < sorted.length; i++) {
      if (sorted[i].time - sorted[i - 1].time < 5) {
        errs.push({ level: "warn", msg: "Some reminders are less than 5 min apart." });
        break;
      }
    }
  }
  return errs;
}

/* ============================================================
   STEPPER
   ============================================================ */
const Stepper = ({ label, value, unit, onDec, onInc, fillPct, badge, disabled }) => (
  <div className="rem-stepper-row">
    <div className="rem-stepper-head">
      <span className="lbl">{label}</span>
      <span className="val">{value}<small>{unit}</small> {badge && <span style={{marginLeft:8, color:"var(--fg-3)", fontWeight:600, fontSize:11}}>{badge}</span>}</span>
    </div>
    <div className="rem-stepper-ctl">
      <button className="rem-step-btn" onClick={onDec} disabled={disabled?.dec}>−</button>
      <div className="rem-step-track"><div className="rem-step-fill" style={{ width: `${Math.max(0, Math.min(100, fillPct))}%` }}/></div>
      <button className="rem-step-btn" onClick={onInc} disabled={disabled?.inc}>+</button>
    </div>
  </div>
);

/* ============================================================
   TAB BAR (5 items — Today / History / Stats / Reminders / Settings)
   ============================================================ */
const TabBarRem = ({ active, onChange }) => {
  const tabs = [
    { id: "today",     label: "Today" },
    { id: "history",   label: "History" },
    { id: "stats",     label: "Stats" },
    { id: "reminders", label: "Reminders" },
    { id: "settings",  label: "Settings" },
  ];
  return (
    <div className="dw-tabbar">
      {tabs.map(t => (
        <button key={t.id}
          className={`dw-tab ${active === t.id ? "is-active" : ""}`}
          onClick={() => onChange(t.id)}>
          <TabIconRem name={t.id} active={active === t.id}/>
          <span className="dw-tab-label">{t.label}</span>
        </button>
      ))}
    </div>
  );
};
const TabIconRem = ({ name, active }) => {
  const c = active ? "var(--brand-magenta)" : "var(--ink-400)";
  const f = active ? c : "none";
  const sw = 1.9;
  if (name === "today") return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill={f} stroke={c} strokeWidth={sw} strokeLinejoin="round">
      <path d="M12 3c-4 5-7 9-7 12a7 7 0 0014 0c0-3-3-7-7-12z"/>
    </svg>
  );
  if (name === "history") return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={c} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 12a9 9 0 1 0 3-6.7"/>
      <polyline points="3 4 3 9 8 9"/>
      <path d="M12 8v5l3 2"/>
    </svg>
  );
  if (name === "stats") return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={c} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 17 q 3 -4 6 0 t 6 0 t 6 0"/>
      <path d="M3 12 q 3 -4 6 0 t 6 0 t 6 0" opacity={active ? 1 : 0.5}/>
    </svg>
  );
  if (name === "reminders") return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill={f} stroke={c} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 8a6 6 0 0112 0c0 7 3 9 3 9H3s3-2 3-9z"/>
      <path d="M10 21h4"/>
    </svg>
  );
  if (name === "settings") return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={c} strokeWidth={sw} strokeLinejoin="round">
      <circle cx="12" cy="12" r="3" fill={active ? c : "none"}/>
      <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 11-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 11-4 0v-.09A1.65 1.65 0 008 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 11-2.83-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H2a2 2 0 110-4h.09A1.65 1.65 0 003.6 8a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 112.83-2.83l.06.06a1.65 1.65 0 001.82.33H8a1.65 1.65 0 001-1.51V2a2 2 0 114 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 112.83 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V8a1.65 1.65 0 001.51 1H22a2 2 0 110 4h-.09a1.65 1.65 0 00-1.51 1z"/>
    </svg>
  );
};

/* ============================================================
   MASTER SWITCH
   ============================================================ */
const RemSwitch = ({ on, onChange }) => (
  <button className={`rem-switch ${on ? "is-on" : ""}`} onClick={(e) => { e.stopPropagation(); onChange(!on); }} aria-label="toggle">
    <span className="rem-switch-knob"/>
  </button>
);

Object.assign(window, { fmtTime12, fmtTimeParts, fmtDur, snap5, roundML,
  genByInterval, genByAmount, validate, Stepper, TabBarRem, TabIconRem, RemSwitch });
