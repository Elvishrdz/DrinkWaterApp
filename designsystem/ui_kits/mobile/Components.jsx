/* eslint-disable */
/* Drink Water — Mobile UI Kit Components
   Modernized recreation of the original Android Drink Water App.
   Visuals follow colors_and_type.css tokens. */

const { useState, useEffect, useRef } = React;

/* ---------- ICONS (inline SVG, lucide-style stroke) ---------- */
const Icon = ({ name, size = 20, color = "currentColor", strokeWidth = 2 }) => {
  const paths = {
    bell: <><path d="M6 8a6 6 0 0112 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10 21h4"/></>,
    clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
    settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 11-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 11-4 0v-.09A1.65 1.65 0 008 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 11-2.83-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H2a2 2 0 110-4h.09A1.65 1.65 0 003.6 8a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 112.83-2.83l.06.06a1.65 1.65 0 001.82.33H8a1.65 1.65 0 001-1.51V2a2 2 0 114 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 112.83 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V8a1.65 1.65 0 001.51 1H22a2 2 0 110 4h-.09a1.65 1.65 0 00-1.51 1z"/></>,
    chart: <><path d="M3 3v18h18"/><path d="M7 14l4-4 4 4 5-7"/></>,
    alarm: <><circle cx="12" cy="13" r="8"/><path d="M12 9v4l2.5 2.5"/><path d="M5 3L2 6"/><path d="M19 3l3 3"/></>,
    share: <><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4"/></>,
    image: <><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="9" cy="9" r="2"/><path d="M21 15l-5-5L5 21"/></>,
    star: <><polygon points="12 2 15 9 22 9.5 17 14.5 18.5 22 12 18 5.5 22 7 14.5 2 9.5 9 9 12 2"/></>,
    moon: <><path d="M21 12.8A9 9 0 1111.2 3a7 7 0 009.8 9.8z"/></>,
    sun: <><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/></>,
    chevronLeft: <><polyline points="15 18 9 12 15 6"/></>,
    chevronRight: <><polyline points="9 18 15 12 9 6"/></>,
    chevronDown: <><polyline points="6 9 12 15 18 9"/></>,
    plus: <><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></>,
    check: <><polyline points="5 12 10 17 20 7"/></>,
    drop: <><path d="M12 3s7 7 7 12a7 7 0 11-14 0c0-5 7-12 7-12z"/></>,
    glass: <><path d="M7 4h10l-1 16a2 2 0 01-2 2h-4a2 2 0 01-2-2L7 4z"/><path d="M7.4 9h9.2"/></>,
  };
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      {paths[name]}
    </svg>
  );
};

/* ---------- BUTTONS ---------- */
const Button = ({ children, variant = "primary", icon, onClick, disabled, fullWidth }) => (
  <button
    onClick={onClick}
    disabled={disabled}
    className={`dw-btn dw-btn-${variant} ${fullWidth ? "dw-btn-full" : ""} ${disabled ? "is-disabled" : ""}`}
  >
    {icon && <Icon name={icon} size={16} />}
    <span>{children}</span>
  </button>
);

/* ---------- ROUND ICON BUTTON (toolbar magenta circles) ---------- */
const IconButton = ({ name, onClick, active, size = 44 }) => (
  <button
    onClick={onClick}
    className={`dw-iconbtn ${active ? "is-active" : ""}`}
    style={{ width: size, height: size }}
    aria-label={name}
  >
    <Icon name={name} size={Math.round(size * 0.45)} color="#fff" />
  </button>
);

/* ---------- STATUS PILL (top "you carry" pill) ---------- */
const CarryPill = ({ current, goal, unit = "mL" }) => (
  <div className="dw-carrypill">
    You carry <b>{current} {unit}</b> of <b>{goal} {unit}</b>
  </div>
);

/* ---------- DRINK CTA (concentric ripple) ---------- */
const DrinkButton = ({ onTap, label = "Drink water" }) => {
  const [pressed, setPressed] = useState(false);
  return (
    <button
      className={`dw-drink-btn ${pressed ? "is-pressed" : ""}`}
      onPointerDown={() => setPressed(true)}
      onPointerUp={() => { setPressed(false); onTap && onTap(); }}
      onPointerLeave={() => setPressed(false)}
    >
      <span className="ring r1"></span>
      <span className="ring r2"></span>
      <span className="ring r3">
        <span className="dw-drink-label">{label.toUpperCase()}</span>
      </span>
    </button>
  );
};

