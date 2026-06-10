# The fountain of wishes

- migrate from koin to metro for dependency injection
- schedule future schedule for alarms. example: from a future day in a calendar select a day to
  configure the alarm for that specific day. This overrides any recurrent alarm configuration for
  that particular day. Once the day is over OR the last alarm of that day is completed this
  configuration is disposed. Use case: Im on holiday next week and I dont want reminders those days,
  OR i just want a midday reminder during my holidays.
- Fix: Change goal is not updating the stats graph immediately
- Goal reached: Polish existing celebrations
- Goal reached: add more celebrations
- En el gráfico siempre sale menos va periodo anterior si seguimos bebiendo. Lo mejor sería mostrar
  algo más positivo. O cuando hicimos el mismo día la semana pasada. Esto para V2
- Eliminar datos botón: mostrar acción disponible en 5 (counter) para no asustar a la gente que
  se borran cuando llegue a zero
- al aceptar una nueva politica de privacidad, y si ya esta aprovado el tracking, no mostrar esta
  pantalla. actualmente aparece rapidamente antes de quitarse cuando se aceptan nuevas politicas de
  privacidad en la app
- Grafico no cambia letras del dia abreviadas al nuevo idioma al cambiar
- Gráfico de goles y gráfico de ingesta. Uno muestra si alcanzaste el gol y otro es de cantidad hay
  q agregar ambos. Con toggle al gráfico.
- Fix: graph in stats screen is not updating in realtime (example: when injecting new data in dev
  menu). Verify is updating when drinking and not only when reopening the app.
- Fix: The history screen is not updating the current day when app is always open (kept in recent
  apps stack) and we phone system changes to the next day. Today button targets the previous day in
  that case, not the current day.
- Obtener los skips para sugerir reducir notificaciones ai son demasiado. Si el usuario borra mucho
  la notificacion se puede tratar de reducir el intervalo para evitar molestias. Se sugiere en la
  pantalla de configuracion de notificaciones/reminders
- Verify monochrome icon is displayed. Also verify for some API levels why the app is not changing
  like other apps when a theme is applied (check on pixel 3 test device where is issue happens)
- fix insets in foldables and a black area is displayed. (research the phone model that has this
  issue, check the documentation for hints)
- use Instant instead of LocalDateTime (good for timezone conversion) for dates, then parse to
  localdate time if needed.
- notification messages. do not use goodmorning if the start time is not in the morning. start time
  can be at 6pm evening.
- start and end time of active hours require at least the interval minimum to be set. otherwise we
  have issues if the interval is just one minute for example.
- after triggering last alarm remdinder component take another minute before updating its ui state.
  also last alarm in the component should say: last for the next time of the alarm.
- test feature and problem emails are arriving to the correct tags in the support email inbox.
- recalculate when drinking water. notification alarm should be rescheduled. despues de tomar agua y
  una alarma existe, hay que cambiar la hora de la proxima alarma, no quiero beber 10 segundos antes
  y aun recibir la notification.
- calendario, mostrar un circulo sobre los dias que bebi agua, pero el circulo sobre las fechas
  debe estar llenado de igual forma que la cantidad de agua de ese dia. si complete mi gol un dia,
  ese dia aparece con circulo azul completo indicando que se llego a la meta. puede ser un progress
  bar sobre el circulo tambien si es que queda mejor.
- guardar datos en google drive o cualquier otro cloud service.
- en el history, filtrar fecha debe mostrar solo desde la fecha de inicio, desactivar busqueda de
  fechas antes de la fecha de inicio
- tambien marcar con circulo los dias que hay regisstro de consumo de agua para encotrar rapido dias
  con registros.
- agregar iconos a los presets o imagen que represente un vaso y el total de agua dentro de el.
  talvez un creador de icono para tu preset donde puedes elegir la forma y hasta donde esta lleno de
  agua.
- mostrar en cada rango del history el tiempo que ha pasado desde el ultimo registro. por ejemplo
  ahora tomo agua y aparece cuandot hay desde este registro
  al anterior. tener algun threshold para mostrar el tiempo, por ejemplo, si es mayor a 30mins,
  mostrarlo. si es menor talvez no para no confundir mucho
  visualmente. o solo mostrarlo al ultimo trago con el penuntino y no a todos los registros. (esta
  opcion suena mejor para la primera implementation.)
- Generar cantidad recomendada para notificación basado en los registros de bebida. El valor que
  más se repita se muestra como sugerencia.
