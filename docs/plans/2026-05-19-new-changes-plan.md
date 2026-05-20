# Plan: New Changes — 2026-05-19

Source: `docs/new-changes.md`

---

## Resumen general

15 cambios identificados. Organizados de menor a mayor complejidad. Cada uno tendrá su propio commit.

---

## Tareas

### T01 — Habilitar botón "Request a feature" en prod
**Archivo:** `compose/build.gradle.kts`
**Cambio:** Cambiar `FEATURE_REQUEST_FEATURE` de `"false"` a `"true"` en el buildType `release`.
**Contexto:** Actualmente está en `false` solo para release. En debug/staging/qa ya está en `true`.

---

### T02 — Corregir intervalo en Settings screen (muestra "every 0 min")
**Archivo:** `compose/.../feature/settings/SettingsViewModel.kt` línea 127
**Problema:** Al guardar el intervalo desde onboarding, se almacena como `ActiveTime(hour=1, minute=0)` para 60 min. Pero el ViewModel solo lee `minute` (= 0). Fix: leer `hour * 60 + minute`.
**Cambio:**
```kotlin
// Antes
intervalMinutes = reminderInfo?.second?.minute ?: 0,
// Después
intervalMinutes = reminderInfo?.second?.let { it.hour * 60 + it.minute } ?: 0,
```
**Nota adicional:** Normalizar también el almacenamiento en `onIntervalChanged` para evitar inconsistencias futuras (usar `ActiveTime(hour=minutes/60, minute=minutes%60)` igual que onboarding).

---

### T03 — Corregir texto vacío en drink log
**Archivo:** `compose/.../res/values/strings.xml`
**Cambio:** `drink_log_empty_state` de `"No drinks logged yet. Tap Add water to start."` a `"Log a drink to see it here."` (y en español si aplica).

---

### T04 — Corregir "every" hardcodeado en paso final de onboarding (StepDone)
**Archivo:** `compose/.../feature/onboarding/ui/steps/StepDone.kt`
**Problema:** `formatInterval()` retorna cadenas como `"every 45 min"`, `"every 1h"` con texto hardcodeado en inglés.
**Cambio:** Agregar string resources y usar `Context`/`stringResource` para formatear con localización. Ejemplo: `onb_done_interval_min = "every %1$d min"`, `onb_done_interval_hour = "every %1$dh"`, `onb_done_interval_hour_min = "every %1$dh %2$dmin"`.
**Nota:** La función `formatInterval` es `private fun` pura (no-Composable), necesitará recibir los strings formateados como parámetros o moverse a un Composable context.

---

### T05 — Corregir texto "Battery-safe" en paso exact alarm (onboarding)
**Archivo:** `compose/.../res/values/strings.xml`
**Problema:** `onb_exact_alarm_benefit_reliable_label` = "Battery-safe and reliable" y `onb_exact_alarm_benefit_reliable_description` = "Works even with battery optimization, so you never miss a reminder." — El texto implica que no gasta batería, lo cual no es 100% preciso.
**Cambio:** Actualizar label y description con texto más honesto sobre lo que hace la alarma exacta, sin prometer que es "battery-safe". Ejemplo: `"Reliable and consistent"` / `"Reminds you exactly on schedule, even when other apps are in the background."`.

---

### T06 — Reemplazar nombre de la app hardcodeado con string resource
**Archivos:** Todos los archivos que usen literales como `"DrinkWater"`, `"Drink Water App"`, `"DrinkWater app"` en strings.xml y Kotlin
**Enfoque:** Buscar en `strings.xml` (compose y app modules) y reemplazar instancias relevantes con `%1$s` + `app_name` en los strings, o con `getString(R.string.app_name)` en código Kotlin. Principalmente en el paso de alarmas exactas en onboarding (steps 2-4 mencionan "DrinkWater" explícitamente), y en los emails de feature request/problem report.

---

### T07 — Centrar verticalmente número de paso y texto en StepExactAlarm (HowToStepRow)
**Archivo:** `compose/.../feature/onboarding/ui/steps/StepExactAlarm.kt`
**Problema:** En `HowToStepRow`, el `Row` tiene `verticalAlignment = Alignment.Top`, lo que hace que el número del paso y el texto queden desalineados cuando el texto es corto.
**Cambio:** Cambiar a `verticalAlignment = Alignment.CenterVertically`.

---

### T08 — Corregir promedio en stats screen (divisor usa días transcurridos en vez de 7)
**Archivo:** `compose/.../feature/stats/StatsViewModel.kt`, función `computeSummary`
**Problema:** Para el período actual, solo divide por los días transcurridos (e.g., 2 si hoy es martes). Usuario quiere dividir siempre por el tamaño completo del período (7 para semanas, días del mes para meses, 12 para años).
**Cambio:** Usar `pages[pageIndex].entries.size` como divisor en vez de `periodEntries.size`.

---

### T09 — Simplificar descripción de unidades (fl oz) y detectar locale automático
**Archivos:** `strings.xml`, `DataStorePrefsStorage.kt` / `PrefsStorage.kt`
**Cambio 1:** En `strings.xml`, simplificar `settings_unit_fl_oz_desc` de `"US fluid ounces"` a `"Fluid ounces"` (quitar la referencia a "EEUU"). Hacer lo mismo con labels redundantes si aplica.
**Cambio 2:** En `DataStorePrefsStorage`, si no hay preferencia guardada, detectar el locale del teléfono y devolver `FL_OZ` si el locale es US, de lo contrario `ML`. Esto se puede hacer en el `Flow` de `getUnitPreference()` como valor por defecto calculado.

---

