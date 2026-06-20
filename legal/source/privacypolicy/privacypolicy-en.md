# Privacy Policy

## 1. Introduction

Drink Water App is a hydration tracking application developed and operated as an independent
project. This Privacy Policy explains what information is collected when you use the App, how it is
used, and what controls you have over it.

By installing or using Drink Water App, you agree to the practices described in this policy. If you
do not agree, please uninstall the App.

## 2. Information We Collect

### 2.1 Information You Provide (stored locally on your device)

During onboarding and normal use, the App asks for:

- **Privacy policy acceptance** — legally acknowledge how the App uses the information you provide
- **Terms of use acceptance** — accept the guidelines and rules for using the App
- **Usage tracking consent** — choose whether to send app feature usage data for analysis to improve
  the user experience
- **Unit preference (ml / oz / cups)** — display measurements in your preferred format
- **Daily water goal** — set the amount of water you want to drink each day
- **Wake time / sleep time** — schedule drink reminders within active hours
- **Reminder frequency** — control how often notifications are sent
- **Notification quick-add amount** — log a predefined amount of water consumed directly from
  notifications

**All of the above is stored on your device** using the device local storage. We do not operate
servers that store it. It is never transmitted to us or any third party, except the portion you
choose to back up if you enable the optional Cloud Backup feature (see 2.7).

### 2.2 App Usage Data (stored locally on your device)

The App records the following data as you use it:

- **Water intake logs:** amount, timestamp, and entry type (manual, from notification, or skipped).
- **Daily hydration goals:** target amounts and effective dates.
- **Drink suggestions:** custom quick-add amounts you create.

This data is stored locally on your device, unless you enable the optional Cloud Backup feature
(see 2.7).

### 2.3 Anonymous Usage Analytics (optional, opt-out available)

With your consent, the App sends anonymous usage events to **Mixpanel** to help us understand which
features are used and improve the App. Specifically:

- App opened / onboarding started, completed, or abandoned
- Drink recorded and source of the "drink" action (e.g.: notification or app)
- Custom preset created
- Drink suggestions quick-add sheet opened
- Reminders tab opened, and reminder active-day changes (which days of the week reminders are
  on/off, and how many)
- Cloud backup actions: connecting or disconnecting the backup, tapping manual sync or restore,
  changing the auto-backup schedule, toggling sync-on-save, and when a sync finishes (with its
  result). These events include an anonymous count of how many of your own devices use the backup —
  never the file contents or any account identity.

**No personal data, water log content, or identifiable information is included in these events.**
Each device is assigned a random anonymous ID that is not linked to your name, email, or any other
identity. The App does not collect your name, email address, or any other identity information from
any source.

- Mixpanel data is processed on servers located in the **European Union**.
- You can opt out at any time from **Settings → Privacy & Legal → Usage Tracking**.
- Once opted out, no further events are sent and the anonymous ID is reset.

### 2.4 Crash Reports

The App uses **Firebase Crashlytics** (Google) to automatically collect crash reports when the App
unexpectedly stops. These reports contain stack traces, device model and Android version, app
version, and time of crash. Crash reports do not include your personal data or water logs. They are
used solely to identify and fix bugs.

### 2.5 Advertising Data

The free version uses **Google AdMob**, which may collect device advertising identifiers to show
relevant ads. You can reset or limit your advertising ID in your device's Google settings.

Purchasing **Remove Ads** permanently disables all ads and associated tracking. We ensure no further
connections are made to advertising services once the ad-free purchase is completed. If you notice
any continued ad-related activity after purchasing, please report this unusual behavior so we can
take immediate action.

### 2.6 Purchase Information

In-app purchases are processed through **RevenueCat**, which receives Google Play purchase tokens
(issued by Google, not by us) and your entitlement status (whether ads are removed). RevenueCat does
not receive any other sensitive information. If you notice any unusual activity, we encourage you to
report it so we can take appropriate action. See RevenueCat's Privacy Policy for details.

### 2.7 Cloud Backup & Sync (optional, off by default — Google Drive)

The App offers an **optional** cloud backup so you can protect your data and synchronize it across
your own devices. It is **disabled by default** and only activates after you explicitly connect your
Google Drive account and authorize access.

When enabled, the App uploads a backup file containing your app data to your Google Drive account:

- **Water intake logs** (amount, timestamp, entry type)
- **Daily hydration goals** (target amounts and effective dates)
- **Preferences** (unit, time format, notification quick-add amount)
- A random per-device identifier used only to merge data across your own devices

The backup does **not** include your name, email, payment data, or any advertising or analytics
identifiers.

**Where your backup is stored:** the file is written to your private, hidden *application data
folder* (`appDataFolder`) in Google Drive — a Drive area reserved for the App that does not appear
among your normal Drive files and is not accessible to other apps. The App requests the
`drive.appdata` scope plus your basic Google profile (email and name) solely to show which account
is connected.

The backup is stored in **your own** Google Drive account. We do not operate any server that stores
this data, and the developer cannot read your backup. You can disconnect at any time from
**Settings → Backup**, and you can delete the backup file directly from your Google Drive account.

