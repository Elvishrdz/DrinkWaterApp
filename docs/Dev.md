Development

Next release priorities

- [x] Drink water
- [x] Drink suggestions drag and drop.
- [x] Stats: graph
- [x] History: V1
- [x] Goal reached one celebration.

Analytics

- [20%] Track feature usage for improvements
- [x] Crashlytics

Out of scope

- Stats: streaks
- History: V2
- Settings: User profile
- Goal reached more celebrations.

App Release

- [] listing: preparar capturas
- [] listing: preparar recursos de imagenes
- [] listing: preparar description y title en diferentes idiomas

Priorities Tier 1

- [x] Tablet: onboarding muestra rounded corners y white background en darknmode. Ver captura
- [] Grafico no cambia letras del dia abreviadas al nuevo idioma al cambiar
- [] Gráfico de goles y gráfico de ingesta. Uno muestra si alcanzaste el gol y otro es de cantidad
  hay q agregar ambos.
  Con toggle al gráfico.
- [] Las estadísticas no se actualizan real time. Si la app está abierta nunca se actualiza solo an
  cerra y abrir otra vez se verán los cambios en el gráfico
- [] El historia no se actualiza al pasar de un día a otro
- [] El botón de hoy se queda en el día anterior pero la fecha ya cambió. Revisar y corregir esto.
- [] Obtener los skips para sugerir reducir notificaciones ai son demasiado. Si el usuario borra
  mucho la notificacion se puede tratar de reducir el intervalo para evitar molestias. Se sugiere en
  la pantalla de configuracion de notificaciones/reminders
- [x] app nav: si presionamos atras desde otra screen, regresar a drink screen y luego salir de la
  app.
- [] app: agregar icono monochrome
- [] fix insets
- [] fix splash screen (losing aspect ratio) app: corregir splash screen
- [x] add more banners in app screens (adaptive banners)
- [x] Usar otro banner id para banners en history y stats
- [x] Expandir width de banners
- [x] Editar un registro de días solo al ver un interstitial. Pero los últimos 2 registros no
  muestran video y se pueden editar normal siempre
- [x] Mostrar animación beber agua al usar presenta también input y preset
- [x] Actualizar componente reminders en drink screen. Fuera de horas activas solo mostrar el tiempo
  de la proxima alarma y el estado. no se necesita el grafico ni intervalo, ni vasos.
- [x] Muestra fecha del BEST record personal que diga hoy si es hoy o la fecha del día q fue. Hace 3
  días hace 1 mes. Más de un mes no se muestra
- [x] Revisar traduccion en ingles que no existan guiones de IA mire unos en notificaciones en
  ingles.
- [] Agregar aleman a idiomas
- [] Probar restaurar compra. Que pasa si un usuario compra desinstala e instala otra vez? Se
  restaura con la cuenta de Google o con el id único generado? Probar y agregar comentario.
- [x] ocultar countdown cuando notificaciones estan desactivadas
- [x] fix shimmer animation in ring progress. if ring pressed the animation is not inside the
  circle.
  instead keeps the previous path outside the shape.
- [] use Instant instead of LocalDateTime (good for timezone conversion) for dates, then parse to
  localdate time if needed.
- [] notification messages. do not use goodmorning if the start time is not in the morning. start
  time can be at 6pm evening.
- [] start and end time of active hours require at least the interval minimum to be set. otherwise
  we have issues if the interval is just one minute for example.
- after triggering last alarm remdinder component take another minute before updating its ui state.
  also last alarm in the component should say: last for the next time of the alarm.
- [x] Agregar codigo para los correo de drinkwater app y poder filtralos en gmail.
  codigo problemas: PR100:
  codigo features: FR200:
- [] test feature and problem emails are arriving to the correct tags in the support email inbox.

Priorities Tier 2

- [] recalculate when drinking water. notification alarm should be rescheduled. despues de tomar
  agua y una alarma existe, hay que cambiar la hora de la proxima alarma, no quiero beber 10
  segundos antes y aun recibir la notification.
