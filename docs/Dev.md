Development
ideas:
mostrar en cada rango del history el tiempo que ha pasado desde el ultimo registro. por ejemplo
ahora tomo agua y aparece cuandot hay desde este registro
al anterior. tener algun threshold para mostrar el tiempo, por ejemplo, si es mayor a 30mins,
mostrarlo. si es menor talvez no para no confundir mucho
visualmente. o solo mostrarlo al ultimo trago con el penuntino y no a todos los registros. (esta
opcion suena mejor para la primera implementation.)

Claude fixes
[x] grafico: calcular bien el porcentaje tomando todos los dias de la semana anterior (incluso si
emprezamos a usar la app un miercoles, considerar desde lunes).
[x] grafico: no mostrar primera semana antes del primer record de daily goal registrado (primer dia
de la app) en ese caso mostrar un texto vacio.
[x] goal overlay: numero de vasos no esta bien, debe coincidir con la cantidad de agua registrada en
el dia hasta el momento
[x] goal overlay: traducir textos en y es.
[x] goal overlay: randomize with new texts, crear 3 variantes de textos SOLO para ocean style.
[x] consent: agregar url a privacy
policy: https://www.freeprivacypolicy.com/privacy/view/04c015dc119a00b1ec8a21df46a9bfb1
[x] settings: agregar row para cancelar el tracking en ajustes Privacy-> Share usage data (tambien
actualizar consent screen text para ir a este menu)
[x] settings: agregar row para ir al privacy policy y terms of use. agrupado en el mismo grupo de
privacy and legal
[x] drink screen: al usar alarma inexacta: recordatorio mostrar texto que dice: proxima aprox.
[x] drink screen: al usar alarma inexacta: mostrar titulo: recordatorios aproximados. y decir "
aprox. cada" para el intervalo.
[] app: agregar icono monochrome
[] listing: traducir listing en diferentes idiomas (description, title, app name, etc)
[x] drink screen: logs: quitar guion en mensaje de no hay registros.
[x] traducciones: revisar todas estan bien y eliminar warnings.

Fixes
[x] stat graph, por debajo de la meta, cambiar a : cantidad alcanzada
[x] grafico: calcular bien el porcentaje.
[x] grafico: no mostrar primera semana antes del primer record de daily goal registrado.
[x] goal overlay: numero de vasos no esta bien
[x] goal overlay: traducir textos
[x] goal overlay: randomize with new texts
[x] settings: traducir texto de compartir con amigos
[x] settings: poner url correcta para calificar la app
[x] settings: traducir texto para solicitar funcion
[x] settings: traducir texto para reportar problema
[x] consent: traducir textos de pantalas
[x] consent: agregar privacy policy final de release
[x] consent: agregar url a privacy policy
[x] consent: cambiar orden de botones en track screen
[x] settings: agregar row para cancelar el tracking en ajustes Privacy-> Usage data
[x] settings: agregar row para ir al privacy policy y terms of use
[x] drink screen: al usar alarma inexacta: recordatorio mostrar hora aproximada!
[x] drink screen: al usar alarma inexacta: mostrar alarma aproximada en el componente
[] app nav: si presionamos atras desde otra screen, regresar a drink screen y luego salir de la app.
[] app: agregar icono monochrome
[] app: corregir splash screen
[] listing: preparar capturas
[] listing: preparar recursos de imagenes
[] listing: preparar description y title en diferentes idiomas
[x] drink screen: logs: quitar guion en mensajes
[x] traducciones: revisar todas estan bien

In-App final setup
Proximos pasos:

1. Crear en Google Play Console un producto Non-consumable con ID remove_ads_lifetime
2. En RevenueCat dashboard: crear entitlement ads_free, producto, y offering remove_ads (tipo
   Lifetime)
3. Reemplazar REVENUECAT_API_KEY_PLACEHOLDER en untranslated.xml con tu API key de RevenueCat

To fix (prio)

- [x] revisar que se establezca bien el goal desde el onboarding. en una prueba con tamara puso 1500
  y no se guardo en la app.
- [x] explicar presets porque no se entiende bien
- [x] highlight presets quicks.
- [x] limit amount of presets
- [x] hide vs preivous period if we dont have a previous period to compare.
- [x] error de calculo cuando ajustamos horas y el valor queda antes, la siguiente hora se programa
  hasta
  el dia siguiente (ejemplo de 6 a 17 a las 10:3 recibi alarma y el valor no cambio, la siguiente
  alarma es a las 10:3 del dia siguiente.) revisar
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
- [x] fix alarm bug

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
[x] fix admob
[x] request notification permissions to users
[x] fix alarm and notifications to arrive in the intervals
[cancelled] test connectivity
[] fix insets
[] fix splash screen (losing aspect ratio)
[x] migrate to compose

Fixes

- [] despues de tomar agua y una alarma existe, hay que cambiar la hora de la proxima alarma, no
  quiero
  beber 10 segundos antes y aun recibir la notification.
- [] use Instant instead of LocalDateTime (good for timezone conversion) for dates, then parse to
  localdate time if needed.
- [x] goal reached widget deberia estar centrado cuando aparezca y mas enfatizado, para que se vea
  bien
  con el confetti.
- [x] detectar cambios de hora en el telefono (receiver) y ajustarse para reprogramar alarmas.
- Insets en la politica de privacidad inicial. Sobrepasan la barra de estatus y navegacion.
- Insets en el onboarding tambien falla
- [x] notificacion en el onboarding esta obsoleto, actualizar estilo para android (y iOS if
  necessary)
- [x] despues del onboarding pedimos permiso para notificaciones sin decir nada... ajustar esto.
- [x] quitar las novedades al abrir la app. revisar que pagina de firebase esta controlando esto.
- [x] al abrir la app nos reenvia a ajustes para mostrar la app sobre otras apps. mostrar algo antes
  de
  redireccionar al usuario.

Features

- [] los mensajes de cuanto me falta para alcanzar la meta, deberian ser motivadores cuando hay
  chances
  de lograrlo, pero si ya estamos cerca de la hora de finalizar horas activas, los mensajes deberian
  ser mas neutrales, no forzar al
  usuario a beber agua solo para alcanzar la meta, puede que solo beber agua sea suficiente (en caso
  que no estemos ni a la mitad del progreso.)
- [] calendario, mostrar un circulo sobre los dias que bebi agua, pero el circulo sobre las fechas
  debe estar llenado de igual forma que la cantidad de agua de ese dia. si complete mi gol un dia,
  ese dia aparece con circulo azul completo indicando que se llego a la meta. puede ser un progress
  bar sobre el circulo tambien si es que queda mejor.
- [x] agregar UNDO al beber agua, en caso de haber fallado algo, revertir el error. (dialogo
  explicando como borrarlo)
- [x] grafico de consumo de agua al dia, semana, mes y año.
- [] guardar datos en google drive o cualquier otro cloud service.
- [x] sistema que soporte multiples unidades de medidas, luego se debe poder agregar si usuarios lo
  piden.
- [] en el history, filtrar fecha debe mostrar solo desde la fecha de inicio, desactivar busqueda de
  fechas antes de la fecha de inicio
- [] tambien marcar con circulo los dias que hay regisstro de consumo de agua para encotrar rapido
  dias
  con registros.

Projects:

- [x] we need to keep some stats, each day the user has a daily goal, but this goal can be changed
  any day, after active hours (endtime) is considered that the next starttime will be a new day.
  track the goal of each day and the amount of drink of that day, to display those values in the
  daily intake graph in the stats screen.   
