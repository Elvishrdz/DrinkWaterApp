# Política de Privacidad

## 1. Introducción

Drink Water App es una aplicación de seguimiento de hidratación desarrollada y operada como un
proyecto independiente. Esta Política de Privacidad explica qué información se recopila cuando usas
la
App, cómo se utiliza y qué controles tienes sobre ella.

Al instalar o usar Drink Water App, aceptas las prácticas descritas en esta política. Si no estás de
acuerdo, por favor desinstala la App.

## 2. Información que recopilamos

### 2.1 Información que proporcionas (almacenada localmente en tu dispositivo)

Durante la configuración inicial y el uso normal, la App te solicita:

- **Aceptación de la política de privacidad** — reconocer legalmente cómo la App utiliza la
  información que proporcionas
- **Aceptación de los términos de uso** — aceptar las pautas y reglas de uso de la App
- **Consentimiento de seguimiento de uso** — elegir si enviar datos de uso de funciones para
  análisis y mejorar la experiencia del usuario
- **Preferencia de unidades (ml / oz / tazas)** — mostrar las medidas en el formato de tu
  preferencia
- **Meta diaria de agua** — establecer la cantidad de agua que deseas beber cada día
- **Hora de despertar / dormir** — programar recordatorios dentro de las horas activas
- **Frecuencia de recordatorios** — controlar con qué frecuencia se envían las notificaciones
- **Cantidad de adición rápida en notificaciones** — registrar una cantidad predefinida de agua
  consumida directamente desde las notificaciones

**Todo lo anterior se almacena en tu dispositivo** usando el almacenamiento local del dispositivo.
No
operamos servidores que lo almacenen. Nunca se transmite a nosotros ni a terceros, salvo la parte
que
elijas respaldar si activas la función opcional de Copia de seguridad en la nube (ver 2.7).

### 2.2 Datos de uso de la App (almacenados localmente en tu dispositivo)

La App registra los siguientes datos durante su uso:

- **Registros de ingesta de agua:** cantidad, marca de tiempo y tipo de entrada (manual, desde
  notificación o saltado).
- **Metas diarias de hidratación:** cantidades objetivo y fechas de vigencia.
- **Sugerencias de bebida:** cantidades personalizadas de adición rápida que creas.

Estos datos se almacenan localmente en tu dispositivo, salvo que actives la función opcional de
Copia
de seguridad en la nube (ver 2.7).

### 2.3 Análisis de uso anónimo (opcional, con opción de desactivación)

Con tu consentimiento, la App envía eventos de uso anónimos a **Mixpanel** para ayudarnos a entender
qué funciones se usan y mejorar la App. Específicamente:

- App abierta / configuración inicial iniciada, completada o abandonada
- Bebida registrada y fuente de la acción de "beber" (p. ej.: notificación o app)
- Preset personalizado creado
- Hoja de adición rápida de sugerencias de bebida abierta
- Pestaña de Recordatorios abierta y cambios en los días activos de recordatorios (qué días de la
  semana están activados/desactivados y cuántos)
- Acciones de copia de seguridad en la nube: conectar o desconectar la copia, tocar sincronización
  manual o restaurar, cambiar la programación automática, activar/desactivar la sincronización al
  guardar, y cuándo finaliza una sincronización (con su resultado). Estos eventos incluyen un conteo
  anónimo de cuántos de tus propios dispositivos usan la copia de seguridad, nunca el contenido del
  archivo ni identidad de cuenta alguna.

**Ningún dato personal, contenido de registros de agua o información identificable se incluye en
estos
eventos.** A cada dispositivo se le asigna un ID anónimo aleatorio que no está vinculado a tu
nombre,
correo ni otra identidad. La App no recopila tu nombre, dirección de correo electrónico ni ninguna
otra información de identidad de ninguna fuente.

- Los datos de Mixpanel se procesan en servidores en la **Unión Europea**.
- Puedes desactivarlo en cualquier momento desde **Ajustes → Privacidad y legal → Seguimiento de uso
  **.
- Una vez desactivado, no se envían más eventos y el ID anónimo se restablece.

### 2.4 Reportes de fallos

La App usa **Firebase Crashlytics** (Google) para recopilar automáticamente reportes de fallos con
modelo del dispositivo, versión de Android, versión de la App, hora del fallo y trazas de error. No
se incluyen datos personales ni registros de agua. Se usan únicamente para identificar y corregir
errores.

### 2.5 Datos de publicidad

La versión gratuita usa **Google AdMob**, que puede recopilar identificadores de publicidad del
dispositivo para mostrar anuncios relevantes. Puedes restablecer o limitar tu ID de publicidad en la
configuración de Google de tu dispositivo.

