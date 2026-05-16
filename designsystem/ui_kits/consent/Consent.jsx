/* eslint-disable */
/* Drink Water — First-launch consent flow (v3)
   Chrome inspired by the onboarding screenshots:
   - Tall coloured wave header with back arrow + "n of N" + thin progress bar
   - White sheet butts up to the wave (no overlap)
   - Centred hero medallion, centred title, soft-grey inner cards
   - Magenta pill CTA at the bottom, secondary text link above
   Both screens keep distinct per-screen colour (water = privacy, magenta = tracking). */

const { useState: useStateConsent } = React;

/* ---------- shared chrome ------------------------------------ */
const ConsentWave = ({ tone = "water", step, total = 2, onBack }) => (
  <div className={`dw-cv2-wave is-${tone}`}>
    <div className="dw-cv2-bubble b1"/>
    <div className="dw-cv2-bubble b2"/>
    <div className="dw-cv2-bubble b3"/>

    <div className="dw-cv2-wave-bar">
      <button className="dw-cv2-back" onClick={onBack} aria-label="Back">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none"
             stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
          <path d="M19 12H5M11 18l-6-6 6-6"/>
        </svg>
      </button>
      <div className="dw-cv2-count">{step} of {total}</div>
    </div>

    <div className="dw-cv2-progress">
      <div className="dw-cv2-progress-fill" style={{ width: ((step / total) * 100) + "%" }}/>
    </div>
  </div>
);

const ConsentHero = ({ tone, children }) => (
  <div className={`dw-cv2-hero is-${tone}`}>
    {children}
  </div>
);

/* ---------- icons used in the medallions ---------------------- */
const ShieldCheck = () => (
  <svg viewBox="0 0 32 32" width="30" height="30" fill="none"
       stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M16 4 L26 8 V16 C26 22 21.5 26.5 16 28 C10.5 26.5 6 22 6 16 V8 Z" fill="rgba(255,255,255,0.10)"/>
    <polyline points="12 16 15 19 21 13"/>
  </svg>
);
const PulseHeart = () => (
  <svg viewBox="0 0 32 32" width="30" height="30" fill="none"
       stroke="#fff" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M16 27 C 8 21 4 16 7 11 C 10 6 15 7 16 11 C 17 7 22 6 25 11 C 28 16 24 21 16 27 Z" fill="rgba(255,255,255,0.10)"/>
    <path d="M7 16 H11 L13 12 L16 20 L18 14 L20 16 H25"/>
  </svg>
);

/* ============================================================
   1. PRIVACY POLICY
   ============================================================ */
const PrivacyPolicyScreen = ({ onAccept, onOpenWeb, onBack, lastUpdated = "May 12, 2026" }) => {
  const [agreed, setAgreed] = useStateConsent(false);

  return (
    <div className="dw-screen dw-cv2-screen">
      <ConsentWave tone="water" step={1} onBack={onBack}/>

      <div className="dw-cv2-sheet">
        <div className="dw-cv2-medallion is-water">
          <ShieldCheck/>
        </div>

        <h1 className="dw-cv2-title">Privacy Policy</h1>
        <p className="dw-cv2-sub">
          Drink Water is built to help you hydrate — not to collect data on you.
          Take a moment to read what we do and don't keep.
        </p>

        <div className="dw-cv2-updated-row">
          <span>Last updated · {lastUpdated}</span>
          <button className="dw-cv2-weblink" onClick={onOpenWeb}>
            Read on the web
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none"
                 stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M7 17L17 7M9 7h8v8"/>
            </svg>
          </button>
        </div>

        <div className="dw-cv2-policy" tabIndex="0" aria-label="Privacy policy">
          <p className="dw-cv2-policy-lede">
            <b>You're not the product here.</b> Everything Drink Water knows
            about you stays on this device.
          </p>

          <h3>1. What stays on your device</h3>
          <p>
            Your weight, daily goal, reminder schedule and drink log live in
            this app's local storage. Nothing in this list is uploaded.
          </p>

          <h3>2. What we collect from you</h3>
          <ul>
            <li>Weight — to calculate your daily water goal.</li>
            <li>Drink amounts and timestamps that you log.</li>
            <li>Reminder window and interval you set in Settings.</li>
          </ul>

          <h3>3. Notifications</h3>
          <p>
            Reminder notifications are scheduled locally by the OS. They
            contain only what you see in the app and never any personal data.
          </p>

          <h3>4. Anonymous usage data (optional)</h3>
          <p>
            On the next screen you'll choose whether to share anonymous usage
            data. It's opt-in, never linked to a person, and reversible from
            Settings.
          </p>

          <h3>5. What we never do</h3>
          <ul>
            <li>Sell, rent, or trade your data.</li>
            <li>Share data with advertisers or marketing networks.</li>
            <li>Track you across other apps or sites.</li>
          </ul>

          <h3>6. Your rights</h3>
          <p>
            <b>Settings → Data → Export</b> hands you a JSON file of
            everything. <b>Erase</b> wipes it in one tap.
          </p>

          <p className="dw-cv2-policy-trailing">
            Questions? <a href="#contact">privacy@drinkwater.app</a>
          </p>
        </div>

        <label className="dw-cv2-check">
          <button
            role="checkbox"
            aria-checked={agreed}
            onClick={() => setAgreed(!agreed)}
            className={`dw-cv2-checkbox ${agreed ? "is-checked" : ""}`}>
            {agreed && (
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none"
                   stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="5 12 10 17 20 7"/>
              </svg>
            )}
          </button>
          <span>I've read and agree to the Privacy Policy.</span>
        </label>

        <div className="dw-cv2-actions">
          <Button variant="primary" fullWidth disabled={!agreed} onClick={onAccept}>
            Accept and continue
          </Button>
        </div>
      </div>
    </div>
  );
};

