Development

Fixes
[x] stat graph, por debajo de la meta, cambiar a : cantidad alcanzada
[] grafico: calcular bien el porcentaje.
[] grafico: no mostrar primera semana antes del primer record de daily goal registrado.
[] goal overlay: numero de vasos no esta bien
[] goal overlay: traducir textos 
[] goal overlay: randomize with new texts
[x] settings: traducir texto de compartir con amigos
[x] settings: poner url correcta para calificar la app
[] settings: traducir texto para solicitar funcion
[] settings: traducir texto para reportar problema
[] consent: traducir textos de pantalas
[] consent: agregar privacy policy final de release
[] consent: agregar url a privacy policy
[] consent: cambiar orden de botones en track screen
[] settings: agregar row para cancelar el tracking en ajustes Privacy-> Usage data
[] settings: agregar row para ir al privacy policy y terms of use 
[] drink screen: al usar alarma inexacta: recordatorio mostrar hora aproximada! 
[] drink screen: al usar alarma inexacta: mostrar alarma aproximada en el componente
[] app nav: si presionamos atras desde otra screen, regresar a drink screen y luego salir de la app.
[] app: agregar icono monochrome
[] app: corregir splash screen
[] listing: preparar capturas
[] listing: preparar recursos de imagenes
[] listing: preparar description y title en diferentes idiomas
[] drink screen: logs: quitar guion en mensajes
[] traducciones: revisar todas estan bien





In-App final setup
Proximos pasos:

1. Crear en Google Play Console un producto Non-consumable con ID remove_ads_lifetime
2. En RevenueCat dashboard: crear entitlement ads_free, producto, y offering remove_ads (tipo
   Lifetime)
3. Reemplazar REVENUECAT_API_KEY_PLACEHOLDER en untranslated.xml con tu API key de RevenueCat

To fix (prio)
- revisar que se establezca bien el goal desde el onboarding. en una prueba con tamara puso 1500 y no se guardo en la app.
- explicar presets porque no se entiende bien
- highlight presets quicks.
limit amount of presets
hide vs preivous period if we dont have a previous period to compare.
error de calculo cuando ajustamos horas y el valor queda antes, la siguiente hora se programa hasta
el dia siguiente (ejemplo de 6 a 17 a las 10:3 recibi alarma y el valor no cambio, la siguiente alarma es a las 10:3 del dia siguiente.) revisar
calculo y si la alarma llega antes, de la hora no
usar hora actual, sino que calcule un poco mas a la siguiente hora.

To Complete

- [x] Adjust notification messages
- [x] onboarding
- [x] Adjust celebration
- [x] translations.
- [x] paid version
- [x] crashlytics
- [x] ads
- [x] analytics
- [x] setup listing workflow (to upload listing assets)
- [x] privacy policy and tracking request.
- [x] fix remove ads bottom sheet on rotation bug
- [x] adaptive layouts (v1)
- [x] new alarm design (exact and inexact)
- tiempo en drink water (countdown) ahora no se actualiza al cambiar de horas activas
- [-] fix alarm bug

Next release priorities

- Drink water
- Drink suggestions drag and drop.
- Stats: graph
- History: V1
- Goal reached one celebration.

Analytics

- Track feature usage for improvements
- Crashlytics

Out of scope

- Stats: streaks
- History: V2
- Settings: User profile
- Settings: Change units
- Goal reached more celebrations.

Priorities

- recalculate when drinking water. notification alarm should be rescheduled.
- Store drink amount and provide it to all drink calculations.
- organize drink alarm package and all shared code.

Next steps
[] fix admob
[] request notification permissions to users
[] fix alarm and notifications to arrive in the intervals
[] test connectivity
[] fix insets
[] fix splash screen (losing aspect ratio)
[] migrate to compose

Fixes

- despues de tomar agua y una alarma existe, hay que cambiar la hora de la proxima alarma, no quiero
  beber 10 segundos antes y
- aun recibir la notification.
- use Instant instead of LocalDateTime (good for timezone conversion) for dates, then parse to
  localdate time if needed.
- goal reached widget deberia estar centrado cuando aparezca y mas enfatizado, para que se vea bien
  con el confetti.
- detectar cambios de hora en el telefono (receiver) y ajustarse para reprogramar alarmas.
- Insets en la politica de privacidad inicial. Sobrepasan la barra de estatus y navegacion.
- Insets en el onboarding tambien falla
- notificacion en el onboarding esta obsoleto, actualizar estilo para android (y iOS if necessary)
- despues del onboarding pedimos permiso para notificaciones sin decir nada... ajustar esto.
- quitar las novedades al abrir la app. revisar que pagina de firebase esta controlando esto.
- al abrir la app nos reenvia a ajustes para mostrar la app sobre otras apps. mostrar algo antes de
  redireccionar al usuario.

Features

- los mensajes de cuanto me falta para alcanzar la meta, deberian ser motivadores cuando hay chances
  de lograrlo, pero si ya estamos cerca
- de la hora de finalizar horas activas, los mensajes deberian ser mas neutrales, no forzar al
  usuario a beber agua solo para alcanzar la meta, puede
- que solo beber agua sea suficiente (en caso que no estemos ni a la mitad del progreso.)
- calendario, mostrar un circulo sobre los dias que bebi agua, pero el circulo sobre las fechas debe
  estar llenado de igual forma que
- la cantidad de agua de ese dia. si complete mi gol un dia, ese dia aparece con circulo azul
  completo indicando que se llego a la meta. puede
- ser un progress bar sobre el circulo tambien si es que queda mejor.
- agregar UNDO al beber agua, en caso de haber fallado algo, revertir el error.
- grafico de consumo de agua al dia, semana, mes y año.
- guardar datos en google drive o cualquier otro cloud service.
- sistema que soporte multiples unidades de medidas, luego se debe poder agregar si usuarios lo
  piden.
- en el history, filtrar fecha debe mostrar solo desde la fecha de inicio, desactivar busqueda de
  fechas antes de la fecha de inicio
- tambien marcar con circulo los dias que hay regisstro de consumo de agua para encotrar rapido dias
  con registros.

Projects:
we need to keep some stats, each day the user has a daily goal, but this goal can be changed any
day, after active hours (endtime) is considered that the next starttime will be a new day. track the
goal of
each day and the amount of drink of that day, to display those values in the daily intake graph in
the stats screen.   