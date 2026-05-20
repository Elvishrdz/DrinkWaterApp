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

Ideas y tareas

- [] app nav: si presionamos atras desde otra screen, regresar a drink screen y luego salir de la
  app.
- [] app: agregar icono monochrome
- [] app: corregir splash screen
- [] listing: preparar capturas
- [] listing: preparar recursos de imagenes
- [] listing: preparar description y title en diferentes idiomas
- [] fix insets
- [] fix splash screen (losing aspect ratio)
- [] add more banners in app screens (adaptive banners)
- [] despues de tomar agua y una alarma existe, hay que cambiar la hora de la proxima alarma, no
  quiero
  beber 10 segundos antes y aun recibir la notification.
- [] use Instant instead of LocalDateTime (good for timezone conversion) for dates, then parse to
  localdate time if needed.
- [] recalculate when drinking water. notification alarm should be rescheduled.
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
- [] guardar datos en google drive o cualquier otro cloud service.
- [] en el history, filtrar fecha debe mostrar solo desde la fecha de inicio, desactivar busqueda de
  fechas antes de la fecha de inicio
- [] tambien marcar con circulo los dias que hay regisstro de consumo de agua para encotrar rapido
  dias
  con registros.
- [] agregar iconos a los presets o imagen que represente un vaso y el total de agua dentro de el.
  talvez un creador de icono para tu preset donde puedes elegir la forma y hasta donde esta lleno de
  agua.
- [] agregar descripcion corta a los presets.
- [] mostrar en cada rango del history el tiempo que ha pasado desde el ultimo registro. por ejemplo
  ahora tomo agua y aparece cuandot hay desde este registro
  al anterior. tener algun threshold para mostrar el tiempo, por ejemplo, si es mayor a 30mins,
  mostrarlo. si es menor talvez no para no confundir mucho
  visualmente. o solo mostrarlo al ultimo trago con el penuntino y no a todos los registros. (esta
  opcion suena mejor para la primera implementation.)