**Google API Services — Limited Use disclosure.** Drink Water App's use of information received from
Google APIs adheres to
the [Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy),
including the **Limited Use** requirements. Data accessed through Google Drive is used **only** to
provide and improve the backup-and-sync feature you requested. It is never sold, never used for
advertising, and is never transferred to others except as needed to provide the feature, to comply
with applicable law, or as part of a merger or acquisition. We do not allow humans to read your
Google Drive data unless we have your explicit consent, it is necessary for security purposes (such
as investigating abuse), or to comply with applicable law.

## 3. Permissions Requested

- **POST_NOTIFICATIONS** — send drink water reminder notifications
- **SCHEDULE_EXACT_ALARM** — deliver reminders at the precise scheduled time
- **RECEIVE_BOOT_COMPLETED** — restore scheduled reminders after the device restarts
- **SYSTEM_ALERT_WINDOW** — show a goal-reached celebration overlay above other apps
- **INTERNET** — required for ads (free version), analytics, purchase validation, and optional cloud
  backup

No location, contact, camera, microphone, or health permissions are requested.

## 4. How We Use Your Information

- Deliver core hydration tracking features — all locally stored data
- Send drink reminders — active hours, reminder frequency
- Show personalized ads (free version) — AdMob device advertising ID
- Understand feature usage and improve the App — anonymous Mixpanel events (opt-in)
- Fix crashes and bugs — Firebase Crashlytics reports
- Process in-app purchases — RevenueCat purchase tokens
- Back up and sync your data (optional) — water logs, goals, preferences uploaded to your Google
  Drive

We do not use your data for profiling, automated decision-making, or any purpose beyond those listed
above.

## 5. Data Sharing and Disclosure

We do not sell, rent, or trade your personal information.

- **Google Firebase (Crashlytics)** — crash reporting — device info, crash traces
- **Mixpanel** — analytics (opt-in only) — anonymous events, random device ID
- **Google AdMob** — advertising (free version only) — advertising ID
- **RevenueCat** — purchase management — Google Play purchase tokens
- **Google Drive** — cloud backup (optional, you connect it) — backup file in your private app data
  folder; your email/name to show the connected account

We may also disclose information if required by law or to protect the rights, safety, or property of
users or the public. The App generates a unique ID per installation, which means it is not possible
to identify a specific user unless local device files are accessed. In any case, any user may
request
removal of their data at any time; reinstalling the App also permanently deletes all local data.

Tracking information is used solely to determine which settings and features are most commonly used,
in order to set better defaults in future updates and to add or remove features based on usage
patterns. All of this is aimed at improving the user experience.

## 6. Data Retention and Deletion

- **Local app data** remains on your device until you uninstall the App or use Settings → Clear all
  data.
- **Mixpanel analytics** data is retained per Mixpanel policy (typically 5 years). Opt out anytime
  to stop future collection.
- **Crashlytics** reports are retained by Google per Firebase policies.
- **RevenueCat** purchase records are retained as required by app store terms and applicable law.
- **Cloud backup** data lives in your own Google Drive account and remains until you delete it there
  or disconnect the backup. We have no control over, and cannot access, files stored in your
  account.

## 7. This App Is Not a Medical Application

Drink Water App is a personal wellness utility designed to help you build a hydration habit. It is
**not a medical device** and does not provide medical advice, diagnosis, or treatment. The daily
water goal suggested by the App is an estimate based on commonly used formulas and is not a
substitute for advice from a qualified healthcare professional. Consult your doctor for any
health-related decisions.

## 8. Children's Privacy

The App is simple to use and may be used by people of any age. We recommend that minors be guided by
a parent or guardian before using the App. The App is not directed at minors. We do not knowingly
collect personal information from minors. If you are a parent or guardian and believe your child has
provided personal data through the App, please contact us at eahm3595@gmail.com and we will take
appropriate steps to address it.

## 9. Data Security

All data you enter into the App is stored locally on your device's storage. We do not operate
servers
that store your personal hydration data. If you enable Cloud Backup, your data is transmitted over
an
encrypted connection and stored in your own Google Drive account, protected by Google's security and
your account credentials. However, no method of electronic storage or transmission is completely
secure, and we encourage you to protect your device and your Google account with the recommended
security techniques.

## 10. Your Rights

- **Access** — all your data is visible within the App itself.
- **Correct** — edit data via the App's settings and onboarding screens.
- **Delete** — use Settings → Clear Data, or uninstall the App.
- **Withdraw consent** for analytics — Settings → Privacy & Legal → Usage Tracking.
- **Object or restrict** processing — contact us for requests beyond what the App covers.

**EEA users:** you may lodge a complaint with your local data protection authority.

**California (CCPA):** we do not sell personal information as defined by the CCPA.

## 11. Changes to This Privacy Policy

We may update this Privacy Policy from time to time. When we do, we will update the "Last updated"
date at the top of this document and, if the changes are material, notify you within the App on next
launch. Continued use of the App after changes are posted constitutes your acceptance of the updated
policy.

## 12. Contact

**Email:** eahm3595@gmail.com
**App:** Drink Water App (com.eahm.drinkwaterapp)
