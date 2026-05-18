## Logica de las alarmas

Debido a restricciones de android, programar alarmas exactas sera decision del usuario.
Por lo tanto, tenemos que incluir un fallback para cuando este permiso no es otorgado.

Alarm types:
1. Exact
  Alarmas llegan a la hora que la app indica.

2. Inexact
  Alarmas llegaran a la hora que el sistema encuentre apropiado.


La aplicacion debe obtener este permiso desde el onboarding, explicando al usuario las diferencias
entre ambos tipos y permitiendo enviar al usuario a app settings para otorgar el permiso.
Si el usuario omite este paso, el tipo de alarma sera el predeterminado: Inexact.

Tambien se puede modificar este permiso en cualquier momento, por lo cual debemos estar a la 
escucha de cambios para reajustar alarmas existentes y tambien la UI de la app.

Dev menu es otro entry point para modificar el tipo de alarma, aunque solo disponible en debug builds.

-------------

# Cambios en la app
1. Componente Recordatorios en DrinkScreen. Mostrara solo el progreso de la hora de inicio, la hora
de fin y la hora cuando programamos la alarma inexacta. abajo deben decir "aproximadamente"
Ademas, el total de bebidas y el intervalo y la hora de la proxima alarma, deben mostrarse como 
tiempos aproximados.
2. Settings screen debe permitir cambiar el tipo de alarma. Mostraremos el valor actual, y al presionar
el row mostrar otro dialogo explicando como cambiar el tipo de alarma (similar a goal reached celebration
row). esta opcion envia al usuario a ajustes del telefono: alarmas y recordatorios.

# Cambios en logica
1. Si es alarma inexact, debemos registrar la hora en que la alarma llego, asi podemos mostrar
al usuario las fechas de cada alarma pasada en la UI del componente de recordatorios.
2. Si es exact, la logica actual esta bien.
3. si es inexact, debemos enviar la notificacion en el broadcast receiver, tambien programar la proxima
alarma. Si cambia el tipo de alarma, reprogramar alarma:
a) si es inexact, basado en la hora de la ultima notificacion recibida programar la siguiente alarma
b) si es exact, usar la logica actual para programar la alarma.

# IMPL
ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
recibe solo cuando tenemos el permiso, NO cuando se revoca.
-> recibido: check permission
tiene permiso: reajustar y programar alarmas exactas al momento.
no tiene permiso: revisar y ajustar alarma inexacta si es necesario. 

-> quitar permiso.
la app no recibe ningun broadcast cuando el permiso se quita.
TODAS las alarmas exactas existentes se borraran de immediato.

mostrar dialogo al entrar en la app que las alarmas se han restaurado nuevamente
(en caso de quitar el permiso y volverlo a dar, las alarmas ya estaran muertas sin poder reprogramarse
solo hasta abrir la app otra vez)

IMPORTANTE: antes de ir a app settings, avisar al usuario que las alarmas no se programaran otra vez si 
quita y pone el permiso otra vez.


# Requerimientos

- hora de inicio
- hora de fin

# Logica

- las alarmas estan activas durante la hora de inicio y la hora de fin.
- permitir pausar y reanudar las alarmas si el usuario lo desea.

Calculacion:
hora de inicio - hora de fin
tiempo entre horas: 13 horas
cantidad a beber: 2litros
dividir 2 litros entre 13 horas para determinar el intervalo de cada notificacion.

de la calculacion del dia, vamos adeterminar la cantidad a beber en cada intervalo

- en caso de saltar varias veces mantendremos la configuracion del dia para no forzar a beber de
  mas.

el usuario debe poder ajustar las notificaciones si desea alcanzar todavia el gol del dia.
puede recalcular y ajustar el resultado antes de establecerlo.
mostrar en alguna pantalla o menu (solo si notificaciones estan activadas)

gol: 2 litros (2000 ml)
6:00 hasta 19:00 (13horas) (780 mins) (46800 segundos)

// el intervalo es dinamico, el usuario puede dar un valor aprox.
intervalo de bebida = 40mins

2000ml/780mins

yo bebo 30ml, como beber 2 litros en 13 horas?

30ml+30ml+... = 2000ml

= 2000ml/30ml = 66,66666667 veces -- 67 veces

67 veces a beber en 13 horas

67 bebidas = 780mins
beber 67 veces en 780mins, a cuanto tiempo se logra esto?

tiempo total = 780 min (46800 segundos)
eventos = 67 veces

Entonces:
intervalo = tiempo total / cantidad de eventos

780min / 67 = 11,64179104 min
46800 segundos / 67 = 698,50746269 seconds (11,64179104 min)

rendondeado es 12
67x11,64179104 = 780mins
67vecesx12mins = 804mins (24mins de mas)

alarma cada 11,64179104 minutos (698,5074624 segundos)

graphic to display when notification will show up.

1 2 3 4 5 6
| | | | | |
| | | | | |
| | | | | |
6:00 - 7:00 - 8:00 - 9:00 - 10:00 - 11:00 - 12:00

