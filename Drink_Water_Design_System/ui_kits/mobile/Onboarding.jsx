/* eslint-disable */
/* Drink Water — Onboarding flow steps
   11 sequential screens, all phone-sized (390×780), wired through a single
   onboarding state object. Each step is pure presentation. */

const { useState: useStateOnb } = React;

/* ============================================================
   SHARED SHELL — handles wave header, step counter, back, body
   ============================================================ */
const OnbShell = ({ step, total, title, subtitle, art, children, onBack, footer, narrow }) => (
  <div className="dw-screen dw-onb">
    <div className="dw-onb-art">
      <div className="dw-onb-bubble b1"></div>
      <div className="dw-onb-bubble b2"></div>
      <div className="dw-onb-bubble b3"></div>
      {onBack && (
        <button className="dw-onb-back" onClick={onBack}>
          <Icon name="chevronLeft" size={20} color="#fff"/>
        </button>
      )}
      <div className="dw-onb-progress">
        <div className="dw-onb-progress-fill" style={{ width: `${(step/total)*100}%` }}/>
      </div>
      <div className="dw-onb-counter">{step} of {total}</div>
      {art && <div className="dw-onb-art-figure">{art}</div>}
    </div>
    <div className={`dw-onb-body ${narrow ? "is-narrow" : ""}`}>
      <h1 className="dw-onb-title">{title}</h1>
      {subtitle && <p className="dw-onb-sub">{subtitle}</p>}
      <div className="dw-onb-content">{children}</div>
      {footer && <div className="dw-onb-footer">{footer}</div>}
    </div>
  </div>
);

/* ============================================================
   ART HELPERS
   ============================================================ */
const ArtLogo = () => (
  <img src="../../assets/logo.png" className="dw-onb-logo" alt=""/>
);

const ArtBell = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/>
      <path d="M10 21h4"/>
    </svg>
  </div>
);

const ArtPerson = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="4"/>
      <path d="M4 21a8 8 0 0 1 16 0"/>
    </svg>
  </div>
);

const ArtScale = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="18" height="18" rx="3"/>
      <circle cx="12" cy="12" r="4"/>
      <path d="M12 8 v 4"/>
    </svg>
  </div>
);

const ArtRun = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="13" cy="5" r="2"/>
      <path d="M7 22 l 3 -6 l -2 -4 l 4 -3 l 2 4 l 4 1"/>
      <path d="M5 12 l 3 -2"/>
    </svg>
  </div>
);

const ArtSun = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="4"/>
      <path d="M12 2 v 2 M12 20 v 2 M4.93 4.93 l 1.41 1.41 M17.66 17.66 l 1.41 1.41 M2 12 h 2 M20 12 h 2 M4.93 19.07 l 1.41 -1.41 M17.66 6.34 l 1.41 -1.41"/>
    </svg>
  </div>
);

const ArtMoon = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 12.8 A 9 9 0 1 1 11.2 3 a 7 7 0 0 0 9.8 9.8 z"/>
    </svg>
  </div>
);

const ArtClock = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9"/>
      <path d="M12 7 v 5 l 3 2"/>
    </svg>
  </div>
);

const ArtDrop = () => (
  <div className="dw-onb-icon-circle">
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none">
      <path d="M12 3 c -3.5 4.5 -6 8 -6 11 a 6 6 0 0 0 12 0 c 0 -3 -2.5 -6.5 -6 -11 z" fill="#fff"/>
      <path d="M9 14 a 3 3 0 0 0 3 3" stroke="#1C8EBF" strokeWidth="1.8" strokeLinecap="round" fill="none"/>
    </svg>
  </div>
);

/* ============================================================
   SELECTABLE CARD (gender / activity / interval)
   ============================================================ */
const ChoiceCard = ({ icon, title, sub, selected, onSelect }) => (
  <button className={`dw-choice ${selected ? "is-on" : ""}`} onClick={onSelect}>
    <div className="dw-choice-icon">{icon}</div>
    <div className="dw-choice-body">
      <div className="dw-choice-title">{title}</div>
      {sub && <div className="dw-choice-sub">{sub}</div>}
    </div>
    <div className="dw-choice-tick">
      {selected && <Icon name="check" size={14} color="#fff"/>}
    </div>
  </button>
);

/* ============================================================
   STEP 1 — INTRO / WELCOME
   ============================================================ */
