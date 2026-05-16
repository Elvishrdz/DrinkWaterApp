/* eslint-disable */
/* Drink Water — Minimalist Today (Reminder-first variant)
   - No greeting, no user name, no streak
   - Starts at 0 mL (start of day)
   - ReminderTimeline container sits between QuickAdd and TodayLog
   - Rail labels condensed to just endpoints + the active slot (font-size resilient)
   - Header is a slot that can carry the date OR an alternative metric
*/

const { useState: useStateReminder, useEffect: useEffectReminder, useMemo: useMemoReminder } = React;

/* ---------- helpers ---------- */
const fmtClock12 = (h, m = 0) => {
  const ap = h >= 12 ? "p.m." : "a.m.";
  const hh = ((h + 11) % 12) + 1;
  return `${hh}:${String(m).padStart(2, "0")} ${ap}`;
};
const fmtClock12Short = (h, m = 0) => {
  const ap = h >= 12 ? "p" : "a";
  const hh = ((h + 11) % 12) + 1;
  return m === 0 ? `${hh}${ap}` : `${hh}:${String(m).padStart(2, "0")}${ap}`;
};
const minutesSinceMidnight = (d) => d.getHours() * 60 + d.getMinutes();

function buildSchedule(startH, endH, intervalMin) {
  const slots = [];
  const startM = startH * 60;
  const endM = endH * 60;
  for (let m = startM; m <= endM; m += intervalMin) {
    const h = Math.floor(m / 60);
    const mm = m % 60;
    slots.push({ h, m: mm, totalMin: m, label: fmtClock12(h, mm), short: fmtClock12Short(h, mm) });
  }
  return slots;
}

/* ---------- TIME-TO-NEXT CHIP ---------- */
const NextReminderChip = ({ nextSlot, nowMin, beforeWindow, afterWindow, firstSlot }) => {
  if (afterWindow) {
    return (
      <div className="dw-reminder-next dw-reminder-next--quiet">
        <span className="dot"/>
        <div className="dw-reminder-next-meta">
          <div className="dw-reminder-next-label">Quiet hours</div>
          <div className="dw-reminder-next-time">Reminders resume at {firstSlot.label}</div>
        </div>
      </div>
    );
  }
  if (beforeWindow && nextSlot) {
    const delta = Math.max(0, nextSlot.totalMin - nowMin);
    const h = Math.floor(delta / 60);
    const m = delta % 60;
    const inLabel = h > 0 ? `in ${h}h ${m}m` : `in ${m} min`;
    return (
      <div className="dw-reminder-next dw-reminder-next--upcoming">
        <span className="dot"/>
        <div className="dw-reminder-next-meta">
          <div className="dw-reminder-next-label">First reminder</div>
          <div className="dw-reminder-next-time">{nextSlot.label} · {inLabel}</div>
        </div>
      </div>
    );
  }
  if (!nextSlot) {
    return (
      <div className="dw-reminder-next dw-reminder-next--quiet">
        <span className="dot"/>
        <div className="dw-reminder-next-meta">
          <div className="dw-reminder-next-label">Done for today</div>
          <div className="dw-reminder-next-time">No more reminders scheduled</div>
        </div>
      </div>
    );
  }
  const delta = Math.max(0, nextSlot.totalMin - nowMin);
  const h = Math.floor(delta / 60);
  const m = delta % 60;
  const inLabel = h > 0 ? `in ${h}h ${m}m` : `in ${m} min`;
  return (
    <div className="dw-reminder-next">
      <span className="dot"/>
      <div className="dw-reminder-next-meta">
        <div className="dw-reminder-next-label">Next reminder</div>
        <div className="dw-reminder-next-time">{nextSlot.label} · {inLabel}</div>
      </div>
    </div>
  );
};

