# Mixpanel Event Analysis Guide — DrinkWaterApp

Guía de referencia para analizar eventos de la app en la consola de Mixpanel.
Cubre la estructura de eventos, super properties, filtros por versión, y los reportes recomendados para cada pantalla.

---

## 1. Conceptos base

### Eventos vs. Super Properties

| Concepto | Qué es | Ejemplo |
|----------|--------|---------|
| **Evento** | Una acción puntual del usuario | `screen_viewed` |
| **Propiedad del evento** | Dato adjunto a ese evento específico | `screen: "stats"` |
| **Super Property** | Dato adjunto automáticamente a **todos** los eventos sin excepción | `$app_version_string: "2.0.0"` |

Las super properties se configuran una vez en el SDK. A partir de ese punto, cada evento enviado las lleva consigo. En DrinkWaterApp la versión de la app se registra como super property al iniciar el servicio de tracking.

### Super property registrada

| Nombre en Mixpanel | Valor | Cómo aparece en la UI |
|---|---|---|
| `$app_version_string` | `"2.0.0"`, `"2.1.0"`, etc. | **App Version String** |

> El prefijo `$` le indica a Mixpanel que es una propiedad estándar. Aparece en los filtros como campo nativo, no como custom property.

---

## 2. Catálogo completo de eventos

### 2.1 Navegación principal

| Evento | Propiedad | Valores posibles |
|--------|-----------|-----------------|
| `screen_viewed` | `screen` | `today`, `history`, `stats`, `settings` |

Se dispara cada vez que el usuario cambia de pestaña en el bottom navigation. También se dispara al abrir la app (pantalla `today` es la inicial).

**Pregunta que responde:** ¿Cuál es la pantalla más visitada?

---

### 2.2 Today (pantalla principal de bebida)

| Evento | Propiedades | Descripción |
|--------|-------------|-------------|
| `drink_recorded` | `source`, `amount_ml` | El usuario registró agua |
| `preset_bottom_sheet_opened` | `preset_count` | Abrió el sheet de presets |
| `preset_created` | `amount_ml`, `total_presets` | Creó un nuevo preset |

**Valores de `source`:**
- `quick_suggestion` — chip rápido en la pantalla principal
- `bottom_sheet_suggestion` — chip dentro del sheet
- `bottom_sheet_custom` — cantidad personalizada en el sheet

---

### 2.3 History

| Evento | Descripción |
|--------|-------------|
| `history_edit_amount_tapped` | El usuario tocó editar cantidad de un registro |
| `history_edit_time_tapped` | El usuario tocó editar hora de un registro |
| `history_delete_tapped` | El usuario tocó eliminar un registro |
| `history_filter_changed` | El usuario cambió la fecha del filtro |

**Pregunta que responde:** ¿Qué tan activos son los usuarios corrigiendo su historial? ¿Conviene hacer edición más accesible o menos prominente?

---

### 2.4 Stats

| Evento | Propiedad | Valores posibles |
|--------|-----------|-----------------|
| `stats_period_selected` | `period` | `week`, `month`, `year` |
| `stats_bar_tapped` | — | El usuario tocó una barra del gráfico para ver el tooltip del día |
| `stats_personal_best_tapped` | — | El usuario tocó la tarjeta de personal best para ver la fecha |

**Pregunta que responde:** ¿Cuál período prefieren los usuarios? ¿Los tooltips del gráfico son descubiertos y usados?

---

### 2.5 Settings

| Evento | Propiedades | Descripción |
|--------|-------------|-------------|
| `settings_daily_goal_changed` | `goal_ml` | Cambió el goal diario |
| `settings_remove_ads_tapped` | — | Abrió el sheet de Remove Ads |
| `settings_volume_unit_changed` | `unit` | Cambió las unidades (`ml`, `auto_liters`, `fl_oz`) |
| `settings_reminder_type_opened` | — | Fue a ajustes del sistema para cambiar tipo de alarma |
| `settings_notifications_disabled` | — | Confirmó ir a ajustes para desactivar notificaciones |
| `settings_quick_drink_changed` | `amount_ml` | Cambió la cantidad del botón de notificación |
| `settings_start_time_changed` | `hour`, `minute` | Cambió hora de inicio de recordatorios |
| `settings_end_time_changed` | `hour`, `minute` | Cambió hora de fin de recordatorios |
| `settings_goal_reached_opened` | — | Fue a ajustes para activar la celebración overlay |
| `settings_share_tapped` | — | Tocó "Share with friends" |
| `settings_rate_app_tapped` | — | Tocó "Rate the app" |
| `settings_feature_request_tapped` | — | Tocó "Feature request" |
| `settings_report_problem_tapped` | — | Tocó "Report a problem" |
| `settings_reset_all_data_confirmed` | — | Confirmó reset total de datos (después del countdown) |

