# Plan: Dual Alarm System — Exact Permission Fallback

## Problem

When `SCHEDULE_EXACT_ALARM` is revoked, Android silently cancels all exact alarms.
There is no system broadcast for the revocation event, so the app cannot react to it in the
background. Result: all reminders stop until the user opens the app or re-grants the permission.

---

## Solution: Two alarms running in parallel

| Alarm         | ID  | Action                 | Type                        | Role                                                          |
|---------------|-----|------------------------|-----------------------------|---------------------------------------------------------------|
| Exact alarm   | 127 | `EXACT_ALARM_ACTION`   | `setExactAndAllowWhileIdle` | Primary — shows notification when exact permission is granted |
| Inexact alarm | 128 | `INEXACT_ALARM_ACTION` | `setAndAllowWhileIdle`      | Guard — always active, acts as fallback                       |

Both alarms are always scheduled for the same target time.
Only one shows a notification at a time, depending on permission state.

---

## States

```
┌──────────────────────────────────────────────────────────────┐
│  NORMAL  (exact permission granted)                          │
│  Exact alarm   ──► fires ──► check exact permission state    |
|                              yes: schedule next exact alarm  │
|                              yes: show notification          │
|                              no: do nothing                  │
│  Inexact alarm ──► fires ──► check exact permission state    │
|                              yes: check exact alarm exists   │
|                              yes: yes: do nothing            │
|                              yes: no: reschedule exact alarm │
│                              no: show notification           │
│                              schedule next inexact alarm     │
└──────────────────────────────────────────────────────────────┘
         │ permission revoked                ▲
         │ (Android cancels exact alarm)     │ broadcast received
         ▼                                   │ restoreAlarm() called
┌───────────────────────────────────────────────────────────────┐
│  GUARD  (exact permission revoked)                            │
│  Exact alarm   ──► DEAD                                       │
│  Inexact alarm ──► fires ──►  check exact permission state    │
│                               yes: check exact alarm exists   │
│                               yes: yes: do nothing            │
│                               yes: no: reschedule exact alarm │
│                               no: show notification           │
│                              schedule next inexact alarm      │
└───────────────────────────────────────────────────────────────┘
```

Inexact alarm can arrive before or after the exact alarm, and in some edge cases at the same time.
Rescheduling calls must be executed sequentially (via mutex) to avoid race conditions.
In both decision trees, rescheduling always compares the calculated next time against the currently
scheduled alarm time. If they are the same, nothing is done. If they differ, the alarm is updated.
This means when both alarms fire simultaneously, one receiver will do nothing because the other
already rescheduled to the same value.

---

## Inexact alarm fires — decision tree

```
           InexactAlarm fires
                   │
                   ▼
           canScheduleExactAlarms()?
        YES                       NO
        │                         │
        │                         │
        ▼                         ▼
  isExactAlarmActive()?      show notification
  YES             NO              │
  │               │               │
  ▼               ▼               │
do nothing    reschedule next     │
  │           exact alarm         │
  │           (restored)          │
  │               │               │
  │               │               │
  ▼               ▼               ▼
    calculate time for next inexact alarm.
    if same time as existing inexact alarm → do nothing
    if different time → reschedule inexact alarm with new time
```

---

## Exact alarm fires — decision tree

```
           ExactAlarm fires
                 │
                 ▼
         canScheduleExactAlarms()?
      YES                       NO
      │                         │
      ▼                         ▼
reschedule next            do nothing
exact alarm
      │
      ▼
show notification
```

The reschedule logic always compares the value of the existing alarm with the newly calculated time.
If the same, do nothing. If different, override with the new value.

---

## Alarm lifecycle