Comprar **Remove Ads** desactiva permanentemente todos los anuncios y el seguimiento asociado. Nos
aseguramos de que no se realicen más conexiones a servicios de publicidad una vez completada la
compra sin anuncios. Si notas alguna actividad relacionada con anuncios después de comprar, por
favor
reporta este comportamiento inusual para que podamos tomar medidas inmediatas.

### 2.6 Información de compras

Las compras dentro de la App se procesan a través de **RevenueCat**, que recibe tokens de compra de
Google Play (emitidos por Google, no por nosotros) y tu estado de derecho (si los anuncios están
desactivados). RevenueCat no recibe ninguna otra información confidencial. Si notas alguna actividad
inusual, te invitamos a reportarla para que podamos tomar las medidas apropiadas. Consulta la
Política
de Privacidad de RevenueCat para más detalles.

### 2.7 Copia de seguridad y sincronización en la nube (opcional, desactivada por defecto — Google Drive)

La App ofrece una copia de seguridad en la nube **opcional** para que protejas tus datos y los
sincronices entre tus propios dispositivos. Está **desactivada por defecto** y solo se activa
después
de que conectes explícitamente tu cuenta de Google Drive y autorices el acceso.

Cuando está activada, la App sube un archivo de copia de seguridad con tus datos a tu cuenta de
Google
Drive:

- **Registros de ingesta de agua** (cantidad, marca de tiempo, tipo de entrada)
- **Metas diarias de hidratación** (cantidades objetivo y fechas de vigencia)
- **Preferencias** (unidad, formato de hora, cantidad de adición rápida en notificaciones)
- Un identificador aleatorio por dispositivo, usado solo para combinar datos entre tus propios
  dispositivos

La copia de seguridad **no** incluye tu nombre, correo, datos de pago ni ningún identificador de
publicidad o de análisis.

**Dónde se almacena tu copia de seguridad:** el archivo se escribe en tu *carpeta de datos de la
aplicación* privada y oculta (`appDataFolder`) en Google Drive, un área de Drive reservada para la
App
que no aparece entre tus archivos normales de Drive y no es accesible para otras apps. La App
solicita
el ámbito `drive.appdata` más tu perfil básico de Google (correo y nombre) únicamente para mostrar
qué
cuenta está conectada.

La copia de seguridad se almacena en **tu propia** cuenta de Google Drive. No operamos ningún
servidor
que almacene estos datos, y el desarrollador no puede leer tu copia de seguridad. Puedes
desconectarla
en cualquier momento desde **Ajustes → Copia de seguridad**, y puedes eliminar el archivo
directamente
desde tu cuenta de Google Drive.