/* ============================================================
   2. ANONYMOUS USAGE / TRACKING
   ============================================================ */
const AnalyticsConsentScreen = ({ onDecide, onBack }) => {
  return (
    <div className="dw-screen dw-cv2-screen">
      <ConsentWave tone="magenta" step={2} onBack={onBack}/>

      <div className="dw-cv2-sheet">
        <div className="dw-cv2-medallion is-magenta">
          <PulseHeart/>
        </div>

        <h1 className="dw-cv2-title">Help shape the app</h1>
        <p className="dw-cv2-sub">
          Share anonymous usage so we can see which features people love and
          which need work. Counts only — no name, no email, no device ID.
        </p>

        {/* example payload card — minimal */}
        <div className="dw-cv2-card">
          <div className="dw-cv2-card-row">
            <div className="dw-cv2-card-icon is-water">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none"
                   stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 3v18h18"/><path d="M7 14l4-4 4 4 5-7"/>
              </svg>
            </div>
            <div className="dw-cv2-card-body">
              <div className="dw-cv2-card-title">Aggregate counts</div>
              <div className="dw-cv2-card-sub">Taps, screen views, feature usage.</div>
            </div>
          </div>
          <div className="dw-cv2-card-row">
            <div className="dw-cv2-card-icon is-magenta">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none"
                   stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="11" r="3"/>
                <path d="M4 13a8 8 0 0 1 16 0v8l-3-2-3 2-2-2-2 2-3-2-3 2z"/>
              </svg>
            </div>
            <div className="dw-cv2-card-body">
              <div className="dw-cv2-card-title">Never linked to you</div>
              <div className="dw-cv2-card-sub">Each session is a fresh anonymous token.</div>
            </div>
          </div>
          <div className="dw-cv2-card-row">
            <div className="dw-cv2-card-icon is-water">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none"
                   stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 3l18 18"/>
                <path d="M1 12s4-8 11-8c2 0 3.8.6 5.3 1.6"/>
                <path d="M23 12s-4 8-11 8c-2 0-3.8-.6-5.3-1.6"/>
              </svg>
            </div>
            <div className="dw-cv2-card-body">
              <div className="dw-cv2-card-title">Off any time</div>
              <div className="dw-cv2-card-sub">Settings → Privacy → Usage data.</div>
            </div>
          </div>
        </div>

        <div className="dw-cv2-actions">
          <button className="dw-cv2-textlink" onClick={() => onDecide(false)}>
            Not now
          </button>
          <Button variant="primary" fullWidth onClick={() => onDecide(true)}>
            Share anonymous data
          </Button>
        </div>
      </div>
    </div>
  );
};

/* ============================================================
   ALT (overlap-sheet) — kept from the previous round so we can
   show both directions side by side. Same fundamentals, different
   chrome: the white sheet overlaps the wave by 22px and the glyph
   lives inside the wave instead of in a medallion.
   ============================================================ */
