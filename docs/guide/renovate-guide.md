# Renovate Guide — DrinkWaterApp

Guía para activar y reconfigurar Renovate en este proyecto después de clonar el repositorio.
Cubre qué es Renovate, cómo instalarlo en GitHub, cómo está configurado en este proyecto y cómo
ajustarlo.

---

## 1. ¿Qué es Renovate?

Renovate es un bot que monitorea las dependencias del proyecto y abre Pull Requests automáticas
cuando hay nuevas versiones disponibles. Soporta el catálogo de versiones de Gradle
(`libs.versions.toml`), el Gradle wrapper, y los workflows de GitHub Actions.

**Lo que hace por este proyecto:**

- Detecta actualizaciones en `gradle/libs.versions.toml`
- Actualiza el Gradle wrapper (`gradle/wrapper/gradle-wrapper.properties`)
- Actualiza las versiones de los actions en `.github/workflows/`
- Agrupa dependencias relacionadas en una sola PR para evitar incompatibilidades

---

## 2. Requisitos previos

- Acceso de administrador al repositorio en GitHub
- El repositorio debe ser público **o** estar en un plan que permita GitHub Apps en repos privados

---

## 3. Activar Renovate en GitHub

Para ver el estado de un proyecto configurado ir a la consola de med.io. Usa tu nombre de usuario de
github en la url:
https://developer.mend.io/github/{username}

### Paso 1 — Instalar la app de Renovate