---

### 2.6 Notificaciones

| Evento | Descripción |
|--------|-------------|
| `notification_drink_tapped` | El usuario tocó el botón de beber en la notificación |
| `notification_skip_tapped` | El usuario tocó Skip en la notificación |
| `notification_opened` | El usuario tocó el cuerpo de la notificación para abrir la app |

**Pregunta que responde:** ¿Las notificaciones generan acción directa (drink) o solo se ignoran o se saltan?

---

### 2.7 Onboarding

| Evento | Propiedad | Descripción |
|--------|-----------|-------------|
| `onboarding_started` | — | El usuario comenzó el onboarding |
| `onboarding_completed` | — | Completó el onboarding |
| `onboarding_abandoned` | `step` | Salió antes de completar |
| `onboarding_time` | — | Evento de tiempo (duración total del onboarding) |

---

### 2.8 Sistema / Privacidad

| Evento | Propiedad | Descripción |
|--------|-----------|-------------|
| `tracking_opted_out` | `source` | El usuario desactivó el tracking (`Welcome`, `Settings`, `DevMenu`) |

---

## 3. Filtrar eventos por versión de la app

### 3.1 Dónde agregar el filtro

En cualquier reporte de Mixpanel (Insights, Funnels, Flows, Retention):

1. Haz clic en **+ Filter** (o **Add Filter**) en la barra superior del reporte.
2. Busca el campo **App Version String** (es el nombre que Mixpanel muestra para `$app_version_string`).
3. Elige el operador y el valor.

### 3.2 Operadores útiles

| Objetivo | Operador | Valor |
|----------|----------|-------|
| Solo versión 2.0.0 exacta | `is` | `2.0.0` |
| Versión 2.0 en adelante | `is greater than or equal to` | `2.0.0` |
| Excluir datos sin versión (anteriores al tracking) | `is set` | — |
| Comparar dos versiones | Usa **Breakdown** por `App Version String` | — |

> **Nota sobre comparación lexicográfica:** Mixpanel compara versiones como strings. El orden `1.9.0 < 2.0.0 < 2.1.0` funciona correctamente mientras el formato sea siempre `X.Y.Z`. Si el formato cambia (ej. `10.0.0` vs `9.0.0`) puede haber resultados inesperados porque `"10" < "9"` lexicográficamente. Usa el filtro de fecha como alternativa más segura cuando conozcas la fecha de release.

### 3.3 Alternativa: filtro por fecha

Si sabes cuándo se publicó la versión 2.0.0 en Play Store, puedes filtrar por rango de fechas:

1. En el selector de fecha del reporte, elige **Custom Range**.
2. Establece la fecha de inicio como el día del release de la versión 2.0.0.

Esto es especialmente útil para eventos que existían antes de 2.0 — verás solo las sesiones desde ese día.

---

## 4. Reportes recomendados

### Reporte 1: Engagement de pantallas

**Tipo:** Insights → Line chart

**Configuración:**
- Event: `screen_viewed`
- Breakdown: `screen`
- Metric: Total (o Unique users si prefieres usuarios únicos)
- Filter: `App Version String` → `is` → `2.0.0`

**Qué verás:** Cuántas veces se abre cada pantalla por día. Permite identificar la pantalla más y menos visitada.

---

### Reporte 2: Efectividad de notificaciones

**Tipo:** Insights → Bar chart

**Configuración:**
- Events: `notification_drink_tapped`, `notification_skip_tapped`, `notification_opened`
- Metric: Total
- Visualización: Stacked bar

