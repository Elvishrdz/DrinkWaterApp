/* eslint-disable */
/* Drink Water — Modern app screens
   Tab-bar architecture: Today · Stats · Settings.
   Onboarding flow lives outside the tabs. */

const { useState: useStateScreens } = React;

/* ---------- TAB BAR (shared chrome) ---------- */
const TabBar = ({ active, onChange }) => {
  const tabs = [
    { id: "today", label: "Today", icon: "drop" },
    { id: "history", label: "History", icon: "history" },
    { id: "stats", label: "Stats", icon: "wave" },
    { id: "settings", label: "Settings", icon: "settings" },
  ];
  return (
    <div className="dw-tabbar">
      {tabs.map(t => (
        <button key={t.id}
          className={`dw-tab ${active === t.id ? "is-active" : ""}`}
          onClick={() => onChange(t.id)}>
          <TabIcon name={t.icon} active={active === t.id}/>
          <span className="dw-tab-label">{t.label}</span>
        </button>
      ))}
    </div>
  );
};

const TabIcon = ({ name, active }) => {
  const c = active ? "var(--brand-magenta)" : "var(--ink-400)";
  if (name === "history") return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke={c} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 12a9 9 0 1 0 3-6.7"/>
      <polyline points="3 4 3 9 8 9"/>
      <path d="M12 8v5l3 2"/>
    </svg>
  );
  if (name === "drop") return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? c : "none"} stroke={c} strokeWidth="2" strokeLinejoin="round">
      <path d="M12 3 c -4 5 -7 9 -7 12 a7 7 0 0 0 14 0 c 0 -3 -3 -7 -7 -12 z"/>
    </svg>
  );
  if (name === "wave") return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke={c} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 17 q 3 -4 6 0 t 6 0 t 6 0"/>
      <path d="M3 12 q 3 -4 6 0 t 6 0 t 6 0" opacity={active ? "1" : "0.55"}/>
    </svg>
  );
  if (name === "settings") return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill={active ? c : "none"} stroke={c} strokeWidth="2" strokeLinejoin="round">
      <circle cx="12" cy="12" r="3" fill={active ? "#fff" : "none"}/>
      <path d="M12 3 l 1.6 1.6 h 2.2 l 0.6 2.2 l 2.2 0.6 v 2.2 l 1.6 1.6 l -1.6 1.6 v 2.2 l -2.2 0.6 l -0.6 2.2 h -2.2 l -1.6 1.6 l -1.6 -1.6 h -2.2 l -0.6 -2.2 l -2.2 -0.6 v -2.2 l -1.6 -1.6 l 1.6 -1.6 v -2.2 l 2.2 -0.6 l 0.6 -2.2 h 2.2 z"/>
    </svg>
  );
};

/* ---------- PROGRESS RING (hero) ---------- */
const ProgressRing = ({ percent, current, goal, unit = "mL" }) => {
  const r = 110, c = 2 * Math.PI * r;
  const dash = (percent / 100) * c;
  return (
    <div className="dw-ring-wrap">
      <svg viewBox="0 0 260 260" className="dw-ring">
        <defs>
          <linearGradient id="ringGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stopColor="#4FB3DD"/>
            <stop offset="1" stopColor="#0E5A82"/>
          </linearGradient>
        </defs>
        <circle cx="130" cy="130" r={r} stroke="rgba(28,142,191,0.15)" strokeWidth="14" fill="none"/>
        <circle cx="130" cy="130" r={r}
          stroke="url(#ringGrad)" strokeWidth="14" fill="none"
          strokeLinecap="round"
          strokeDasharray={`${dash} ${c}`}
          transform="rotate(-90 130 130)"
          style={{ transition: "stroke-dasharray 600ms var(--ease-out)" }}
        />
      </svg>
      <div className="dw-ring-center">
        <div className="dw-ring-pct">{Math.round(percent)}%</div>
        <div className="dw-ring-amount">{current.toLocaleString()} <span>{unit}</span></div>
        <div className="dw-ring-goal">of {goal.toLocaleString()} {unit}</div>
      </div>
    </div>
  );
};

/* ---------- QUICK-ADD CHIPS (today screen) ---------- */
const QuickAddRow = ({ onAdd, onCustom }) => {
  const presets = [100, 250, 500];
  return (
    <div className="dw-quickadd">
      {presets.map(p => (
        <button key={p} className="dw-quickadd-chip" onClick={() => onAdd(p)}>
          <Icon name="glass" size={20} color="var(--brand-water)"/>
          <span>{p}<small>mL</small></span>
        </button>
      ))}
      <button className="dw-quickadd-chip dw-quickadd-custom" onClick={onCustom}>
        <Icon name="plus" size={20} color="var(--brand-magenta)"/>
        <span>Custom</span>
      </button>
    </div>
  );
};