```
scheduleAlarm() called
  ├── setExactAndAllowWhileIdle  → exact alarm  (ID 127, EXACT_ALARM_ACTION)
  └── setAndAllowWhileIdle       → inexact alarm (ID 128, INEXACT_ALARM_ACTION)

cancelExactAlarm() called
  └── cancel exact alarm (ID 127)

cancelInexactAlarm() called
  └── cancel inexact alarm (ID 128)

cancelAllAlarms() called
  ├── cancelExactAlarm()
  └── cancelInexactAlarm()

Exact alarm fires (EXACT_ALARM_ACTION)
  └── see decision tree above

Inexact alarm fires (INEXACT_ALARM_ACTION)
  └── see decision tree above

Onboarding
  └── scheduleAlarm() → both alarms scheduled

onResume
  └── check first setup ready → check alarms don't exist → scheduleAlarm() → both alarms rescheduled

Boot / package replaced / permission re-granted
  └── check first setup ready → check alarms don't exist → scheduleAlarm() → both alarms rescheduled

Settings / DevMenu screens
  └── scheduleAlarm() → both alarms rescheduled
```

---

## Files to change

| File                         | Change                                                                                                                                                                                                                                                                                                                                                                                             |
|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AndroidManifest.xml`        | Add `EXACT_ALARM_ACTION` and `INEXACT_ALARM_ACTION` to `AlarmReceiver` intent-filter                                                                                                                                                                                                                                                                                                               |
| `AlarmReceiver.kt`           | Add `EXACT_ALARM_ACTION` and `INEXACT_ALARM_ACTION` constants; add `when` branch for each wired to `DrinkAlarmManagerV2`; update boot / package-replaced / permission-granted branches to also call `drinkAlarmManagerV2.restoreAlarm()`; keep existing `ALARM_ACTION` branch wired to original `DrinkAlarmManager` untouched                                                                      |
| `AlarmSchedulerV3.kt`        | New scheduler. Receives a pre-calculated target time (hour, minute) and schedules it. Does not contain business logic. Exposes `scheduleExactAlarm()`, `scheduleInexactAlarm()`, `isExactAlarmActive()`, `isInexactAlarmActive()`, `areAlarmsActive()`, `cancelExactAlarm()`, `cancelInexactAlarm()`, `cancelAllAlarms()`.                                                                         |
| `DrinkAlarmManagerV2.kt`     | New manager. Contains the hardcoded test slots and next-slot selection logic. Uses `AlarmSchedulerV3`. `EXACT_ALARM_ID = 127`, `INEXACT_ALARM_ID = 128`. Adds `onInexactAlarmReceived()`; `scheduleDrinkAlarm()` calculates the next slot and schedules both alarms; `stopAlarm()` and `clear()` call `cancelAllAlarms()`; all scheduling paths (including `restoreAlarm()`) share a single mutex. |
| `DrinkAlarmManagerModule.kt` | Register `DrinkAlarmManagerV2` as a Koin singleton so `AlarmReceiver` can resolve it via `GlobalContext.get<DrinkAlarmManagerV2>()`                                                                                                                                                                                                                                                                |

`DrinkAlarmManager.kt` is **not modified**. The original manager and its alarms (`ALARM_ACTION`, ID

127) continue to work independently during the testing phase. Once `DrinkAlarmManagerV2` is
     validated, the original can be removed in a follow-up.

---

## Hardcoded test data (device testing phase)

`AlarmSchedulerV3` and `DrinkAlarmManagerV2` use hardcoded values instead of reading from
`PrefsStorage`. This isolates the new logic from user configuration during testing.

```
Start time : 06:00
End time   : 18:00

Alarm slots:
  1 → 10:00
  2 → 13:00
  3 → 16:00
```

**Next alarm calculation rule:**
Given the current device time, pick the first slot that is still in the future today.
If all three slots have already passed, pick slot 1 (10:00) for the next day.

```
Examples:
  current time 08:00 → next alarm 10:00 (today)
  current time 11:00 → next alarm 13:00 (today)
  current time 14:00 → next alarm 16:00 (today)
  current time 17:30 → next alarm 10:00 (tomorrow)
