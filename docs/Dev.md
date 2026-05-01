Development

Next steps
[] fix admob
[] request notification permissions to users
[] fix alarm and notifications to arrive in the intervals
[] test connectivity
[] fix insets
[] fix splash screen (losing aspect ratio)
[] migrate to compose

Fixes
- use Instant instead of LocalDateTime (good for timezone conversion) for dates, then parse to localdate time if needed.
- goal reached widget deberia estar centrado cuando aparezca y mas enfatizado, para que se vea bien con el confetti.
- detectar cambios de hora en el telefono (receiver) y ajustarse para reprogramar alarmas.
- Insets en la politica de privacidad inicial. Sobrepasan la barra de estatus y navegacion.
- Insets en el onboarding tambien falla
- notificacion en el onboarding esta obsoleto, actualizar estilo para android (y iOS if necessary)
- despues del onboarding pedimos permiso para notificaciones sin decir nada... ajustar esto.
- quitar las novedades al abrir la app. revisar que pagina de firebase esta controlando esto.
- al abrir la app nos reenvia a ajustes para mostrar la app sobre otras apps. mostrar algo antes de
  redireccionar al usuario.

Features
- los mensajes de cuanto me falta para alcanzar la meta, deberian ser motivadores cuando hay chances de lograrlo, pero si ya estamos cerca
- de la hora de finalizar horas activas, los mensajes deberian ser mas neutrales, no forzar al usuario a beber agua solo para alcanzar la meta, puede
- que solo beber agua sea suficiente (en caso que no estemos ni a la mitad del progreso.)
- calendario, mostrar un circulo sobre los dias que bebi agua, pero el circulo sobre las fechas debe estar llenado de igual forma que
- la cantidad de agua de ese dia. si complete mi gol un dia, ese dia aparece con circulo azul completo indicando que se llego a la meta. puede 
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
we need to keep some stats, each day the user has a daily goal, but this goal can be changed any day, after active hours (endtime) is considered that the next starttime will be a new day. track the goal of
each day and the amount of drink of that day, to display those values in the daily intake graph in the stats screen.   