/* ---------- REMINDER TIMELINE ----------
   Cleaner rail: dots only, no per-slot text underneath.
   Only the *active* (next) slot lifts a small floating callout above it
   showing its time and the running total — every other slot is just a dot.
   Endpoints (window start + end) are labeled below the rail.
*/
const ReminderTimeline = ({
  schedule, intervalMin, drinks, nowMin, onEditSchedule,
}) => {
  const slotData = useMemoReminder(() => {
    return schedule.map((s, i) => {
      const total = drinks.filter(d => d.minute <= s.totalMin).reduce((a, d) => a + d.amount, 0);
      const passed = nowMin >= s.totalMin;
      const isNext = !passed && (i === 0 || nowMin >= schedule[i - 1].totalMin);
      return { ...s, total, passed, isNext };
    });
  }, [schedule, drinks, nowMin]);

  const first = schedule[0]?.totalMin ?? 0;
  const last = schedule[schedule.length - 1]?.totalMin ?? 1;
  const range = Math.max(1, last - first);

  const beforeWindow = nowMin < first;
  const afterWindow = nowMin > last;
  const nowPct = afterWindow ? 100
                : beforeWindow ? 0
                : Math.max(0, Math.min(100, ((nowMin - first) / range) * 100));
  const nextSlot = slotData.find(s => !s.passed) || null;

  const totalSoFar = drinks.filter(d => d.minute <= nowMin).reduce((a, d) => a + d.amount, 0);

  return (
    <section className={`dw-reminder-card ${afterWindow ? "is-quiet" : ""}`}>
      <header className="dw-reminder-head">
        <div>
          <div className="dw-reminder-eyebrow">Reminders</div>
          <h3 className="dw-reminder-title">Every {intervalMin} min</h3>
        </div>
        <button className="dw-reminder-edit" onClick={onEditSchedule} aria-label="Edit schedule">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M11 4H4a2 2 0 0 0 -2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2 -2v-7"/>
            <path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          <span>Edit</span>
        </button>
      </header>

      <NextReminderChip
        nextSlot={nextSlot}
        nowMin={nowMin}
        beforeWindow={beforeWindow}
        afterWindow={afterWindow}
        firstSlot={schedule[0]}
      />

      <div className="dw-rail-wrap">
        {/* track + filled portion */}
        <div className="dw-rail-track">
          <div className="dw-rail-fill" style={{ width: nowPct + "%" }}/>
        </div>

        {/* dots */}
        <div className="dw-rail-dots">
          {slotData.map((s, i) => {
            const pct = ((s.totalMin - first) / range) * 100;
            return (
              <div key={i}
                   className={`dw-rail-dot ${s.passed ? "is-passed" : ""} ${s.isNext ? "is-next" : ""}`}
                   style={{ left: pct + "%" }}
                   title={`${s.label} · ${s.total} mL`}/>
            );
          })}

          {/* now pin — hidden in quiet mode */}
          {!afterWindow && (
            <div className="dw-rail-now" style={{ left: nowPct + "%" }}>
              <div className="dw-rail-now-pin"/>
              <div className="dw-rail-now-label">
                <span className="dw-rail-now-time">{fmtClock12(Math.floor(nowMin / 60), nowMin % 60)}</span>
                <span className="dw-rail-now-total">{totalSoFar} mL</span>
              </div>
            </div>
          )}
        </div>

        {/* endpoint anchors — only the rail's boundary times */}
        <div className="dw-rail-anchors">
          <span>{schedule[0]?.label}</span>
          <span>{schedule[schedule.length - 1]?.label}</span>
        </div>
      </div>
    </section>
  );
};

/* ---------- HEADER VARIANTS ----------
   - "date"   : current day (default)
   - "clock"  : current time + relative day, useful when reminders matter more than date
*/
const MinimalHeader = ({ variant = "date", date, time, goal }) => {
  const showClock = variant === "clock";
  return (
    <header className="dw-min-head">
      <div>
        <div className="dw-min-eyebrow">
          {showClock ? "Right now" : "Today"}
        </div>
        <h1 className="dw-min-date">
          {showClock ? time : date}
        </h1>
        {showClock && <div className="dw-min-subtle">{date}</div>}
      </div>
      <div className="dw-min-goalpill">
        Goal · {(goal / 1000).toFixed(1)}L
      </div>
    </header>
  );
};

/* ---------- MINIMAL TODAY SCREEN ----------
   Order: header → hero ring → quick add → reminder timeline → log
*/
const TodayMinimalScreen = ({
  current = 0, goal = 2200, log = [],
  schedule, intervalMin, nowMin,
  headerVariant = "date", headerDate = "Tue, May 12", headerTime,
  onAdd, onCustom, onSeeAll, onEditSchedule,
}) => {
  const pct = Math.min(100, (current / goal) * 100);
  const remaining = Math.max(0, goal - current);

  const drinks = useMemoReminder(() => {
    return log.map(item => {
      const m = item.time.match(/(\d+):(\d+)\s?(a|p)/i);
      if (!m) return { minute: 0, amount: item.amount };
      let h = parseInt(m[1], 10);
      const mm = parseInt(m[2], 10);
      const isPm = /p/i.test(m[3]);
      if (isPm && h !== 12) h += 12;
      if (!isPm && h === 12) h = 0;
      return { minute: h * 60 + mm, amount: item.amount };
    });
  }, [log]);

  const computedTime = headerTime || fmtClock12(Math.floor(nowMin / 60), nowMin % 60);

  return (
    <div className="dw-screen dw-screen-today dw-screen-today--minimal">
      <MinimalHeader variant={headerVariant} date={headerDate} time={computedTime} goal={goal}/>

      <div className="dw-today-hero">
        <ProgressRing percent={pct} current={current} goal={goal}/>
        <div className="dw-today-remaining">
          {current === 0
            ? <>Day's just starting — <b>tap a glass</b> to log your first sip</>
            : remaining > 0
              ? <>You're <b>{remaining.toLocaleString()} mL</b> away from today's goal</>
              : <><b>Goal reached</b> — keep sipping to stay ahead</>}
        </div>
      </div>

      <QuickAddRow onAdd={onAdd} onCustom={onCustom}/>

      <ReminderTimeline
        schedule={schedule}
        intervalMin={intervalMin}
        drinks={drinks}
        nowMin={nowMin}
        onEditSchedule={onEditSchedule}
      />

      <TodayLog items={log} onSeeAll={onSeeAll}/>
    </div>
  );
};

Object.assign(window, {
  TodayMinimalScreen, ReminderTimeline, NextReminderChip, MinimalHeader,
  buildSchedule, fmtClock12, minutesSinceMidnight,
});