/* ---------- TODAY-LOG STRIP ---------- */
const TodayLog = ({ items, onSeeAll }) => (
  <div className="dw-todaylog">
    <div className="dw-todaylog-head">
      <div className="dw-section-title">Today</div>
      <button className="dw-link" onClick={onSeeAll}>See all</button>
    </div>
    <div className="dw-todaylog-body">
      {items.length === 0 && (
        <div className="dw-empty">No drinks logged yet — tap <b>Add water</b> to start.</div>
      )}
      {items.map((it, i) => (
        <div className="dw-log-row" key={i}>
          <div className="dw-log-icon"><Icon name="glass" size={16} color="#fff"/></div>
          <div className="dw-log-amount">{it.amount} mL</div>
          <div className="dw-log-time">{it.time}</div>
        </div>
      ))}
    </div>
  </div>
);

/* ---------- 1. SPLASH ---------- */
const SplashScreen = () => (
  <div className="dw-screen dw-screen-splash">
    <div className="splash-mark">
      <div className="splash-ring r1"></div>
      <div className="splash-ring r2"></div>
      <div className="splash-ring r3"></div>
      <img src="../../assets/logo.png" alt="Drink Water"/>
    </div>
    <div className="splash-name">DRINK WATER</div>
    <div className="splash-tag">Stay hydrated, every hour</div>
  </div>
);

/* ---------- 2. WELCOME ---------- */
const WelcomeStep = ({ onNext }) => (
  <div className="dw-screen dw-onboard">
    <div className="dw-onboard-art">
      <div className="dw-onboard-bubble b1"></div>
      <div className="dw-onboard-bubble b2"></div>
      <div className="dw-onboard-bubble b3"></div>
      <img src="../../assets/logo.png" className="dw-onboard-logo" alt=""/>
    </div>
    <div className="dw-onboard-body">
      <div className="dw-onboard-step">Step 1 of 3</div>
      <h1 className="dw-onboard-title">Welcome to<br/>Drink Water</h1>
      <p className="dw-onboard-sub">
        We'll help you reach your daily hydration goal — one gentle nudge at a time.
      </p>
      <Button variant="primary" fullWidth onClick={onNext}>Get started</Button>
    </div>
  </div>
);

/* ---------- 3. WEIGHT ---------- */
const WeightStep = ({ weight, setWeight, unit, setUnit, onBack, onNext }) => (
  <div className="dw-screen dw-onboard">
    <div className="dw-onboard-art shrunk">
      <div className="dw-onboard-bubble b1"></div>
      <div className="dw-onboard-bubble b2"></div>
      <button className="dw-back" onClick={onBack}><Icon name="chevronLeft" size={20} color="#fff"/></button>
    </div>
    <div className="dw-onboard-body">
      <div className="dw-onboard-step">Step 2 of 3</div>
      <h1 className="dw-onboard-title">What's<br/>your weight?</h1>
      <p className="dw-onboard-sub">We use this to calculate your daily water goal.</p>

      <div className="dw-weight-input">
        <input
          value={weight}
          onChange={e => setWeight(e.target.value)}
          inputMode="numeric"
        />
        <div className="dw-weight-unit-toggle">
          {["lb","kg"].map(u => (
            <button key={u}
              className={`dw-pill-toggle ${unit === u ? "is-active" : ""}`}
              onClick={() => setUnit(u)}>{u}</button>
          ))}
        </div>
      </div>

      <Button variant="primary" fullWidth onClick={onNext}>Continue</Button>
    </div>
  </div>
);