**Qué verás:** La proporción de cada acción ante una notificación. Si `skip` es muy alto relativo a `drink`, los recordatorios son molestos o mal temporizados.

---

### Reporte 3: Uso de las acciones del History

**Tipo:** Insights → Bar chart

**Configuración:**
- Events: `history_edit_amount_tapped`, `history_edit_time_tapped`, `history_delete_tapped`, `history_filter_changed`
- Metric: Unique users
- Filter: `App Version String` → `is greater than or equal to` → `2.0.0`

**Qué verás:** Cuántos usuarios únicos usan cada acción. Si `edit_time` es casi cero, ese botón puede simplificarse o moverse a un lugar menos prominente.

---

### Reporte 4: Período favorito en Stats

**Tipo:** Insights → Pie chart

**Configuración:**
- Event: `stats_period_selected`
- Breakdown: `period`
- Metric: Total

**Qué verás:** Distribución entre `week`, `month` y `year`. El período más usado es en el que los usuarios confían más para revisar su progreso.

---

### Reporte 5: Goals más establecidos

**Tipo:** Insights → Bar chart con Breakdown

**Configuración:**
- Event: `settings_daily_goal_changed`
- Breakdown: `goal_ml`
- Metric: Total

**Qué verás:** Un histograma de los valores de goal más elegidos. Útil para definir los defaults y los rangos sugeridos en la UI.

---

### Reporte 6: Conversión onboarding → primera bebida

**Tipo:** Funnels

**Pasos:**
1. `onboarding_completed`
2. `drink_recorded`

**Ventana:** 1 día

**Qué verás:** Qué porcentaje de usuarios que terminan el onboarding registran su primera bebida el mismo día. Una caída grande aquí indica que el onboarding no está motivando la acción inmediata.

---

### Reporte 7: Retención por pantalla

**Tipo:** Retention → Recurring

**Configuración:**
- First event: `screen_viewed` (filter `screen` = `today`)
- Return event: `screen_viewed` (filter `screen` = `today`)

**Qué verás:** Cuántos usuarios vuelven a abrir la app día tras día. Es la métrica más directa de hábito.

---

### Reporte 8: Usuarios que desactivaron notificaciones

**Tipo:** Insights → Line

**Configuración:**
- Event: `settings_notifications_disabled`
- Metric: Unique users
- Granularidad: Weekly

**Qué verás:** Tendencia de usuarios que optan por no recibir recordatorios. Un pico después de un cambio en la frecuencia de notificaciones indica que fue demasiado.

---

## 5. Breakdowns útiles para cualquier reporte

Puedes agregar cualquiera de estos como **Breakdown** secundario en un reporte existente:

| Campo | Qué revela |
|-------|-----------|
| `App Version String` | Comparar comportamiento entre versiones |
| `$os_version` | Ver si algún comportamiento es específico de una versión de Android |
| `$manufacturer` | Fabricante del dispositivo |
| `$model` | Modelo exacto |

---

## 6. Datos que NO se trackean (intencional)

Los siguientes eventos **no existen** en el sistema actual para mantener el tracking enfocado:

- Apertura y cierre de bottom sheets individuales (excepto presets)
- Scroll en cualquier pantalla
- Tiempo en pantalla (excepto onboarding)
- Errores de red o crashes (eso va a Firebase Crashlytics)
- Eventos de la pantalla DEV (excluida del `screen_viewed`)
- Acciones del switch de tracking (el usuario puede optarse fuera en cualquier momento)

---

## 7. Buenas prácticas al agregar nuevos eventos

1. **Crea una interfaz nueva** por dominio (ej. `TrackingOnboarding`, `TrackingHistory`). No mezcles eventos de pantallas distintas en la misma interfaz.
2. **Extiende `TrackingService`** con la nueva interfaz.
3. **Implementa en `MixpanelTrackingService`** usando siempre `sendEvent()` para respetar el consent.
4. **Nunca llames `mixpanel.track()` directamente** fuera de `sendEvent()` — excepto para `tracking_opted_out`, que es el único evento que debe enviarse aunque el usuario haya revocado el consent.
5. **Documenta aquí** los eventos nuevos antes de hacer merge.

---

*Última actualización: versión 2.0.0*
