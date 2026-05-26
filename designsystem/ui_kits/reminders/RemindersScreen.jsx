/* eslint-disable */
/* Drink Water — RemindersScreen
   Per-day profiles + scope-based editing. */

const { useState: rsState, useEffect: rsEffect, useMemo: rsMemo, useRef: rsRef } = React;

const DAY_FULL  = ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"];
const DAY_SHORT = ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"];

const RemindersScreen = ({
  goalMl, now, today,
  notif, setNotif,
  activeDays, setActiveDays,
  /* scope/profile */
  scope, setScope, profiles,
  onCreateOverride, onRemoveOverride, todayProfile,
  /* current scope's profile (mirrored as flat props for ergonomics) */
  mode, setMode,
  autoMode, setAutoMode,
  start, setStart,
  end, setEnd,
  interval, setInterval_,
  amount, setAmount,
  schedule, setSchedule,
}) => {
  const [openRow, setOpenRow] = rsState(null);
  const [pendingSwitch, setPendingSwitch] = rsState(false);
  const [view, setView] = rsState("timeline");
  const [selectedTL, setSelectedTL] = rsState(null);
  const [collisionMode, setCollisionMode] = rsState("free");
  const [scopePickerOpen, setScopePickerOpen] = rsState(false);

  /* clear selection when scope changes (we're editing different data) */
  rsEffect(() => { setOpenRow(null); setSelectedTL(null); setScopePickerOpen(false); }, [scope]);

  /* regenerate schedule when in auto mode + auto inputs change */
  rsEffect(() => {
    if (mode !== "auto") return;
    const next = autoMode === "interval"
      ? genByInterval(start, end, interval, goalMl)
      : genByAmount(start, end, amount, goalMl);
    setSchedule(next);
    setOpenRow(null); setSelectedTL(null);
  }, [mode, autoMode, start, end, interval, amount, goalMl]);

  const sorted = rsMemo(() => [...schedule].sort((a, b) => a.time - b.time), [schedule]);
  const totalMl = sorted.reduce((s, r) => s + r.amount, 0);
  const todayActive = activeDays[today];

  /* TODAY-effective profile (independent of editing scope) for the hero card */
  const todaySorted = rsMemo(() => [...(todayProfile.schedule || [])].sort((a, b) => a.time - b.time), [todayProfile]);
  const todayNextIdx = rsMemo(() => {
    const i = todaySorted.findIndex(r => r.time >= now);
    return i === -1 ? null : i;
  }, [todaySorted, now]);
  const todayNext = todayNextIdx == null ? null : todaySorted[todayNextIdx];

  /* Schedule list shows the CURRENT scope (what user is editing) */
  const listNextIdx = rsMemo(() => {
    const i = sorted.findIndex(r => r.time >= now);
    return i === -1 ? null : i;
  }, [sorted, now]);

  const errs = validate({ mode, autoMode, start, end, interval, amount, goalMl, schedule: sorted, activeDays });

  const stepStart = (d) => setStart(Math.max(0, Math.min(end - 60, start + d)));
  const stepEnd = (d) => setEnd(Math.max(start + 60, Math.min(1435, end + d)));
  const stepInterval = (d) => setInterval_(Math.max(5, Math.min(240, interval + d)));
  const stepAmount = (d) => setAmount(Math.max(50, Math.min(goalMl, amount + d)));

  const onModeSwitch = (m) => {
    if (m === mode) return;
    if (m === "auto" && mode === "manual") { setPendingSwitch(true); return; }
    setMode(m);
  };
  const confirmSwitchToAuto = () => { setMode("auto"); setPendingSwitch(false); };

  const updateRow = (id, patch) => setSchedule(schedule.map(r => r.id === id ? { ...r, ...patch } : r));
  const deleteRow = (id) => { setSchedule(schedule.filter(r => r.id !== id)); setOpenRow(null); setSelectedTL(null); };
  const addRow = () => {
    const latest = sorted.length ? sorted[sorted.length - 1].time : start;
    const t = Math.min(1410, latest + 30);
    const id = `m-${Date.now()}`;
    setSchedule([...schedule, { id, time: t, amount: 200 }]);
    setOpenRow(id);
    return id;
  };

  /* scope metadata */
  const scopeIsAll = scope === "all";
  const editingDayIdx = scopeIsAll ? null : Number(scope);
  const overrideDays = Object.keys(profiles).filter(k => k !== "all").map(Number).sort((a, b) => a - b);
  const todayHasOverride = overrideDays.includes(today);
  const scopeLabelLong = scopeIsAll
    ? "Same schedule for every active day"
    : `Custom schedule for ${DAY_FULL[editingDayIdx]}`;
  const editingTitle = scopeIsAll
    ? "Default schedule"
    : `${DAY_FULL[editingDayIdx]} only`;
  const listTitle = scopeIsAll
    ? "Today's schedule"
    : `${DAY_SHORT[editingDayIdx]} schedule`;

  /* the currently-selected timeline block (for editor card below timeline) */
  const selectedBlock = sorted.find(r => r.id === selectedTL);

  return (
    <div className="dw-screen dw-screen-reminders">
      <div className="rem-head">
        <div>
          <h1>Reminders</h1>
          <div className="rem-head-sub">Hydration nudges</div>
        </div>
        <div className="rem-head-actions">
          <span className="rem-now-chip">{DAY_SHORT[today]} · {fmtTime12(now)}</span>
          <RemSwitch on={notif} onChange={setNotif}/>
        </div>
      </div>

      <div className="rem-body">
        {/* HERO — always reflects TODAY */}
        <NextHero
          on={notif && todayActive}
          next={todayNext}
          now={now}
          nextIdx={todayNextIdx}
          total={todaySorted.length}
          todayActive={todayActive}
          todayName={DAY_FULL[today]}
          isCustom={todayHasOverride}
        />

        {/* MODE CARD with scope picker on top */}
        <div className="rem-card">
          <ScopePicker
            scope={scope} setScope={setScope}
            profiles={profiles}
            activeDays={activeDays}
            today={today}
            open={scopePickerOpen} setOpen={setScopePickerOpen}
            onCreateOverride={onCreateOverride}
            onRemoveOverride={onRemoveOverride}
            editingTitle={editingTitle}
            scopeLabelLong={scopeLabelLong}
          />

          <div className="rem-seg">
            <button className={`rem-seg-btn ${mode === "auto" ? "is-active" : ""}`} onClick={() => onModeSwitch("auto")}>Auto</button>
            <button className={`rem-seg-btn ${mode === "manual" ? "is-active" : ""}`} onClick={() => onModeSwitch("manual")}>Manual</button>
          </div>

          {pendingSwitch && (
            <div className="rem-banner is-warn">
              <span className="rem-banner-icon">!</span>
              <span>Switching to Auto will <b>regenerate</b> {scopeIsAll ? "the default schedule" : `${DAY_SHORT[editingDayIdx]}'s schedule`} and lose your manual edits.</span>
              <button className="rem-banner-action" onClick={() => setPendingSwitch(false)}>Cancel</button>
              <button className="rem-banner-action" style={{color:"var(--brand-magenta)"}} onClick={confirmSwitchToAuto}>Replace</button>
            </div>
          )}

          {mode === "auto" && (
            <>
              <div className="rem-seg is-nested">
                <button className={`rem-seg-btn ${autoMode === "interval" ? "is-active" : ""}`} onClick={() => setAutoMode("interval")}>By interval</button>
                <button className={`rem-seg-btn ${autoMode === "amount" ? "is-active" : ""}`} onClick={() => setAutoMode("amount")}>By amount</button>
              </div>

              <div className="rem-fields">
                <TimeField label="Start" min={start} onDec={() => stepStart(-15)} onInc={() => stepStart(15)}/>
                <TimeField label="End"   min={end}   onDec={() => stepEnd(-15)}   onInc={() => stepEnd(15)}/>
              </div>

              {autoMode === "interval" ? (
                <Stepper label="Every" value={interval} unit="min"
                  onDec={() => stepInterval(-5)} onInc={() => stepInterval(5)}
                  fillPct={(interval - 5) / (180 - 5) * 100}
                  disabled={{ dec: interval <= 5, inc: interval >= 240 }}/>
              ) : (
                <Stepper label="Per drink" value={amount} unit="mL"
                  onDec={() => stepAmount(-10)} onInc={() => stepAmount(10)}
                  fillPct={(amount - 50) / (goalMl - 50) * 100}
                  disabled={{ dec: amount <= 50, inc: amount >= goalMl }}/>
              )}

              <div className="rem-livestat">
                <div className="rem-livestat-item"><span className="lbl">Reminders</span><b>{sorted.length}</b></div>
                <div className="rem-livestat-divider"/>
                <div className="rem-livestat-item"><span className="lbl">Each</span><b>{sorted[0]?.amount ?? 0} mL</b></div>
                <div className="rem-livestat-divider"/>
                <div className="rem-livestat-item"><span className="lbl">Total</span><b>{totalMl} mL</b></div>
                <div className="rem-livestat-divider"/>
                <div className="rem-livestat-item"><span className="lbl">Goal</span><b>{goalMl} mL</b></div>
              </div>
            </>
          )}

          {mode === "manual" && (
            <div className="rem-banner is-info">
              <span className="rem-banner-icon">i</span>
              <span>Tap any time to edit, delete, or move it. Total covers <b>{totalMl} mL</b> of <b>{goalMl} mL</b>.</span>
            </div>
          )}
        </div>

        {/* VALIDATION */}
        {errs.map((e, i) => (
          <div key={i} className={`rem-banner ${e.level === "error" ? "is-error" : "is-warn"}`}>
            <span className="rem-banner-icon">{e.level === "error" ? "!" : "⚠"}</span>
            <span>{e.msg}</span>
          </div>
        ))}

        {/* ACTIVE DAYS */}
        <div className="rem-card">
          <div className="rem-section-title">
            <span>Active days</span>
            <small>{activeDays.filter(Boolean).length}/7 active · {overrideDays.length} custom</small>
          </div>
          <div className="rem-days">
            {["M","T","W","T","F","S","S"].map((d, i) => {
              const isCustom = overrideDays.includes(i);
              const isScoped = !scopeIsAll && editingDayIdx === i;
              return (
                <button key={i}
                  className={`rem-day ${activeDays[i] ? "is-on" : ""} ${i === today ? "is-today" : ""} ${isCustom ? "is-custom" : ""} ${isScoped ? "is-scoped" : ""}`}
                  onClick={() => {
                    const next = activeDays.slice();
                    next[i] = !next[i];
                    setActiveDays(next);
                  }}>{d}</button>
              );
            })}
          </div>
          <div className="rem-days-legend">
            <span className="leg leg-today">●</span> today
            <span className="leg leg-custom">●</span> has custom schedule
          </div>
        </div>

        {/* SCHEDULE — list OR timeline */}
        <div>
          <div className="rem-sch-head">
            <div className="rem-sch-title">
              <span className="title">{listTitle}</span>
              <small>· {sorted.length} reminder{sorted.length === 1 ? "" : "s"}</small>
            </div>
            <div className="rem-view-tog" role="tablist" aria-label="View">
              <button className={view === "list" ? "is-active" : ""} onClick={() => setView("list")} title="List view" aria-label="List view">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round">
                  <line x1="4" y1="6"  x2="20" y2="6"/>
                  <line x1="4" y1="12" x2="20" y2="12"/>
                  <line x1="4" y1="18" x2="20" y2="18"/>
                </svg>
              </button>
              <button className={view === "timeline" ? "is-active" : ""} onClick={() => setView("timeline")} title="Timeline view" aria-label="Timeline view">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="6" y1="3" x2="6" y2="21"/>
                  <circle cx="6" cy="7"  r="2" fill="currentColor" stroke="none"/>
                  <circle cx="6" cy="13" r="2" fill="currentColor" stroke="none"/>
                  <circle cx="6" cy="19" r="2" fill="currentColor" stroke="none"/>
                  <line x1="10" y1="7"  x2="19" y2="7"/>
                  <line x1="10" y1="13" x2="16" y2="13"/>
                  <line x1="10" y1="19" x2="20" y2="19"/>
                </svg>
              </button>
              <button className={view === "blocks" ? "is-active" : ""} onClick={() => setView("blocks")} title="Blocks view" aria-label="Blocks view">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinejoin="round">
                  <rect x="3"  y="7" width="5" height="10" rx="1.2"/>
                  <rect x="9.5" y="7" width="5" height="10" rx="1.2"/>
                  <rect x="16" y="7" width="5" height="10" rx="1.2"/>
                </svg>
              </button>
            </div>
          </div>

          {view === "timeline" && sorted.length > 0 && (
            <>
              <TimelineView
                schedule={schedule} setSchedule={setSchedule}
                mode={mode} now={now}
                viewStart={Math.max(0, Math.min(start, sorted[0]?.time ?? start) - 30)}
                viewEnd={Math.min(1439, Math.max(end, sorted[sorted.length-1]?.time ?? end) + 30)}
                selectedId={selectedTL} setSelectedId={setSelectedTL}
                collisionMode={collisionMode} setCollisionMode={setCollisionMode}
                nextIdx={listNextIdx}
              />
              {/* Editor card below the timeline */}
              {selectedBlock && mode === "manual" && (
                <div className="rem-tl-editor" style={{marginTop:10}}>
                  <div className="rem-tl-editor-head">
                    <div>
                      <div className="rem-tl-editor-eyebrow">EDITING</div>
                      <div className="rem-tl-editor-title">{fmtTime12(selectedBlock.time)}</div>
                    </div>
                    <button className="rem-tl-editor-close" onClick={() => setSelectedTL(null)} aria-label="close">×</button>
                  </div>
                  <div className="rem-edit-row">
                    <span className="lbl">Time</span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { time: Math.max(0, selectedBlock.time - 15) })}>−</button>
                    <span className="val">{fmtTime12(selectedBlock.time)}</span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { time: Math.min(1439, selectedBlock.time + 15) })}>+</button>
                  </div>
                  <div className="rem-edit-row">
                    <span className="lbl">Amount</span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { amount: Math.max(10, selectedBlock.amount - 10) })}>−</button>
                    <span className="val">{selectedBlock.amount}<small>mL</small></span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { amount: selectedBlock.amount + 10 })}>+</button>
                  </div>
                  <div className="rem-edit-actions">
                    <button className="rem-edit-delete" onClick={() => deleteRow(selectedBlock.id)}>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                        <path d="M10 11v6M14 11v6"/>
                      </svg>
                      Delete
                    </button>
                    <button className="rem-edit-done" onClick={() => setSelectedTL(null)}>Done</button>
                  </div>
                </div>
              )}
              {selectedBlock && mode === "auto" && (
                <div className="rem-tl-editor rem-tl-editor-hint" style={{marginTop:10}}>
                  <div className="rem-tl-editor-head">
                    <div>
                      <div className="rem-tl-editor-eyebrow">{fmtTime12(selectedBlock.time)} · {selectedBlock.amount} mL</div>
                      <div className="rem-tl-editor-title">Auto-generated reminder</div>
                    </div>
                    <button className="rem-tl-editor-close" onClick={() => setSelectedTL(null)} aria-label="close">×</button>
                  </div>
                  <p className="rem-tl-editor-body">Individual reminders can't be moved in Auto mode. Switch to <b>Manual</b> to drag, edit or delete this one — your current schedule will be kept as a starting point.</p>
                  <div className="rem-edit-actions">
                    <span/>
                    <button className="rem-edit-done" onClick={() => { setMode("manual"); }}>Switch to Manual</button>
                  </div>
                </div>
              )}
            </>
          )}

          {view === "blocks" && sorted.length > 0 && (
            <>
              <BlocksView
                schedule={schedule} setSchedule={setSchedule}
                mode={mode} now={now}
                selectedId={selectedTL} setSelectedId={setSelectedTL}
                nextIdx={listNextIdx}
                onDelete={deleteRow}
              />
              {selectedBlock && mode === "manual" && (
                <div className="rem-tl-editor" style={{marginTop:10}}>
                  <div className="rem-tl-editor-head">
                    <div>
                      <div className="rem-tl-editor-eyebrow">EDITING</div>
                      <div className="rem-tl-editor-title">{fmtTime12(selectedBlock.time)}</div>
                    </div>
                    <button className="rem-tl-editor-close" onClick={() => setSelectedTL(null)} aria-label="close">×</button>
                  </div>
                  <div className="rem-edit-row">
                    <span className="lbl">Time</span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { time: Math.max(0, selectedBlock.time - 15) })}>−</button>
                    <span className="val">{fmtTime12(selectedBlock.time)}</span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { time: Math.min(1439, selectedBlock.time + 15) })}>+</button>
                  </div>
                  <div className="rem-edit-row">
                    <span className="lbl">Amount</span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { amount: Math.max(10, selectedBlock.amount - 10) })}>−</button>
                    <span className="val">{selectedBlock.amount}<small>mL</small></span>
                    <button className="rem-step-btn" onClick={() => updateRow(selectedBlock.id, { amount: selectedBlock.amount + 10 })}>+</button>
                  </div>
                  <div className="rem-edit-actions">
                    <button className="rem-edit-delete" onClick={() => deleteRow(selectedBlock.id)}>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                        <path d="M10 11v6M14 11v6"/>
                      </svg>
                      Delete
                    </button>
                    <button className="rem-edit-done" onClick={() => setSelectedTL(null)}>Done</button>
                  </div>
                </div>
              )}
              {selectedBlock && mode === "auto" && (
                <div className="rem-tl-editor rem-tl-editor-hint" style={{marginTop:10}}>
                  <div className="rem-tl-editor-head">
                    <div>
                      <div className="rem-tl-editor-eyebrow">{fmtTime12(selectedBlock.time)} · {selectedBlock.amount} mL</div>
                      <div className="rem-tl-editor-title">Auto-generated reminder</div>
                    </div>
                    <button className="rem-tl-editor-close" onClick={() => setSelectedTL(null)} aria-label="close">×</button>
                  </div>
                  <p className="rem-tl-editor-body">Individual reminders can't be moved or reordered in Auto mode. Switch to <b>Manual</b> to drag, edit or delete this one — your current schedule will be kept as a starting point.</p>
                  <div className="rem-edit-actions">
                    <span/>
                    <button className="rem-edit-done" onClick={() => { setMode("manual"); }}>Switch to Manual</button>
                  </div>
                </div>
              )}
            </>
          )}

          {view === "list" && sorted.length > 0 && (
            <div className="rem-list">
              {sorted.map((r, i) => {
                const isPast = r.time < now;
                const isNext = i === listNextIdx;
                const isOpen = openRow === r.id;
                return (
                  <div key={r.id} className={`rem-row ${isPast ? "is-past" : ""} ${isNext ? "is-next" : ""} ${isOpen ? "is-open" : ""}`}>
                    <div className="rem-row-head" onClick={() => mode === "manual" && setOpenRow(isOpen ? null : r.id)}>
                      <span className="rem-bullet"/>
                      <span className="rem-row-time">{fmtTime12(r.time)}</span>
                      <span className="rem-row-amount"><b>{r.amount}</b> mL</span>
                      {isNext && <span className="rem-row-badge">Next</span>}
                      {isPast && !isNext && <span className="rem-row-check">✓</span>}
                      {mode === "manual" && (
                        <svg className="rem-row-caret" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="6 9 12 15 18 9"/>
                        </svg>
                      )}
                    </div>
                    {mode === "manual" && isOpen && (
                      <div className="rem-edit">
                        <div className="rem-edit-row">
                          <span className="lbl">Time</span>
                          <button className="rem-step-btn" onClick={() => updateRow(r.id, { time: Math.max(0, r.time - 15) })}>−</button>
                          <span className="val">{fmtTime12(r.time)}</span>
                          <button className="rem-step-btn" onClick={() => updateRow(r.id, { time: Math.min(1439, r.time + 15) })}>+</button>
                        </div>
                        <div className="rem-edit-row">
                          <span className="lbl">Amount</span>
                          <button className="rem-step-btn" onClick={() => updateRow(r.id, { amount: Math.max(10, r.amount - 10) })}>−</button>
                          <span className="val">{r.amount}<small>mL</small></span>
                          <button className="rem-step-btn" onClick={() => updateRow(r.id, { amount: r.amount + 10 })}>+</button>
                        </div>
                        <div className="rem-edit-actions">
                          <button className="rem-edit-delete" onClick={() => deleteRow(r.id)}>
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="3 6 5 6 21 6"/>
                              <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                              <path d="M10 11v6M14 11v6"/>
                            </svg>
                            Delete
                          </button>
                          <button className="rem-edit-done" onClick={() => setOpenRow(null)}>Done</button>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}

          {mode === "manual" && (
            <button className="rem-add" style={{marginTop: 10, width:"100%"}}
              onClick={() => { const id = addRow(); if (view === "timeline") setSelectedTL(id); }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              Add reminder
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

/* ============================================================
   SCOPE PICKER — switch what days the current edits apply to
   ============================================================ */
const ScopePicker = ({ scope, setScope, profiles, activeDays, today, open, setOpen, onCreateOverride, onRemoveOverride, editingTitle, scopeLabelLong }) => {
  const scopeIsAll = scope === "all";
  const editingDayIdx = scopeIsAll ? null : Number(scope);
  const overrideDays = Object.keys(profiles).filter(k => k !== "all").map(Number).sort((a, b) => a - b);

  return (
    <div className={`rem-scope ${open ? "is-open" : ""}`}>
      <button className="rem-scope-pill" onClick={() => setOpen(!open)} aria-expanded={open}>
        <span className={`rem-scope-dot ${scopeIsAll ? "is-all" : "is-day"}`}/>
        <span className="rem-scope-pill-label">
          <span className="lbl-1">Editing</span>
          <span className="lbl-2">{editingTitle}</span>
        </span>
        <svg className="rem-scope-caret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </button>
      {!open && (
        <div className="rem-scope-hint">{scopeLabelLong}</div>
      )}

      {open && (
        <div className="rem-scope-panel">
          {/* All days option */}
          <button className={`rem-scope-row ${scopeIsAll ? "is-active" : ""}`} onClick={() => { setScope("all"); setOpen(false); }}>
            <span className="rem-scope-radio"/>
            <span className="rem-scope-name">All days</span>
            <span className="rem-scope-meta">default</span>
          </button>

          {/* Per-day rows */}
          {[0,1,2,3,4,5,6].map(i => {
            const k = String(i);
            const isOverride = !!profiles[k];
            const isActive = !scopeIsAll && editingDayIdx === i;
            const dayActive = activeDays[i];
            return (
              <div key={i} className={`rem-scope-row ${isActive ? "is-active" : ""} ${!dayActive ? "is-disabled" : ""}`}>
                <button className="rem-scope-row-main" onClick={() => {
                    if (isOverride) { setScope(k); setOpen(false); }
                    else { onCreateOverride(i); setOpen(false); }
                  }}>
                  <span className={`rem-scope-radio ${isOverride ? "is-custom" : ""}`}/>
                  <span className="rem-scope-name">
                    {DAY_FULL[i]}
                    {i === today && <span className="rem-scope-todaytag">today</span>}
                  </span>
                  <span className="rem-scope-meta">
                    {isOverride ? "custom" : "uses default"}
                  </span>
                </button>
                {isOverride && (
                  <button className="rem-scope-rm" title="Remove custom schedule"
                    onClick={(e) => { e.stopPropagation(); onRemoveOverride(i); }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
                    </svg>
                  </button>
                )}
              </div>
            );
          })}

          <div className="rem-scope-foot">
            Pick a day to give it its own start, end, interval and schedule. Other days keep the default.
          </div>
        </div>
      )}
    </div>
  );
};

/* ============================================================
   TIME FIELD (start / end)
   ============================================================ */
const TimeField = ({ label, min, onDec, onInc }) => {
  const p = fmtTimeParts(min);
  return (
    <div className="rem-field">
      <div style={{display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:4}}>
        <span className="rem-field-label">{label}</span>
        <div style={{display:"flex", gap:4}}>
          <button className="rem-step-btn" style={{width:24, height:24, fontSize:14}} onClick={onDec}>−</button>
          <button className="rem-step-btn" style={{width:24, height:24, fontSize:14}} onClick={onInc}>+</button>
        </div>
      </div>
      <div className="rem-field-value">{p.hm}<small>{p.p}</small></div>
    </div>
  );
};

/* ============================================================
   NEXT REMINDER HERO (always reflects TODAY)
   ============================================================ */
const NextHero = ({ on, next, now, nextIdx, total, todayActive, todayName, isCustom }) => {
  const badge = isCustom && (
    <span className="rem-next-tag">CUSTOM · {todayName}</span>
  );
  if (!on) {
    return (
      <div className="rem-next is-off">
        <div className="rem-next-top">
          <span className="rem-next-label">Reminders paused</span>
          {badge}
        </div>
        <div className="rem-next-time">All notifications off</div>
        <div className="rem-next-meta" style={{marginTop:6}}>
          {todayActive ? "Toggle the switch above to resume." : `${todayName} is disabled in active days.`}
        </div>
      </div>
    );
  }
  if (!next) {
    return (
      <div className="rem-next">
        <div className="rem-next-top">
          <span className="rem-next-label">Next reminder</span>
          {badge || <span className="rem-next-countdown">done for today</span>}
        </div>
        <div className="rem-next-time">All set 🎉</div>
        <div className="rem-next-meta" style={{marginTop:8}}>
          <span>You've cleared all of today's {total} reminders.</span>
        </div>
      </div>
    );
  }
  const dur = next.time - now;
  return (
    <div className="rem-next">
      <div className="rem-next-top">
        <span className="rem-next-label">Next reminder</span>
        <div style={{display:"flex", gap:6, alignItems:"center"}}>
          {badge}
          <span className="rem-next-countdown">{fmtDur(dur)}</span>
        </div>
      </div>
      <div className="rem-next-time">{fmtTime12(next.time)} <small>· {next.amount} mL</small></div>
      <div className="rem-next-meta">
        <span><b>{nextIdx + 1}</b> of {total} today</span>
        <span className="rem-next-divider"/>
        <span>{nextIdx} completed</span>
      </div>
    </div>
  );
};

Object.assign(window, { RemindersScreen, TimeField, NextHero, ScopePicker });