/* ---------- 4. READY ---------- */
const ReadyStep = ({ goalMl, interval = 45, onStart, onBack }) => (
  <div className="dw-screen dw-onboard">
    <div className="dw-onboard-art shrunk">
      <div className="dw-onboard-bubble b1"></div>
      <div className="dw-onboard-bubble b2"></div>
      <button className="dw-back" onClick={onBack}><Icon name="chevronLeft" size={20} color="#fff"/></button>
    </div>
    <div className="dw-onboard-body">
      <div className="dw-onboard-step">Step 3 of 3</div>
      <h1 className="dw-onboard-title">You're<br/>all set</h1>

      <div className="dw-ready-card">
        <div className="dw-ready-row">
          <div className="dw-ready-label">Daily goal</div>
          <div className="dw-ready-value">{(goalMl/1000).toFixed(2)} L</div>
        </div>
        <div className="dw-ready-divider"/>
        <div className="dw-ready-row">
          <div className="dw-ready-label">Reminder every</div>
          <div className="dw-ready-value">{interval} min</div>
        </div>
        <div className="dw-ready-divider"/>
        <div className="dw-ready-row">
          <div className="dw-ready-label">Active hours</div>
          <div className="dw-ready-value">6:00 a.m. – 11:55 p.m.</div>
        </div>
      </div>

      <p className="dw-onboard-sub" style={{ marginTop: 8 }}>
        You can change any of this in Settings later.
      </p>
      <Button variant="primary" fullWidth onClick={onStart}>Start drinking</Button>
    </div>
  </div>
);

/* ---------- 5. TODAY (modern home) ---------- */
const TodayScreen = ({ current, goal, log, name = "Alex", streak = 4, onAdd, onCustom, onSeeAll }) => {
  const pct = Math.min(100, (current / goal) * 100);
  const remaining = Math.max(0, goal - current);
  return (
    <div className="dw-screen dw-screen-today">
      <div className="dw-today-head">
        <div>
          <div className="dw-greet">Good morning,</div>
          <div className="dw-greet-name">{name}</div>
        </div>
        <div className="dw-streak">
          <Icon name="star" size={14} color="#fff"/>
          <span>{streak}-day streak</span>
        </div>
      </div>

      <div className="dw-today-hero">
        <ProgressRing percent={pct} current={current} goal={goal}/>
        <div className="dw-today-remaining">
          {remaining > 0
            ? <>You're <b>{remaining.toLocaleString()} mL</b> away from today's goal</>
            : <><b>Goal reached</b> — keep sipping to stay ahead</>}
        </div>
      </div>

      <QuickAddRow onAdd={onAdd} onCustom={onCustom}/>
      <TodayLog items={log} onSeeAll={onSeeAll}/>
    </div>
  );
};

/* ---------- 6. ADD-DRINK SHEET ---------- */
const AddDrinkSheet = ({ onClose, onAdd }) => {
  const presets = [100, 200, 250, 330, 400, 500];
  const [custom, setCustom] = useStateScreens("");
  return (
    <div className="dw-sheet">
      <div className="dw-sheet-grip"/>
      <div className="dw-sheet-head">
        <h2>Add water</h2>
        <button className="dw-sheet-close" onClick={onClose}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--ink-500)" strokeWidth="2" strokeLinecap="round"><path d="M6 6l12 12M18 6l-12 12"/></svg>
        </button>
      </div>

      <div className="dw-sheet-presets">
        {presets.map(p => (
          <button key={p} className="dw-preset-tile" onClick={() => onAdd(p)}>
            <PresetGlass amount={p}/>
            <div className="dw-preset-amount">{p} <small>mL</small></div>
          </button>
        ))}
      </div>

      <div className="dw-sheet-custom-row">
        <div className="dw-sheet-custom-input">
          <input
            value={custom}
            onChange={e => setCustom(e.target.value.replace(/\D/g,""))}
            placeholder="Custom amount"
            inputMode="numeric"
          />
          <span className="dw-sheet-unit">mL</span>
        </div>
        <Button variant="primary" disabled={!custom} onClick={() => { if(custom) { onAdd(+custom); setCustom(""); }}}>Add</Button>
      </div>
    </div>
  );
};