const StepIntro = ({ onNext }) => (
  <OnbShell
    step={1} total={11}
    art={<ArtLogo/>}
    title={<>Welcome to<br/>Drink Water</>}
    subtitle="We'll calculate your daily hydration goal and remind you to drink throughout the day."
    footer={<Button variant="primary" fullWidth onClick={onNext}>Get started</Button>}
  >
    <div className="dw-onb-bullets">
      <div className="dw-onb-bullet"><Icon name="drop" size={18} color="var(--brand-water)"/><span>Personal daily goal</span></div>
      <div className="dw-onb-bullet"><Icon name="bell" size={18} color="var(--brand-magenta)"/><span>Gentle reminders</span></div>
      <div className="dw-onb-bullet"><Icon name="chart" size={18} color="var(--brand-water)"/><span>Track your progress</span></div>
    </div>
  </OnbShell>
);

/* ============================================================
   STEP 2 — NOTIFICATIONS PERMISSION
   ============================================================ */
const StepNotifs = ({ onNext, onSkip, onBack }) => (
  <OnbShell
    step={2} total={11}
    onBack={onBack}
    art={<ArtBell/>}
    title="Stay on track"
    subtitle="We'll send a gentle reminder every 45 minutes so you never forget to hydrate."
    footer={
      <>
        <Button variant="primary" fullWidth onClick={onNext}>Allow notifications</Button>
        <button className="dw-onb-skip" onClick={onSkip}>Maybe later</button>
      </>
    }
  >
    <div className="dw-onb-perm-card">
      <div className="dw-onb-perm-icon">
        <img src="../../assets/logo.png" alt=""/>
      </div>
      <div className="dw-onb-perm-body">
        <div className="dw-onb-perm-title">"Drink Water" would like to send you notifications</div>
        <div className="dw-onb-perm-sub">Notifications may include alerts, sounds and badges.</div>
      </div>
    </div>
  </OnbShell>
);

/* ============================================================
   STEP 3 — NAME
   ============================================================ */
const StepName = ({ value, onChange, onNext, onBack }) => (
  <OnbShell
    step={3} total={11}
    onBack={onBack}
    art={<ArtPerson/>}
    title="What's your name?"
    subtitle="We'll use it to greet you on the home screen."
    footer={<Button variant="primary" fullWidth disabled={!value.trim()} onClick={onNext}>Continue</Button>}
  >
    <div className="dw-onb-textfield">
      <input
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder="Your name"
        autoFocus
      />
    </div>
  </OnbShell>
);

/* ============================================================
   STEP 4 — GENDER (improves goal calc precision)
   ============================================================ */
const GenderIcon = ({ kind, selected }) => {
  const c = selected ? "#fff" : "var(--brand-water)";
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={c} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      {kind === "f" && (<><circle cx="12" cy="9" r="5"/><path d="M12 14 v 7 M9 18 h 6"/></>)}
      {kind === "m" && (<><circle cx="10" cy="14" r="5"/><path d="M14 10 L 20 4 M16 4 h 4 v 4"/></>)}
      {kind === "x" && (<><circle cx="12" cy="12" r="5"/><path d="M8 8 l 8 8 M16 8 l -8 8"/></>)}
    </svg>
  );
};

const StepGender = ({ value, onChange, onNext, onBack }) => (
  <OnbShell
    step={4} total={11}
    onBack={onBack}
    art={<ArtPerson/>}
    title="A bit about you"
    subtitle="Helps us refine your hydration formula. You can skip this."
    footer={<Button variant="primary" fullWidth onClick={onNext}>Continue</Button>}
  >
    <div className="dw-onb-stack">
      {[
        { id: "f", label: "Female" },
        { id: "m", label: "Male" },
        { id: "x", label: "Prefer not to say" },
      ].map(o => (
        <ChoiceCard key={o.id}
          icon={<GenderIcon kind={o.id} selected={value === o.id}/>}
          title={o.label}
          selected={value === o.id}
          onSelect={() => onChange(o.id)}
        />
      ))}
    </div>
  </OnbShell>
);

/* ============================================================
   STEP 5 — WEIGHT
   ============================================================ */