**Servicios de la API de Google — Divulgación de Uso Limitado.** El uso por parte de Drink Water App
de la información recibida de las API de Google se rige por la
[Política de Datos de Usuario de los Servicios de la API de Google](https://developers.google.com/terms/api-services-user-data-policy),
incluidos los requisitos de **Uso Limitado**. Los datos a los que se accede mediante Google Drive se
usan **únicamente** para proporcionar y mejorar la función de copia de seguridad y sincronización
que
solicitaste. Nunca se venden, nunca se usan para publicidad, y nunca se transfieren a terceros salvo
lo necesario para proporcionar la función, cumplir con la ley aplicable o como parte de una fusión o
adquisición. No permitimos que personas lean tus datos de Google Drive salvo que contemos con tu
consentimiento explícito, sea necesario por motivos de seguridad (como investigar abusos), o para
cumplir con la ley aplicable.

## 3. Permisos solicitados

- **POST_NOTIFICATIONS** — enviar notificaciones recordatorias de hidratación
- **SCHEDULE_EXACT_ALARM** — entregar recordatorios a la hora exacta programada
- **RECEIVE_BOOT_COMPLETED** — restablecer los recordatorios al reiniciar el dispositivo
- **SYSTEM_ALERT_WINDOW** — mostrar una animación de celebración al cumplir la meta diaria
- **INTERNET** — necesario para anuncios (versión gratuita), análisis, validación de compras y copia
  de seguridad en la nube opcional

No se solicitan permisos de ubicación, contactos, cámara, micrófono ni datos de salud.

## 4. Cómo usamos tu información

- Proporcionar las funciones principales de seguimiento — todos los datos locales
- Enviar recordatorios de hidratación — horas activas, frecuencia
- Mostrar anuncios personalizados (versión gratuita) — ID de publicidad del dispositivo
- Entender el uso de funciones y mejorar la App — eventos anónimos de Mixpanel (opt-in)
- Corregir fallos y errores — reportes de Firebase Crashlytics
- Procesar compras dentro de la App — tokens de compra de RevenueCat
- Respaldar y sincronizar tus datos (opcional) — registros, metas y preferencias subidos a tu Google
  Drive

No usamos tus datos para perfilado, toma de decisiones automatizada ni ningún propósito más allá de
los indicados.

## 5. Compartición y divulgación de datos

No vendemos, alquilamos ni comerciamos con tu información personal.

- **Google Firebase (Crashlytics)** — reporte de fallos — info del dispositivo, trazas de error
- **Mixpanel** — análisis (solo opt-in) — eventos anónimos, ID aleatorio
- **Google AdMob** — publicidad (solo versión gratuita) — ID de publicidad
- **RevenueCat** — gestión de compras — tokens de compra de Google Play
- **Google Drive** — copia de seguridad en la nube (opcional, tú la conectas) — archivo de respaldo
  en tu carpeta de datos de app privada; tu correo/nombre para mostrar la cuenta conectada

También podemos divulgar tu información si lo exige la ley o para proteger los derechos, la
seguridad
o la propiedad de los usuarios o el público. La App genera un ID único por instalación, lo que
significa que no es posible identificar a un usuario específico a menos que se acceda a los archivos
locales del dispositivo. En cualquier caso, cualquier usuario puede solicitar la eliminación de sus
datos en cualquier momento; reinstalar la App también elimina permanentemente todos los datos
locales.

La información de seguimiento se usa únicamente para determinar qué ajustes y funciones son más
utilizados, con el fin de establecer mejores valores predeterminados en futuras actualizaciones y
agregar o eliminar funciones según los patrones de uso. Todo esto tiene como objetivo mejorar la
experiencia del usuario.

## 6. Retención y eliminación de datos

- **Datos locales de la App** permanecen en tu dispositivo hasta que la desinstales o uses Ajustes →
  Borrar todos los datos.
- **Datos de análisis de Mixpanel** retenidos según su política (típicamente 5 años). Puedes
  desactivarlo en cualquier momento.
- **Reportes de Crashlytics** retenidos por Google según las políticas de Firebase.
- **Registros de compras de RevenueCat** retenidos según los términos de la tienda y la ley
  aplicable.
- **Datos de copia de seguridad en la nube** residen en tu propia cuenta de Google Drive y
  permanecen hasta que los elimines allí o desconectes la copia de seguridad. No tenemos control ni
  acceso a los archivos almacenados en tu cuenta.

## 7. No es una aplicación médica

Drink Water App es una herramienta de bienestar personal diseñada para ayudarte a construir un
hábito
de hidratación. **No es un dispositivo médico** y no proporciona consejos médicos, diagnósticos ni
tratamientos. La meta diaria de agua sugerida por la App es una estimación basada en fórmulas de uso
común y no sustituye el consejo de un profesional de la salud calificado. Consulta a tu médico para
cualquier decisión relacionada con tu salud.

## 8. Privacidad de menores

La App es fácil de usar y puede ser usada por personas de cualquier edad. Recomendamos que los
menores
sean guiados por un padre o tutor antes de usar la App. La App no está dirigida a menores. No
recopilamos conscientemente información personal de menores. Si eres padre, madre o tutor y crees
que
tu hijo/a ha proporcionado datos personales a través de la App, por favor contáctanos en
eahm3595@gmail.com y tomaremos las medidas adecuadas para solucionarlo.

## 9. Seguridad de los datos

Todos los datos que introduces en la App se almacenan localmente en el almacenamiento de tu
dispositivo. No operamos servidores que almacenen tus datos personales de hidratación. Si activas la
Copia de seguridad en la nube, tus datos se transmiten a través de una conexión cifrada y se
almacenan
en tu propia cuenta de Google Drive, protegida por la seguridad de Google y tus credenciales. Sin
embargo, ningún método de almacenamiento o transmisión electrónica es completamente seguro, y te
recomendamos proteger tu dispositivo y tu cuenta de Google con las técnicas de seguridad
recomendadas.

## 10. Tus derechos

- **Acceso** — todos tus datos son visibles dentro de la misma App.
- **Corrección** — edita datos desde los ajustes y pantallas de configuración.
- **Eliminación** — usa Ajustes → Borrar datos, o desinstala la App.
- **Retirar consentimiento** para análisis — Ajustes → Privacidad y legal → Seguimiento de uso.
- **Oponerte o restringir** el procesamiento — contáctanos para solicitudes adicionales.

**Usuarios del EEE:** puedes presentar una queja ante tu autoridad local de protección de datos.

**California (CCPA):** no vendemos información personal según la definición de la CCPA.

## 11. Cambios en esta Política de Privacidad

Podemos actualizar esta Política de Privacidad periódicamente. Cuando lo hagamos, actualizaremos la
fecha de "Última actualización" en este documento y, si los cambios son importantes, te
notificaremos
dentro de la App al próximo inicio. El uso continuado de la App después de publicar los cambios
constituye tu aceptación de la política actualizada.

## 12. Contacto

**Email:** eahm3595@gmail.com
**App:** Drink Water App (com.eahm.drinkwaterapp)
