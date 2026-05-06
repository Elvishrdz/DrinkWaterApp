/* eslint-disable */
/* Drink Water — Goal Reached Celebration Overlay
   Floating modal that surfaces the moment a user hits their daily goal.
   The visual treatment + copy rotate daily so the moment doesn't get stale. */

const { useMemo: useMemoCel, useState: useStateCel } = React;

/* ---------- DAILY VARIANTS ---------- */
/* Six themes; one is selected by day-of-year so adjacent days never repeat.
   Each variant pairs:
   - theme  : hero background + decoration treatment (CSS class)
   - decor  : ambient particle preset
   - mark   : trophy / drop / star asset shown in the medallion
   - eyebrow: small chip text above headline
   - headline + sub : the cheerful copy, rotated each day
   - cta    : primary button label
*/
const CELEBRATIONS = [
  {
    id: "ocean",
    theme: "theme-ocean",
    decor: "bubbles",
    mark: { kind: "img", src: "../../assets/ic_trophy_white.png" },
    eyebrow: "Goal · Day 1",
    headline: "You did it. Today belongs to your future self.",
    sub: "Every glass added up — and so did the focus, the energy, the calm.",
    cta: "Cheers to that",
  },
  {
    id: "carnival",
    theme: "theme-carnival",
    decor: "confetti",
    mark: { kind: "glyph", text: "🎉", fallback: "★" },
    eyebrow: "Daily goal — smashed",
    headline: "2.2 litres later, you're a hydration headliner.",
    sub: "That's a streak in the making. The body is 60% water — yours is freshly topped up.",
    cta: "Keep the party going",
  },
  {
    id: "starlight",
    theme: "theme-starlight",
    decor: "sparkles",
    mark: { kind: "img", src: "../../assets/ic_trophy_yellow.png" },
    eyebrow: "Stellar work",
    headline: "A goal met quietly is a goal met twice.",
    sub: "No fanfare needed — your skin, joints and brain felt every glass of this one.",
    cta: "Rest easy",
  },
  {
    id: "rain",
    theme: "theme-rain",
    decor: "drips",
    mark: { kind: "img", src: "../../assets/ic_drink_blue.png" },
    eyebrow: "Goal reached",
    headline: "Make it rain. (You already did.)",
    sub: "Eight cups, give or take. The plants are jealous. Tomorrow's a fresh forecast.",
    cta: "Nice. I'm done",
  },
  {
    id: "sunset",
    theme: "theme-sunset",
    decor: "rays",
    mark: { kind: "glyph", text: "✦" },
    eyebrow: "Bonus territory",
    headline: "Goal cleared with daylight to spare.",
    sub: "Every sip from here is overtime — proud of you for showing up before the end credits.",
    cta: "Log a bonus glass",
  },
  {
    id: "moonlit",
    theme: "theme-moonlit",
    decor: "rings",
    mark: { kind: "glyph", text: "☾" },
    eyebrow: "Late but legendary",
    headline: "The goal didn't go to bed without you.",
    sub: "A little persistence, a lot of water. Set tomorrow up — your future self thanks you.",
    cta: "Tuck it in",
  },
];

/* day-of-year, used as a deterministic rotator so the theme is stable per day */
function dayOfYear(d = new Date()) {
  const start = new Date(d.getFullYear(), 0, 0);
  const diff = (d - start) + ((start.getTimezoneOffset() - d.getTimezoneOffset()) * 60 * 1000);
  return Math.floor(diff / 86400000);
}