- [] calendario, mostrar un circulo sobre los dias que bebi agua, pero el circulo sobre las fechas
  debe estar llenado de igual forma que la cantidad de agua de ese dia. si complete mi gol un dia,
  ese dia aparece con circulo azul completo indicando que se llego a la meta. puede ser un progress
  bar sobre el circulo tambien si es que queda mejor.
- [] guardar datos en google drive o cualquier otro cloud service.
- [] en el history, filtrar fecha debe mostrar solo desde la fecha de inicio, desactivar busqueda de
  fechas antes de la fecha de inicio
- [] tambien marcar con circulo los dias que hay regisstro de consumo de agua para encotrar rapido
  dias con registros.
- [] agregar iconos a los presets o imagen que represente un vaso y el total de agua dentro de el.
  talvez un creador de icono para tu preset donde puedes elegir la forma y hasta donde esta lleno de
  agua.
- [] mostrar en cada rango del history el tiempo que ha pasado desde el ultimo registro. por ejemplo
  ahora tomo agua y aparece cuandot hay desde este registro
  al anterior. tener algun threshold para mostrar el tiempo, por ejemplo, si es mayor a 30mins,
  mostrarlo. si es menor talvez no para no confundir mucho
  visualmente. o solo mostrarlo al ultimo trago con el penuntino y no a todos los registros. (esta
  opcion suena mejor para la primera implementation.)
- [] Generar cantidad recomendada para notificación basado en los registros de bebida. El valor que
  más se repita se muestra como sugerencia.
- [] Considerar la cantidad de agua para textos de los mensajes en las notificaciones:
  Si no tiene registros: mañana puede ser el dia. Todavia puedes registrar algo hoy. Con las manos
  vacias. Supongo que se acabo. Me voy en blanco.
  Si bebio poco: con pequeños pasos se inicia. De a poco se empieza.
  Supero el gol: lograste tu meta bien hecho. Aun puedes superar mas tu meta. Estas por encima de tu
  meta. Llegaste encima de la meta. Hoy estas que ardes con tus registros. Hoy impresionaste con tu
  persistencia. Buen habito.
  Racha de dias: dos dias seguidos sigue asi. La racha continua un dia mas.
  Racha alta: ya lo tienes controlado. Estas en las grandes ligas. Nuevo dia sumado a tu racha. Etc
  etc.
- [] los mensajes de cuanto me falta para alcanzar la meta, deberian ser motivadores cuando hay
  chances
  de lograrlo, pero si ya estamos cerca de la hora de finalizar horas activas, los mensajes deberian
  ser mas neutrales, no forzar al
  usuario a beber agua solo para alcanzar la meta, puede que solo beber agua sea suficiente (en caso
  que no estemos ni a la mitad del progreso.)
- [] agregar descripcion corta a los presets.
- [] Obtener los skips para sugerir reducir notificaciones ai son demasiado. Si el usuario borra
  mucho la notificacion se puede tratar de reducir el intervalo para evitar molestias. Se sugiere en
  la pantalla de configuracion de notificaciones/reminders

Plan para restaurar compras:

- [] existe un plan todavia no ejecutado en docs/plans

- vamos a dejar la opcion de solo poder restaurar en el mismo dispositivo si se guardan los datos
  segun la documentacion de revenuecat.
- para nuevos dispositivos se require una nueva compra

1. autobackup-> app es desinstalada y reinstalada en el mismo dispositivo. restauracion ocurre
   porque se guardo el id de revenuecat en el backup y puede usarse para restaurar la compra
   automaticamente

MORE STEPS

- [] agregar accesibilidad: content description / talkback
- [] agregar accesibilidad: ui navigation
- [] agregar accesibilidad: font size / screen size checks (revisar mas, la app ya esta adaptada
  para distintas pantallas y configuraciones)