/* preset glass — fills proportional to amount */
const PresetGlass = ({ amount }) => {
  const fillPct = Math.min(95, Math.max(20, (amount / 600) * 95));
  return (
    <svg viewBox="0 0 40 48" width="36" height="44">
      <defs>
        <linearGradient id={`pg${amount}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#4FB3DD"/>
          <stop offset="1" stopColor="#1C8EBF"/>
        </linearGradient>
        <clipPath id={`pgc${amount}`}>
          <path d="M9 10 H31 L29 42 a2 2 0 0 1 -2 1.8 H13 a2 2 0 0 1 -2 -1.8 Z"/>
        </clipPath>
      </defs>
      <path d="M9 10 H31 L29 42 a2 2 0 0 1 -2 1.8 H13 a2 2 0 0 1 -2 -1.8 Z"
            fill="rgba(28,142,191,0.10)" stroke="var(--brand-water)" strokeWidth="1.6"/>
      <rect x="9" y={48 - (fillPct * 0.4)} width="22" height={fillPct * 0.4}
            fill={`url(#pg${amount})`} clipPath={`url(#pgc${amount})`}/>
    </svg>
  );
};

/* ---------- 7. STATS ---------- */
const StatsScreen = ({ days, weekly, avg, streak, best }) => (
  <div className="dw-screen dw-screen-stats">
    <div className="dw-stats-head">
      <h1>Stats</h1>
      <div className="dw-segmented">
        {["Week","Month","Year"].map((s,i) => (
          <button key={s} className={i===0 ? "is-active" : ""}>{s}</button>
        ))}
      </div>
    </div>

    <div className="dw-stats-summary">
      <div className="dw-stat-block">
        <div className="dw-stat-block-label">This week's avg</div>
        <div className="dw-stat-block-value">{avg.toLocaleString()} <small>mL/day</small></div>
        <div className="dw-stat-block-trend up">▲ 12% vs last week</div>
      </div>
    </div>

    <div className="dw-chart-card">
      <div className="dw-chart-head">
        <span>Daily intake</span>
        <span className="dw-chart-goal">goal {weekly.goal} mL</span>
      </div>
      <div className="dw-chart">
        {days.map((d,i) => {
          const h = Math.min(100, (d.amount / weekly.goal) * 100);
          const reached = d.amount >= weekly.goal;
          return (
            <div className="dw-bar-col" key={i}>
              <div className="dw-bar">
                <div className={`dw-bar-fill ${reached ? "is-met" : ""}`} style={{ height: h + "%" }}/>
              </div>
              <div className={`dw-bar-day ${d.today ? "is-today" : ""}`}>{d.day}</div>
            </div>
          );
        })}
      </div>
      <div className="dw-chart-legend">
        <span><i className="met"></i>Goal met</span>
        <span><i className="below"></i>Below goal</span>
      </div>
    </div>

    <div className="dw-mini-cards">
      <div className="dw-mini-card">
        <div className="dw-mini-icon" style={{ background: "var(--bg-tint-magenta)" }}>
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="var(--brand-magenta)" strokeWidth="2" strokeLinejoin="round"><path d="M12 3 c -1 4 -5 5 -5 10 a8 8 0 0 0 16 0 c 0 -2 -1.4 -3.5 -2.5 -4 c -0.4 1 -1.1 1.6 -1.8 1.6 c 0 -2.5 -1.8 -5 -6.7 -7.6 z"/></svg>
        </div>
        <div className="dw-mini-num">{streak}</div>
        <div className="dw-mini-lbl">Day streak</div>
      </div>
      <div className="dw-mini-card">
        <div className="dw-mini-icon" style={{ background: "var(--bg-tint-water)" }}>
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="var(--brand-water)" strokeWidth="2" strokeLinejoin="round"><path d="M7 4h10l-1 16a2 2 0 0 1 -2 2h-4a2 2 0 0 1 -2 -2L7 4z"/><path d="M7.4 9h9.2"/></svg>
        </div>
        <div className="dw-mini-num">{best.toLocaleString()}</div>
        <div className="dw-mini-lbl">Personal best (mL)</div>
      </div>
    </div>
  </div>
);

/* ---------- 8. SETTINGS ---------- */
const SettingsScreen = ({ goal, interval, startTime, endTime, units, notif, setNotif, onEdit }) => (
  <div className="dw-screen dw-screen-settings">
    <div className="dw-settings-head">
      <h1>Settings</h1>
    </div>

    <div className="dw-profile-card">
      <div className="dw-avatar">A</div>
      <div className="dw-profile-body">
        <div className="dw-profile-name">Alex</div>
        <div className="dw-profile-meta">68 kg · daily goal {(goal/1000).toFixed(2)} L</div>
      </div>
      <button className="dw-profile-edit" onClick={() => onEdit("profile")}>Edit</button>
    </div>

    <div className="dw-list-section">
      <div className="dw-list-section-title">Hydration</div>
      <SettingRow icon="drop" iconBg="var(--bg-tint-water)" iconColor="var(--brand-water)"
        label="Daily goal" value={`${(goal/1000).toFixed(2)} L`} onClick={() => onEdit("goal")}/>
      <SettingRow icon="ruler" iconBg="var(--bg-tint-magenta)" iconColor="var(--brand-magenta)"
        label="Units" value={units} onClick={() => onEdit("units")}/>
    </div>

    <div className="dw-list-section">
      <div className="dw-list-section-title">Reminders</div>
      <SettingRow icon="bell" iconBg="var(--bg-tint-magenta)" iconColor="var(--brand-magenta)"
        label="Notifications" trailing={<Switch on={notif} onChange={setNotif}/>}/>
      <SettingRow icon="clock" iconBg="var(--bg-tint-water)" iconColor="var(--brand-water)"
        label="Interval" value={`every ${interval} min`} onClick={() => onEdit("interval")}/>
      <SettingRow icon="sun" iconBg="var(--bg-tint-water)" iconColor="var(--brand-water)"
        label="Start time" value={startTime} onClick={() => onEdit("start")}/>
      <SettingRow icon="moon" iconBg="var(--bg-tint-magenta)" iconColor="var(--brand-magenta)"
        label="End time" value={endTime} onClick={() => onEdit("end")}/>
    </div>

    <div className="dw-list-section">
      <div className="dw-list-section-title">About</div>
      <SettingRow icon="share" iconBg="var(--ink-100)" iconColor="var(--ink-500)"
        label="Share with friends" onClick={() => onEdit("share")}/>
      <SettingRow icon="star" iconBg="var(--ink-100)" iconColor="var(--ink-500)"
        label="Rate the app" onClick={() => onEdit("rate")}/>
    </div>
  </div>
);

const SettingRow = ({ icon, iconBg, iconColor, label, value, trailing, onClick }) => (
  <button className="dw-setting-row" onClick={onClick}>
    <div className="dw-setting-icon" style={{ background: iconBg }}>
      <SettingIcon name={icon} color={iconColor}/>
    </div>
    <div className="dw-setting-label">{label}</div>
    {trailing
      ? <div className="dw-setting-trailing">{trailing}</div>
      : <div className="dw-setting-value">
          {value && <span>{value}</span>}
          <Icon name="chevronRight" size={16} color="var(--ink-400)"/>
        </div>}
  </button>
);

const SettingIcon = ({ name, color }) => {
  const props = { width: 18, height: 18, viewBox: "0 0 24 24", fill: "none", stroke: color, strokeWidth: 2, strokeLinecap: "round", strokeLinejoin: "round" };
  if (name === "drop") return <svg {...props}><path d="M12 3 c -3.5 4.5 -6 8 -6 11 a 6 6 0 0 0 12 0 c 0 -3 -2.5 -6.5 -6 -11 z"/></svg>;
  if (name === "ruler") return <svg {...props}><rect x="3" y="9" width="18" height="6" rx="1"/><path d="M7 9v3M11 9v4M15 9v3M19 9v4"/></svg>;
  if (name === "bell") return <svg {...props}><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10 21h4"/></svg>;
  if (name === "clock") return <svg {...props}><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>;
  if (name === "sun") return <svg {...props}><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/></svg>;
  if (name === "moon") return <svg {...props}><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/></svg>;
  if (name === "share") return <svg {...props}><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4"/></svg>;
  if (name === "star") return <svg {...props}><polygon points="12 2 15 9 22 9.5 17 14.5 18.5 22 12 18 5.5 22 7 14.5 2 9.5 9 9 12 2"/></svg>;
  return null;
};

const Switch = ({ on, onChange }) => (
  <button className={`dw-switch ${on ? "is-on" : ""}`} onClick={(e) => { e.stopPropagation(); onChange(!on); }}>
    <span className="dw-switch-knob"/>
  </button>
);

/* ---------- 9. NOTIFICATION (push) ---------- */
const NotificationDemo = () => (
  <div className="dw-screen dw-screen-notify">
    <div className="dw-notify-time">9:41</div>
    <div className="dw-push">
      <div className="dw-push-head">
        <img src="../../assets/logo.png" alt=""/>
        <div className="dw-push-title">DRINK WATER</div>
        <div className="dw-push-time">now</div>
      </div>
      <div className="dw-push-body">
        <div className="dw-push-headline">Time to hydrate</div>
        <div className="dw-push-text">You're 870 mL away from today's goal — a quick glass should do it.</div>
      </div>
      <div className="dw-push-actions">
        <button className="dw-push-action skip">Skip</button>
        <button className="dw-push-action drink">+ 250 mL</button>
      </div>
    </div>
  </div>
);

Object.assign(window, {
  TabBar, ProgressRing, QuickAddRow, TodayLog, PresetGlass,
  SettingRow, Switch,
  SplashScreen, WelcomeStep, WeightStep, ReadyStep,
  TodayScreen, AddDrinkSheet, StatsScreen, SettingsScreen, NotificationDemo
});