const StepWeight = ({ value, onChange, unit, setUnit, onNext, onBack }) => (
  <OnbShell
    step={5} total={11}
    onBack={onBack}
    art={<ArtScale/>}
    title="What's your weight?"
    subtitle="We use this to calculate your daily water goal."
    footer={<Button variant="primary" fullWidth onClick={onNext}>Continue</Button>}
  >
    <div className="dw-weight-input">
      <input
        value={value}
        onChange={e => onChange(e.target.value)}
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
  </OnbShell>
);

/* ============================================================
   STEP 6 — ACTIVITY LEVEL
   ============================================================ */
const StepActivity = ({ value, onChange, onNext, onBack }) => {
  const opts = [
    { id: "light",  title: "Light",    sub: "Mostly sitting, little exercise",   icon: "moon" },
    { id: "moderate", title: "Moderate", sub: "Walks, light workouts 2–3×/week", icon: "sun" },
    { id: "active", title: "Active",   sub: "Daily training or active job",      icon: "alarm" },
  ];
  return (
    <OnbShell
      step={6} total={11}
      onBack={onBack}
      art={<ArtRun/>}
      title="How active are you?"
      subtitle="Active days need ~15% more water than rest days."
      footer={<Button variant="primary" fullWidth disabled={!value} onClick={onNext}>Continue</Button>}
    >
      <div className="dw-onb-stack">
        {opts.map(o => (
          <ChoiceCard key={o.id}
            icon={<Icon name={o.icon} size={22} color={value === o.id ? "#fff" : "var(--brand-water)"}/>}
            title={o.title} sub={o.sub}
            selected={value === o.id}
            onSelect={() => onChange(o.id)}
          />
        ))}
      </div>
    </OnbShell>
  );
};

/* ============================================================
   STEP 7 — WAKE TIME
   ============================================================ */
const TimeWheel = ({ hour, minute, period, onHour, onMinute, onPeriod }) => (
  <div className="dw-time-wheel">
    <div className="dw-time-cell">
      <button onClick={() => onHour((hour % 12) + 1)}>▲</button>
      <div className="dw-time-num">{hour.toString().padStart(2, "0")}</div>
      <button onClick={() => onHour(hour === 1 ? 12 : hour - 1)}>▼</button>
    </div>
    <div className="dw-time-colon">:</div>
    <div className="dw-time-cell">
      <button onClick={() => onMinute((minute + 5) % 60)}>▲</button>
      <div className="dw-time-num">{minute.toString().padStart(2, "0")}</div>
      <button onClick={() => onMinute((minute - 5 + 60) % 60)}>▼</button>
    </div>
    <div className="dw-time-period">
      {["AM","PM"].map(p => (
        <button key={p}
          className={period === p ? "is-active" : ""}
          onClick={() => onPeriod(p)}>{p}</button>
      ))}
    </div>
  </div>
);

const StepWake = ({ wake, setWake, onNext, onBack }) => (
  <OnbShell
    step={7} total={11}
    onBack={onBack}
    art={<ArtSun/>}
    title="When do you wake up?"
    subtitle="We'll start reminding you about 30 minutes after."
    footer={<Button variant="primary" fullWidth onClick={onNext}>Continue</Button>}
  >
    <TimeWheel
      hour={wake.h} minute={wake.m} period={wake.p}
      onHour={h => setWake({...wake, h})}
      onMinute={m => setWake({...wake, m})}
      onPeriod={p => setWake({...wake, p})}
    />
  </OnbShell>
);

/* ============================================================
   STEP 8 — SLEEP TIME
   ============================================================ */
const StepSleep = ({ sleep, setSleep, onNext, onBack }) => (
  <OnbShell
    step={8} total={11}
    onBack={onBack}
    art={<ArtMoon/>}
    title="And bedtime?"
    subtitle="Reminders pause an hour before, so they don't keep you awake."
    footer={<Button variant="primary" fullWidth onClick={onNext}>Continue</Button>}
  >
    <TimeWheel
      hour={sleep.h} minute={sleep.m} period={sleep.p}
      onHour={h => setSleep({...sleep, h})}
      onMinute={m => setSleep({...sleep, m})}
      onPeriod={p => setSleep({...sleep, p})}
    />
  </OnbShell>
);

/* ============================================================
   STEP 9 — REMINDER INTERVAL
   ============================================================ */