/* ---------- STAT CARD ---------- */
const StatCard = ({ label, value, unit, tone = "magenta", caption, onClick }) => (
  <div className={`dw-stat dw-stat-${tone}`} onClick={onClick}>
    <div className="dw-stat-label">{label}</div>
    <div className="dw-stat-value">
      {value}
      {unit && <span className="dw-stat-unit"> {unit}</span>}
    </div>
    {caption && <div className="dw-stat-caption">{caption}</div>}
  </div>
);

/* ---------- TIME ROW (start time / end time) ---------- */
const TimeRow = ({ icon, title, time, onClick }) => (
  <div className="dw-timerow" onClick={onClick}>
    <div className="dw-timerow-body">
      <div className="dw-timerow-title">{title}</div>
      <div className="dw-timerow-time">{time}</div>
    </div>
    <div className="dw-timerow-icon">
      <Icon name={icon} size={20} color="#fff" />
    </div>
  </div>
);

/* ---------- NOTIFICATION CONFIG (skip + drink amounts) ---------- */
const NotificationConfig = ({ skip, drink, onSkipChange, onDrinkChange }) => (
  <div className="dw-notif-config">
    <div className="dw-notif-title">Notification settings</div>
    <div className="dw-notif-grid">
      <div>
        <div className="dw-notif-cell-label">"SKIP" during</div>
        <Stepper value={skip} options={["5min","10min","15min","20min"]} onChange={onSkipChange} />
      </div>
      <div>
        <div className="dw-notif-cell-label">"DRINK" amount</div>
        <Stepper value={drink} options={["10mL","50mL","100mL","150mL"]} onChange={onDrinkChange} />
      </div>
    </div>
  </div>
);

const Stepper = ({ value, options, onChange }) => {
  const idx = options.indexOf(value);
  return (
    <div className="dw-stepper">
      <span className="dw-stepper-value">{value}</span>
      <button
        onClick={() => onChange(options[(idx + 1) % options.length])}
        className="dw-stepper-btn"
      >
        <Icon name="chevronDown" size={14} color="#fff" />
      </button>
    </div>
  );
};

/* ---------- RECORD ROW ---------- */
const RecordRow = ({ time, amount }) => (
  <div className="dw-record">
    <div className="dw-record-time">{time}</div>
    <div className="dw-record-amount">+{amount} mL</div>
    <div className="dw-record-dot">
      <Icon name="check" size={14} color="#fff" />
    </div>
  </div>
);

/* ---------- ONBOARDING SHELL (welcome card) ---------- */
const WelcomeCard = ({ title, children, onBack, onNext, footer }) => (
  <div className="dw-welcome-shell">
    <div className="dw-welcome-wave">
      <h1>{title}</h1>
    </div>
    <div className="dw-welcome-card">
      {children}
      <div className="dw-welcome-nav">
        {onBack && <button className="dw-roundbtn dw-roundbtn-back" onClick={onBack}><Icon name="chevronLeft" size={20} color="#fff"/></button>}
        <span style={{ flex: 1 }}></span>
        {onNext && <button className="dw-roundbtn dw-roundbtn-next" onClick={onNext}><Icon name="chevronRight" size={20} color="#fff"/></button>}
      </div>
      {footer}
    </div>
  </div>
);

/* ---------- AMOUNT GRID (cuanto bebiste) ---------- */
const AmountGrid = ({ amounts, onSelect }) => (
  <div className="dw-amount-grid">
    {amounts.map(a => (
      <button key={a} className="dw-amount-tile" onClick={() => onSelect(a)}>
        <Icon name="plus" size={14} color="#6B8290" />
        <div className="dw-amount-tile-num">{a}</div>
        <div className="dw-amount-tile-unit">mL</div>
      </button>
    ))}
  </div>
);

/* ---------- PROGRESS RING (background ripple under DrinkButton) ---------- */
const HydrationBar = ({ percent }) => (
  <div className="dw-hbar">
    <div className="dw-hbar-fill" style={{ width: percent + "%" }}></div>
  </div>
);

/* expose */
Object.assign(window, {
  Icon, Button, IconButton, CarryPill, DrinkButton, StatCard,
  TimeRow, NotificationConfig, Stepper, RecordRow,
  WelcomeCard, AmountGrid, HydrationBar
});