const PrivacyPolicyScreenAlt = ({ onAccept, onOpenWeb, lastUpdated = "May 12, 2026" }) => {
  const [agreed, setAgreed] = useStateConsent(false);
  return (
    <div className="dw-screen dw-cv-alt">
      <div className="dw-cv-alt-wave is-water">
        <div className="dw-cv-alt-bubble b1"/>
        <div className="dw-cv-alt-bubble b2"/>
        <div className="dw-cv-alt-bubble b3"/>

        <div className="dw-cv-alt-wave-mark">
          <ShieldCheck/>
        </div>
        <div className="dw-cv-alt-progress">
          <span className="is-active"/>
          <span/>
        </div>
        <div className="dw-cv-alt-step">Step 1 of 2</div>
        <h1 className="dw-cv-alt-title">Privacy Policy</h1>
        <div className="dw-cv-alt-updated">Last updated · {lastUpdated}</div>
      </div>

      <div className="dw-cv-alt-sheet">
        <div className="dw-cv-alt-policy" tabIndex="0">
          <p className="dw-cv-alt-policy-lede">
            <b>You're not the product here.</b> Drink Water is built to help
            you hydrate — not to collect data on you.
          </p>
          <h3>1. What stays on your device</h3>
          <p>Weight, goal, reminder schedule and drink log live in this app's local storage. Nothing in this list is uploaded.</p>
          <h3>2. What we collect</h3>
          <ul>
            <li>Weight, to calculate your daily water goal.</li>
            <li>Drink amounts and timestamps that you log.</li>
            <li>Reminder window + interval that you set in Settings.</li>
          </ul>
          <h3>3. Notifications</h3>
          <p>Scheduled locally by the OS. They never carry personal data.</p>
          <h3>4. Anonymous usage data (optional)</h3>
          <p>On the next screen you'll choose whether to share anonymous usage data — opt-in, reversible from Settings.</p>
          <h3>5. What we never do</h3>
          <ul>
            <li>Sell, rent, or trade your data.</li>
            <li>Share data with advertisers or marketing networks.</li>
            <li>Track you across other apps or sites.</li>
          </ul>
          <h3>6. Your rights</h3>
          <p><b>Settings → Data → Export</b> hands you a JSON of everything. <b>Erase</b> wipes it in one tap.</p>
          <p className="dw-cv-alt-policy-trailing">Questions? <a href="#contact">privacy@drinkwater.app</a></p>
        </div>

        <button className="dw-cv-alt-weblink" onClick={onOpenWeb}>
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none"
               stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M7 17L17 7M9 7h8v8"/>
          </svg>
          Read the latest version on drinkwater.app
        </button>

        <label className="dw-cv2-check">
          <button
            role="checkbox"
            aria-checked={agreed}
            onClick={() => setAgreed(!agreed)}
            className={`dw-cv2-checkbox ${agreed ? "is-checked" : ""}`}>
            {agreed && (
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none"
                   stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="5 12 10 17 20 7"/>
              </svg>
            )}
          </button>
          <span>I've read and agree to the Privacy Policy.</span>
        </label>

        <Button variant="primary" fullWidth disabled={!agreed} onClick={onAccept}>
          Accept and continue
        </Button>
      </div>
    </div>
  );
};

const AnalyticsConsentScreenAlt = ({ onDecide }) => {
  return (
    <div className="dw-screen dw-cv-alt">
      <div className="dw-cv-alt-wave is-magenta">
        <div className="dw-cv-alt-bubble b1"/>
        <div className="dw-cv-alt-bubble b2"/>
        <div className="dw-cv-alt-bubble b3"/>

        <div className="dw-cv-alt-wave-mark">
          <PulseHeart/>
        </div>
        <div className="dw-cv-alt-progress">
          <span className="is-done"/>
          <span className="is-active"/>
        </div>
        <div className="dw-cv-alt-step">Step 2 of 2</div>
        <h1 className="dw-cv-alt-title">Help shape the app</h1>
      </div>

      <div className="dw-cv-alt-sheet">
        <p className="dw-cv-alt-lede">
          Share anonymous usage so we can see which features people love.
          <b> No name, no email, no device ID</b> — just counts.
        </p>

        <div className="dw-cv-alt-preview">
          <div className="dw-cv-alt-preview-eyebrow">What we send</div>
          <div className="dw-cv-alt-preview-rows">
            <div><span className="pill">event</span><code>add_drink_tapped</code></div>
            <div><span className="pill">event</span><code>goal_reached</code></div>
            <div><span className="pill">screen</span><code>history</code></div>
          </div>
          <div className="dw-cv-alt-preview-foot">
            Aggregated · anonymous · reversible in Settings.
          </div>
        </div>

        <Button variant="primary" fullWidth onClick={() => onDecide(true)}>
          Share anonymous data
        </Button>
        <button className="dw-cv2-textlink" onClick={() => onDecide(false)}>
          Not now
        </button>
      </div>
    </div>
  );
};

Object.assign(window, {
  PrivacyPolicyScreen, AnalyticsConsentScreen,
  PrivacyPolicyScreenAlt, AnalyticsConsentScreenAlt,
});