const StepInterval = ({ value, onChange, onNext, onBack }) => {
  const opts = [
    { id: 30, title: "Every 30 minutes", sub: "Most consistent" },
    { id: 45, title: "Every 45 minutes", sub: "Recommended" },
    { id: 60, title: "Every hour",        sub: "Light touch" },
    { id: 120, title: "Every 2 hours",    sub: "Minimal" },
  ];
  return (
    <OnbShell
      step={9} total={11}
      onBack={onBack}
      art={<ArtClock/>}
      title="How often should we remind you?"
      subtitle="You can change this anytime in Settings."
      footer={<Button variant="primary" fullWidth onClick={onNext}>Continue</Button>}
    >
      <div className="dw-onb-stack">
        {opts.map(o => (
          <ChoiceCard key={o.id}
            icon={<Icon name="clock" size={22} color={value === o.id ? "#fff" : "var(--brand-water)"}/>}
            title={o.title} sub={o.sub}
            selected={value === o.id}
            onSelect={() => onChange(o.id)}
          />
        ))}
      </div>
    </OnbShell>
  );
};

/* ============================================================
   STEP 10 — GOAL REVEAL
   ============================================================ */
const StepGoal = ({ goalMl, onNext, onBack }) => (
  <OnbShell
    step={10} total={11}
    onBack={onBack}
    art={<ArtDrop/>}
    title="Your daily goal"
    subtitle="Tailored to your weight and activity level."
    footer={<Button variant="primary" fullWidth onClick={onNext}>Looks good</Button>}
  >
    <div className="dw-goal-reveal">
      <div className="dw-goal-big">{(goalMl/1000).toFixed(2)}<span> L</span></div>
      <div className="dw-goal-eq">≈ {goalMl.toLocaleString()} mL</div>
      <div className="dw-goal-help">
        That's about <b>{Math.round(goalMl / 250)} glasses</b> across your day.
      </div>
    </div>
  </OnbShell>
);

/* ============================================================
   STEP 11 — DONE / SUMMARY
   ============================================================ */
const StepDone = ({ data, onStart, onBack }) => (
  <OnbShell
    step={11} total={11}
    onBack={onBack}
    title={<>You're all set,<br/>{data.name || "friend"}</>}
    subtitle="Here's your starting plan — change any of it later in Settings."
    footer={<Button variant="primary" fullWidth onClick={onStart}>Start drinking</Button>}
    narrow
  >
    <div className="dw-summary">
      <div className="dw-summary-row">
        <div className="dw-summary-icon" style={{ background: "var(--bg-tint-water)" }}>
          <Icon name="drop" size={16} color="var(--brand-water)"/>
        </div>
        <div className="dw-summary-label">Daily goal</div>
        <div className="dw-summary-value">{(data.goalMl/1000).toFixed(2)} L</div>
      </div>
      <div className="dw-summary-row">
        <div className="dw-summary-icon" style={{ background: "var(--bg-tint-magenta)" }}>
          <Icon name="bell" size={16} color="var(--brand-magenta)"/>
        </div>
        <div className="dw-summary-label">Reminder</div>
        <div className="dw-summary-value">every {data.interval} min</div>
      </div>
      <div className="dw-summary-row">
        <div className="dw-summary-icon" style={{ background: "var(--bg-tint-water)" }}>
          <Icon name="sun" size={16} color="var(--brand-water)"/>
        </div>
        <div className="dw-summary-label">Wake</div>
        <div className="dw-summary-value">{data.wake.h}:{String(data.wake.m).padStart(2,"0")} {data.wake.p}</div>
      </div>
      <div className="dw-summary-row">
        <div className="dw-summary-icon" style={{ background: "var(--bg-tint-magenta)" }}>
          <Icon name="moon" size={16} color="var(--brand-magenta)"/>
        </div>
        <div className="dw-summary-label">Bedtime</div>
        <div className="dw-summary-value">{data.sleep.h}:{String(data.sleep.m).padStart(2,"0")} {data.sleep.p}</div>
      </div>
    </div>
  </OnbShell>
);

Object.assign(window, {
  OnbShell, ChoiceCard, TimeWheel,
  ArtLogo, ArtBell, ArtPerson, ArtScale, ArtRun, ArtSun, ArtMoon, ArtClock, ArtDrop,
  StepIntro, StepNotifs, StepName, StepGender, StepWeight, StepActivity,
  StepWake, StepSleep, StepInterval, StepGoal, StepDone
});