### T10 — Mostrar mensaje de "fuera de horas activas" en pill del home screen
**Archivos:** `DrinkScreen.kt`, `DrinkState.kt`, `DrinkViewModel.kt`
**Cambio:**
1. Agregar campo `isOutsideActiveHours: Boolean` a `DrinkState.Data`
2. En `DrinkViewModel`, combinar `ActiveHoursObserver.inActiveHours` al estado
3. En `RotatingInfoPill`, agregar nuevo tipo de mensaje `OutsideActiveHours` que muestre: "Notifications are paused outside active hours, but you can still log manually."
4. Mostrar este mensaje cuando `isOutsideActiveHours = true`
**Nota:** El salto visual (UI jump) al cambiar el texto ya tiene `animateContentSize` y `AnimatedContent`, pero verificar que no haya saltos al cambiar entre mensajes de diferente longitud.

---

### T11 — Mostrar unidades en onboarding (selección de unidades para usuarios de EEUU)
**Archivos:** `OnboardingScreen.kt`, `OnboardingViewModel.kt`, `OnboardingState.kt`, nuevo `StepUnits.kt` (o modificar StepInterval/StepGoal)
**Cambio:**
1. Agregar un nuevo paso en el onboarding después del paso de goal o interval para seleccionar unidades (ML vs FL_OZ).
2. Reutilizar el patrón de `UnitPreferenceBottomSheet` / `UnitPreferenceOption` en un nuevo `StepUnits.kt`.
3. Actualizar `OnboardingSteps.totalSteps` acordemente.
4. Si el usuario selecciona FL_OZ, la pantalla de `StepSetGoal` (donde se muestra el goal calculado) debe mostrar el valor en oz también.
5. En `OnboardingViewModel.onIntervalSelect`, guardar también la preferencia de unidad.

---

### T12 — Dialog de primer registro de agua (first drink guidance)
**Archivos:** `PrefsStorage.kt`, `DataStorePrefsStorage.kt`, `DrinkViewModel.kt`, `DrinkState.kt`, `DrinkRoot.kt`, nuevo `FirstDrinkDialog.kt`
**Cambio:**
1. Agregar en `PrefsStorage` el método `getFirstDrinkDialogSeen(): Flow<Boolean>` y `setFirstDrinkDialogSeen()`.
2. Implementar en `DataStorePrefsStorage`.
3. En `DrinkViewModel.onDrink()`, después del primer registro exitoso, emitir un evento para mostrar el diálogo.
4. Crear `FirstDrinkDialog.kt`: un `BasicAlertDialog` que explique "You just logged your first drink!" y cómo deshacer (deslizar/eliminar).
5. En `DrinkRoot.kt`, escuchar el evento y mostrar el diálogo.

---

### T13 — Animación de agua subiendo con burbujas al registrar agua
**Archivos:** `DrinkScreen.kt`, `ProgressRing.kt`, posiblemente nuevo `WaterRisingAnimation.kt`
**Cambio:**
- Al registrar agua (`onDrink` event), mostrar una animación superpuesta sobre el progress ring: el agua "sube" visualmente con burbujas ascendentes.
- Implementar como un `Canvas`-based overlay usando `Animatable` para el nivel del agua y `LaunchedEffect` para las burbujas.
- La animación dura ~2 segundos y desaparece automáticamente.
- Disparar desde `DrinkRoot` cuando recibe el evento de "drink registered".

---

### T14 — Corregir icono monocromático
**Archivos:** `app/src/main/res/mipmap-anydpi-v26/ic_app_3.xml`, `ic_app_3_round.xml`
**Problema:** El ícono monocromático muestra una gota de agua en vez del ícono correcto. Android 13+ usa el nodo `<monochrome>` en el adaptive icon XML.
**Cambio:**
- Verificar si hay un `<monochrome>` apuntando a un drawable incorrecto.
- Si no hay `<monochrome>`, el sistema usa el `foreground` como fallback, que podría ser una gota simple.
- Agregar `<monochrome android:drawable="@mipmap/ic_app_foreground_3"/>` (o el drawable correcto) al XML del adaptive icon para que Android use el foreground real como base monocromática.

---

### T15 — Permitir ingreso de cantidades personalizadas (custom text input)
**Archivos:** `IntervalBottomSheet.kt`, `NotificationAmountBottomSheet.kt`, `GoalBottomSheet.kt`, posiblemente `AddDrinkButton.kt`
**Cambio:**
- En los bottom sheets que usan steppers (+/-), agregar un `TextField` que permita al usuario escribir directamente el valor.
- El campo de texto y los botones +/- coexisten: el campo muestra el valor actual y al escribir actualiza el state.
- Validar el rango permitido (min/max) al confirmar con "Save".
- Reutilizar el componente `inputfield/` existente del proyecto.

---

## Orden de ejecución

| # | Tarea | Complejidad |
|---|-------|-------------|
| 1 | T01 — Feature request en prod | Baja |
| 2 | T02 — Fix intervalo en Settings | Baja |
| 3 | T03 — Texto vacío en drink log | Baja |
| 4 | T04 — Fix "every" en StepDone | Baja-media |
| 5 | T05 — Fix texto "Battery-safe" | Baja |
| 6 | T06 — Reemplazar nombre app hardcodeado | Baja-media |
| 7 | T07 — Centrar pasos en StepExactAlarm | Baja |
| 8 | T08 — Fix promedio en stats | Baja |
| 9 | T09 — Simplificar desc. unidades + locale | Media |
| 10 | T10 — Mensaje fuera de horas activas | Media |
| 11 | T11 — Unidades en onboarding | Media-alta |
| 12 | T12 — Dialog primer registro | Alta |
| 13 | T13 — Animación agua + burbujas | Alta |
| 14 | T14 — Fix ícono monocromático | Media |
| 15 | T15 — Input personalizado en steppers | Alta |