- Considerar la cantidad de agua para textos de los mensajes en las notificaciones:
  Si no tiene registros: mañana puede ser el dia. Todavia puedes registrar algo hoy. Con las manos
  vacias. Supongo que se acabo. Me voy en blanco.
  Si bebio poco: con pequeños pasos se inicia. De a poco se empieza.
  Supero el gol: lograste tu meta bien hecho. Aun puedes superar mas tu meta. Estas por encima de tu
  meta. Llegaste encima de la meta. Hoy estas que ardes con tus registros. Hoy impresionaste con tu
  persistencia. Buen habito.
  Racha de dias: dos dias seguidos sigue asi. La racha continua un dia mas.
  Racha alta: ya lo tienes controlado. Estas en las grandes ligas. Nuevo dia sumado a tu racha. Etc
  etc.
- los mensajes de cuanto me falta para alcanzar la meta, deberian ser motivadores cuando hay
  chances
  de lograrlo, pero si ya estamos cerca de la hora de finalizar horas activas, los mensajes deberian
  ser mas neutrales, no forzar al
  usuario a beber agua solo para alcanzar la meta, puede que solo beber agua sea suficiente (en caso
  que no estemos ni a la mitad del progreso.)
- agregar descripcion corta a los presets.
- Obtener los skips para sugerir reducir notificaciones ai son demasiado. Si el usuario borra
  mucho la notificacion se puede tratar de reducir el intervalo para evitar molestias. Se sugiere en
  la pantalla de configuracion de notificaciones/reminders
- [] fix stats graphic: monthly total is counting all month. When starting using the app we should
  only count since the first day of usage, and calculate the correct total drink amount of the month
  like that, for following months we can take all days. Its just a fix for initial days.
- [] allow to disable drink water animation on settings
- [] research: dependency injection for kmp using metro (no runtime errors)
- [] research: integrate SQLDelight. find benefits. static checks.
- [] fix graphic image in
  listing: https://play.google.com/store/apps/developer?id=eahm&hl=es_NI&gl=US
- [] stats screen: do not display when percentaje is lower than previous period, it always is lower
  at beginnings of week/year show something more positive instead. show percentage only on last day
  it can be negative or positive, for negative show a positive text.
- [] check if interval bottom sheet still display text in english for all languages
- [] delete all data button: update text to explain that the counter will enable the action to
  delete to the user, not that it will delete when reaching zero. dont scare users.
- [] research: integration with MCP. how this can be usefull to our app and our services.
- [] fix: change goal does not update graphic goal value realtime.

## Projects

### Appstore

- Publish on app store: complete all store requirements to be legal compliant
- Setup CD to deploy to appstore
- Setup CD to deploy listing to appstore
- setup scripts to locally start workflows to appstore
- update code based on platform specifics, example: share (to appstore url for ios), rate (to app
  store url for ios), etc.
-

### Restore purchases

restoring purchases is still not tested in production. if a user pays to remove ads, reinstall the
app and tries to restore the purchase within the same device, we have no idea if this could work.

- check existing plan (in docs/plans) that restores a purchase. the plan was created but not
  implemented yet.
- ideally restoring can only happen within the same device and the same account. Read revenuecat
  documentation to know more about this. if user has the app in android and iOS a payment per device
  is required. after paying andriod and ios apps, and using same account, restoration will be
  possible. (new devices require a new purchase)
- check the autobackup case documented in revenuecat. if the app is reinstalled in the same device,
  is it possible to restore the purchase by using only the app autobackup? clarify this.

### Streak system

- display max streak in stats screen
- precalculate streak based on local data for legacy users. we should be able to determine this
  with the records' database. Streak system should treat users with long streaks accordingly to what
  is decided in the system.
- add dialog to explain how streak works ONLY when completing and starting a second day in a
  row. for users with existing streak (due to the introduction of the feature) display the same
  dialog only once, then apply initial logic.
- use overlay to congratulate users if start a new day streak from the notification.
- in the app, by drinking, congratulate users if start a new day streak.

### Reward system

- add a list of achievements in the app
- add rewards by completing achievements
- notify in the app when an achievement is completed
- notify with overlay (if enabled) when an achievement is completed and the app is closed (
  brainstorm further this idea)

### History V2

- add carousel of dates to select between the closest dates.
- add calendar picker to easily pick a very far previous date.

### User profile

- collect user weight and user height in the onboarding to calculate a drink goal amount
  automatically. (include disclaimer, app is not a professional to determine this, still use with
  caution)
- collect user weight and user height in the settings to calculate a drink goal amount
  automatically. (include disclaimer, app is not a professional to determine this, still use with
  caution)
- display username in main screen and settings screen.
- allow to change username in settings.
- cover case when user does not provide a username

### Accessibility support

- agregar accesibilidad: add content descriptions (for talkback) through the whole app (require
  translations for existing languages)
- agregar accesibilidad: verify ui navigation with external devices through the whole app (includes
  defining our own actions to facility the usage of complex ui elements)
- agregar accesibilidad: font size and big screen size adjustments through the whole app

## Manual

1. When completing one task from this document, remove the item from the list and (if possible) add
   that change to the commit that contains the implementation of the previously removed item.