```

The slot selection logic lives in `DrinkAlarmManagerV2`, not in `AlarmSchedulerV3`. The scheduler
only receives a pre-calculated target (hour, minute) and registers it with AlarmManager.
`DrinkAlarmManagerV2` owns the slot list, the "next slot" calculation, and the decision of what
time to pass to the scheduler.

These constants are replaced with dynamic `PrefsStorage` values when the testing phase is complete.

### First scheduling entry points (testing phase)

`DrinkAlarmManagerV2` is new and not wired into the existing onboarding flow. For testing, two
entry points trigger its first `scheduleAlarm()` call:

1. **Dev menu** — add a button that calls `drinkAlarmManagerV2.restoreAlarm("devMenu")` to start
   both alarms for the first time on a fresh install.
2. **Boot / package replaced / permission re-granted** — `AlarmReceiver` already calls
   `drinkAlarmManager.restoreAlarm()` for these events. Add a parallel call to
   `drinkAlarmManagerV2.restoreAlarm()` in the same branches. If no alarms exist yet, V2's
   `whatToDoService` will schedule them; if they already exist and are correct, it does nothing.

---

## Edge cases

### Race condition — both alarms fire simultaneously

`goAsync()` runs each `onReceive()` in a coroutine. If both alarms fire at the same time, both
coroutines can reach the "should I show notification?" check before either finishes rescheduling,
resulting in two notifications for the same slot.

**Fix:** `DrinkAlarmManagerV2` has its own `schedulingMutex` that wraps all scheduling paths:
`onAlarmReceived()`, `onInexactAlarmReceived()`, and `restoreAlarm()`. The first caller wins and
reschedules both alarms. The second caller runs after the lock is released, calculates the same
next time, finds the alarm already set to that value, and does nothing.

### `cancelAllAlarms()` — two independent cancel methods

Both alarms are treated as independent. `cancelExactAlarm()` and `cancelInexactAlarm()` each cancel
their own alarm individually. `cancelAllAlarms()` is a convenience method that calls both.
`stopAlarm()` and `clear()` in `DrinkAlarmManagerV2` call `cancelAllAlarms()`.

### `AlarmSchedulerV3` — new scheduler, out of scope for existing schedulers

`AlarmSchedulerV2` has a conflicting single-alarm design (routes both exact and inexact through
`ALARM_ACTION` and the same ID). It is not touched.
`AlarmSchedulerV3` is a clean implementation built for the dual-alarm system. It handles
`EXACT_ALARM_ACTION` and `INEXACT_ALARM_ACTION` as fully independent alarms with separate IDs,
separate PendingIntents, and separate active/cancel methods. Active hours are hardcoded for the
initial testing phase.

### `isExactAlarmActive()` vs `isInexactAlarmActive()`

Both alarms are probed independently:

- `isExactAlarmActive(context, id)` — probes `PendingIntent` for `EXACT_ALARM_ACTION`
- `isInexactAlarmActive(context, id)` — probes `PendingIntent` for `INEXACT_ALARM_ACTION`
- `areAlarmsActive(context)` — returns true only if both are active

The decision trees in `onInexactAlarmReceived` and `onAlarmReceived` use the correct probe for
each alarm type.

### Inexact timing — Doze delay is acceptable

`setAndAllowWhileIdle()` may be delayed by Android in Doze mode. This is acceptable: the inexact
alarm is a probability-based safety net, not a precision timer.

- If the exact alarm fires first (normal state): the inexact alarm fires after, sees the exact alarm
  is already rescheduled to the same next time, and does nothing.
- If the inexact alarm fires first (exact permission still granted): it reschedules the next inexact
  alarm. When the exact alarm fires shortly after, it recalculates the next time and also
  reschedules
  the inexact alarm. If the value matches what the inexact already set, nothing is overwritten.

### Double scheduling on permission-granted broadcast

When the user grants exact permission, `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` fires
AND the calling screen may also trigger `scheduleAlarm()` on resume. Both calls are idempotent
(same alarm ID overwrites the previous PendingIntent with the same value). The mutex prevents
concurrent execution and ensures only one notification is shown.

The `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` branch in `AlarmReceiver` must call
`drinkAlarmManagerV2.restoreAlarm()` (in addition to the existing V1 call) so V2 reschedules the
exact alarm immediately when permission is re-granted.

---