/* AMBIENT DECORATIONS — pure-CSS animated particles per theme */
const Decor = ({ kind }) => {
  if (kind === "bubbles") {
    const bubbles = [
      { l: "12%", t: "60%", s: 18, d: 0 },
      { l: "78%", t: "70%", s: 26, d: 0.6 },
      { l: "22%", t: "30%", s: 12, d: 1.2 },
      { l: "62%", t: "20%", s: 16, d: 1.8 },
      { l: "44%", t: "78%", s: 22, d: 0.3 },
      { l: "88%", t: "40%", s: 10, d: 2.4 },
    ];
    return (
      <div className="dw-celebrate-decor">
        {bubbles.map((b, i) => (
          <span key={i} className="dw-bubble"
            style={{ left: b.l, top: b.t, width: b.s, height: b.s,
                     animationDelay: b.d + "s" }}/>
        ))}
      </div>
    );
  }
  if (kind === "confetti") {
    const colors = ["#FFFFFF", "#FFE0F0", "#4FB3DD", "#FFD37A", "#FFFFFF", "#D81B60"];
    const items = Array.from({ length: 14 }).map((_, i) => ({
      l: ((i * 7.3) % 92 + 4) + "%",
      t: -10 - (i % 3) * 6,
      c: colors[i % colors.length],
      d: (i % 7) * 0.32,
      r: (i % 2 ? 12 : -8) + "deg",
    }));
    return (
      <div className="dw-celebrate-decor">
        {items.map((c, i) => (
          <span key={i} className="dw-confetti"
            style={{ left: c.l, top: c.t, background: c.c,
                     animationDelay: c.d + "s",
                     transform: `rotate(${c.r})` }}/>
        ))}
      </div>
    );
  }
  if (kind === "sparkles") {
    const stars = [
      { l: "10%", t: "18%", s: 14, d: 0 },
      { l: "82%", t: "12%", s: 10, d: 0.5 },
      { l: "70%", t: "70%", s: 16, d: 1.0 },
      { l: "16%", t: "78%", s: 12, d: 1.5 },
      { l: "48%", t: "8%",  s: 8,  d: 0.3 },
      { l: "92%", t: "55%", s: 10, d: 1.2 },
      { l: "30%", t: "50%", s: 8,  d: 1.8 },
    ];
    return (
      <div className="dw-celebrate-decor">
        {stars.map((s, i) => (
          <svg key={i} className="dw-spark"
            style={{ left: s.l, top: s.t, width: s.s, height: s.s,
                     animationDelay: s.d + "s" }}
            viewBox="0 0 24 24" fill="#fff">
            <path d="M12 2 L13.5 9 L21 12 L13.5 15 L12 22 L10.5 15 L3 12 L10.5 9 Z"/>
          </svg>
        ))}
      </div>
    );
  }
  if (kind === "drips") {
    const drips = Array.from({ length: 10 }).map((_, i) => ({
      l: (8 + i * 9) + "%",
      d: (i % 5) * 0.28,
      h: 10 + (i % 3) * 4,
    }));
    return (
      <div className="dw-celebrate-decor">
        {drips.map((d, i) => (
          <span key={i} className="dw-drip"
            style={{ left: d.l, top: -16, height: d.h,
                     animationDelay: d.d + "s" }}/>
        ))}
      </div>
    );
  }
  if (kind === "rays") {
    const rays = Array.from({ length: 9 }).map((_, i) => ({
      l: (10 + i * 10) + "%",
      h: 60 + (i % 3) * 20,
      r: ((i - 4) * 6) + "deg",
      d: (i % 4) * 0.4,
    }));
    return (
      <div className="dw-celebrate-decor">
        {rays.map((r, i) => (
          <span key={i} className="dw-ray"
            style={{ left: r.l, top: 0, height: r.h,
                     transform: `rotate(${r.r})`,
                     animationDelay: r.d + "s" }}/>
        ))}
      </div>
    );
  }
  if (kind === "rings") {
    const rings = [
      { c: "30%", s: 60, d: 0 },
      { c: "60%", s: 90, d: 0.6 },
      { c: "75%", s: 50, d: 1.2 },
    ];
    return (
      <div className="dw-celebrate-decor">
        {rings.map((r, i) => (
          <span key={i} className="dw-glow-ring"
            style={{ left: `calc(${r.c} - ${r.s/2}px)`,
                     top:  `calc(40% - ${r.s/2}px)`,
                     width: r.s, height: r.s,
                     animationDelay: r.d + "s" }}/>
        ))}
      </div>
    );
  }
  return null;
};

/* ---------- MAIN OVERLAY ---------- */
const GoalCelebration = ({
  /* override for stories / forced day */
  dayIndex,
  /* numeric facts for the stat strip */
  goalMl = 2200,
  drinkCount = 9,
  streakDays = 5,
  /* handlers */
  onClose,
  onLogBonus,
  onShare,
}) => {
  const idx = dayIndex != null
    ? ((dayIndex % CELEBRATIONS.length) + CELEBRATIONS.length) % CELEBRATIONS.length
    : dayOfYear() % CELEBRATIONS.length;
  const v = CELEBRATIONS[idx];

  return (
    <div className="dw-celebrate-scrim" onClick={onClose}>
      <div className={`dw-celebrate-card ${v.theme}`} onClick={(e) => e.stopPropagation()}>
        <div className="dw-celebrate-hero">
          <Decor kind={v.decor}/>

          <button className="dw-celebrate-close" aria-label="Dismiss" onClick={onClose}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
              <line x1="6" y1="6" x2="18" y2="18"/>
              <line x1="18" y1="6" x2="6" y2="18"/>
            </svg>
          </button>

          <div className="dw-celebrate-mark">
            {v.mark.kind === "img"
              ? <img src={v.mark.src} alt=""/>
              : <span className="glyph">{v.mark.text}</span>}
          </div>

          <div className="dw-celebrate-eyebrow">
            <span className="dot"/>
            {v.eyebrow}
          </div>

          <h2 className="dw-celebrate-headline">{v.headline}</h2>
          <p className="dw-celebrate-sub">{v.sub}</p>
        </div>

        <div className="dw-celebrate-stats">
          <div className="dw-celebrate-stat">
            <div className="dw-celebrate-stat-value">
              {(goalMl / 1000).toFixed(1)}<span className="dw-celebrate-stat-unit">L</span>
            </div>
            <div className="dw-celebrate-stat-label">Reached</div>
          </div>
          <div className="dw-celebrate-stat">
            <div className="dw-celebrate-stat-value">{drinkCount}</div>
            <div className="dw-celebrate-stat-label">Glasses</div>
          </div>
          <div className="dw-celebrate-stat">
            <div className="dw-celebrate-stat-value">
              {streakDays}<span className="dw-celebrate-stat-unit">d</span>
            </div>
            <div className="dw-celebrate-stat-label">Streak</div>
          </div>
        </div>

        <div className="dw-celebrate-actions">
          <Button variant="primary" fullWidth onClick={onLogBonus}>
            {v.cta}
          </Button>
          <button className="dw-celebrate-secondary" onClick={onShare}>
            Share progress
          </button>
        </div>
      </div>
    </div>
  );
};

Object.assign(window, { GoalCelebration, CELEBRATIONS });