1. Ir a [https://github.com/apps/renovate](https://github.com/apps/renovate)
2. Hacer clic en **Install** (o **Configure** si ya está instalada en la cuenta)
3. En la pantalla de permisos, seleccionar **Only select repositories**
4. Buscar y seleccionar el repositorio `DrinkWaterApp`
5. Confirmar la instalación

### Paso 2 — PR de inicialización

Una vez instalada la app, Renovate abrirá automáticamente una PR llamada
**"Configure Renovate"**. Esta PR no agrega nada al proyecto porque el archivo `renovate.json`
ya existe en el repositorio. Solo confirmar que la configuración fue detectada y **cerrar la PR
sin hacer merge** (o mergearla si propone algún cambio válido).

### Paso 3 — Verificar la detección

En la pestaña **Actions** del repositorio, Renovate ejecutará un primer escaneo. Al terminar,
abrirá las PRs de actualización que encuentre pendientes basándose en `renovate.json`.

---

## 4. Configuración actual del proyecto

El archivo `renovate.json` en la raíz del repositorio contiene toda la configuración. No es
necesario modificarlo para que Renovate funcione; está listo para usarse.

### Agenda

```json
"schedule": ["before 6am on Monday"]
```

Renovate solo abre PRs los **lunes antes de las 6am**. Esto evita interrupciones durante la
semana y concentra las actualizaciones en un solo momento.

### Límites de PRs

```json
"prConcurrentLimit": 5,
"prHourlyLimit": 2
```

Máximo 5 PRs abiertas al mismo tiempo y 2 por hora. Evita que Renovate sature el repositorio
con decenas de PRs simultáneas.

### Etiqueta automática

Todas las PRs de Renovate reciben la etiqueta `dependencies` para facilitar el filtrado.

---

## 5. Grupos de dependencias

Las dependencias están agrupadas para que las actualizaciones que deben ir juntas lleguen en
una sola PR.

| Grupo                          | Dependencias incluidas                                                 | Razón                                        |
|--------------------------------|------------------------------------------------------------------------|----------------------------------------------|
| **Kotlin & KSP**               | `kotlin`, `ksp`, `composeCompilerGradlePlugin`                         | Deben ser versiones compatibles entre sí     |
| **Android Gradle Plugin**      | `com.android.tools.build:gradle`                                       | AGP y las herramientas van de la mano        |
| **Room**                       | `room`, `roomGradlePlugin`, `roomRuntime`                              | Tres refs en el TOML que deben ser idénticas |
| **AndroidX Lifecycle**         | Los 8 artefactos de `androidx.lifecycle`                               | Comparten la versión `viewModel`             |
| **AndroidX DataStore**         | `datastore`, `datastore-preferences`                                   | Comparten versión                            |
| **Koin**                       | Todos los artefactos de `io.insert-koin`                               | Comparten la versión `koin` del TOML         |
| **Firebase & Google Services** | `firebase-crashlytics-gradle`, `google-services`                       | Se actualizan en pares                       |
| **GitHub Actions**             | `actions/checkout`, `actions/setup-java`, `r0adkll/upload-google-play` | Un solo PR para todos los actions            |

### Registro de JitPack

```json
"registryUrls": ["https://jitpack.io"]
```

Necesario para que Renovate detecte actualizaciones de `com.github.anhaki:PickTime-Compose`,
que viene del repositorio de JitPack.

---

## 6. Flujo de trabajo recomendado

1. Cada lunes revisar las PRs abiertas por Renovate
2. Para el grupo **Kotlin & KSP**: verificar en el
   [changelog de KSP](https://github.com/google/ksp/releases) que la versión de KSP es
   compatible con la versión de Kotlin antes de mergear
3. Para el grupo **Room**: el TOML tiene tres refs separadas (`room`, `roomGradlePlugin`,
   `roomRuntime`). Renovate las actualiza todas en una sola PR, verificar que las tres tengan
   el mismo valor después del merge
4. Mergear el resto de grupos de menor riesgo (Koin, Lifecycle, DataStore, Actions)
5. Hacer build de verificación antes de mergear cualquier actualización de AGP o Kotlin

---

## 7. Ajustes comunes

### Cambiar la agenda

Editar el campo `schedule` en `renovate.json`. Ejemplos:

```json
// Todos los días a la madrugada
"schedule": ["before 4am"]

// Lunes y jueves
"schedule": ["before 6am on Monday and Thursday"]

// Sin restricción de horario
"schedule": [
"at any time"
]
```

### Ignorar una dependencia

Agregar al array `ignoreDeps` en `renovate.json`:

```json
"ignoreDeps": [
"com.github.anhaki:PickTime-Compose"
]
```

### Agregar automerge para un grupo

Para que un grupo se mergee automáticamente sin revisión manual (útil solo para parches menores):

```json
{
  "matchPackagePatterns": [
    "^androidx\\.datastore"
  ],
  "groupName": "AndroidX DataStore",
  "automerge": true,
  "automergeType": "pr"
}
```

### Agregar un nuevo grupo

Si se incorpora una nueva librería multi-artefacto, agregar una entrada en `packageRules`:

```json
{
  "description": "Nombre descriptivo del grupo",
  "matchPackagePatterns": [
    "^com.ejemplo.libreria"
  ],
  "groupName": "Nombre del grupo"
}
```

---

## 8. Solución de problemas

**Renovate no abre PRs después de instalarlo**

- Verificar que el repositorio fue seleccionado al instalar la app (GitHub > Settings >
  Applications > Renovate > Repository access)
- Renovate respeta la agenda configurada; fuera del horario no ejecuta. Para forzar un escaneo
  inmediato, ir al [Dashboard de Renovate](https://developer.mend.io/) e iniciar un rebase manual

**Error "can't find renovate.json"**

- Confirmar que el archivo existe en la raíz del repositorio (no en una subcarpeta)
- Confirmar que la rama principal del repositorio es `development` (como está configurado en
  este proyecto)

**Una PR de Renovate rompe el build**

1. Cerrar la PR sin mergear
2. Agregar la dependencia problemática a `ignoreDeps` temporalmente
3. Investigar la incompatibilidad en el changelog de la librería
4. Cuando esté resuelta, eliminar la entrada de `ignoreDeps`

**Renovate no detecta actualizaciones de JitPack**

- JitPack no siempre expone metadatos de versiones de forma estándar. Si `PickTime-Compose`
  no recibe PRs, es posible que JitPack no publique los metadatos necesarios. En ese caso,
  revisarlo manualmente
  en [https://jitpack.io/#anhaki/PickTime-Compose](https://jitpack.io/#anhaki/PickTime-Compose)
