# Makefile Guide — DrinkWaterApp

Guía de referencia para usar y extender el `Makefile` del proyecto.
Cubre qué es un Makefile, sus beneficios en Android, todos los targets disponibles y cómo crear
nuevos.

---

## 1. ¿Qué es un Makefile?

Un `Makefile` es un archivo de automatización que agrupa comandos de shell bajo nombres cortos
llamados **targets**. Se ejecuta con la herramienta `make`, disponible en macOS por defecto (
incluida con Xcode Command Line Tools).

### Sintaxis básica

```makefile
target: dependencias
	comando
	otro_comando
```

> **Importante:** la indentación dentro de un target debe ser con **tab**, nunca con espacios.

### Ejecución

```bash
make <target>

# Ejemplos
make build-debug
make clean
make screenshot
```

---

## 2. Beneficios en este proyecto

| Beneficio                 | Detalle                                                                           |
|---------------------------|-----------------------------------------------------------------------------------|
| **Comandos cortos**       | `make install` en lugar de `./gradlew :compose:installDebug`                      |
| **Estandarización**       | Todo el equipo ejecuta exactamente el mismo comando                               |
| **Documentación viva**    | `make help` muestra todos los targets disponibles en cualquier momento            |
| **Composición**           | Un target puede depender de otros (`clean-build` ejecuta `clean` + `build-debug`) |
| **Independencia del IDE** | Funciona igual desde cualquier terminal, CI/CD o editor                           |
| **Atajos de ADB**         | Sin necesidad de recordar package IDs o flags de ADB                              |

---

## 3. Variables del proyecto

El Makefile define variables en la parte superior que centralizan los valores del proyecto. Si
alguno cambia, solo se actualiza en un lugar.

```makefile
APP_ID        := com.eahm.drinkwaterapp        # release
APP_ID_DEBUG  := com.eahm.drinkwaterapp.dev    # debug
APP_ID_STAGING:= com.eahm.drinkwaterapp.staging
APP_ID_QA     := com.eahm.drinkwaterapp.qa
MODULE        := :compose                       # módulo principal de Gradle
GRADLEW       := ./gradlew
```

---

## 4. Targets disponibles

### Build

Compila los APKs o el AAB para cada variante del proyecto.

| Target                | Comando equivalente                  |
|-----------------------|--------------------------------------|
| `make build`          | `./gradlew :compose:assemble`        |
| `make build-debug`    | `./gradlew :compose:assembleDebug`   |
| `make build-staging`  | `./gradlew :compose:assembleStaging` |
| `make build-qa`       | `./gradlew :compose:assembleQa`      |
| `make build-release`  | `./gradlew :compose:assembleRelease` |
| `make bundle-release` | `./gradlew :compose:bundleRelease`   |

`bundle-release` genera el `.aab` listo para subir al Play Store.

---

### Install

Instala el APK directamente en un dispositivo o emulador conectado.

| Target                 | Variante instalada |
|------------------------|--------------------|
| `make install`         | debug              |
| `make install-staging` | staging            |
| `make install-qa`      | qa                 |

---

### Clean

| Target             | Descripción                           |
|--------------------|---------------------------------------|
| `make clean`       | Elimina todos los outputs de build    |
| `make clean-build` | Ejecuta `clean` y luego `build-debug` |

---

### Testing

| Target          | Descripción                                        |
|-----------------|----------------------------------------------------|
| `make test`     | Unit tests del módulo `:compose` en variante debug |
| `make test-all` | Unit tests de todos los módulos                    |

---

### ADB – Lanzar la app

Abre la app en el dispositivo conectado sin necesidad de buscar el package ID.

| Target              | App que abre                     |
|---------------------|----------------------------------|
| `make open-debug`   | `com.eahm.drinkwaterapp.dev`     |
| `make open-staging` | `com.eahm.drinkwaterapp.staging` |
| `make open-qa`      | `com.eahm.drinkwaterapp.qa`      |
| `make open-release` | `com.eahm.drinkwaterapp`         |

---

### ADB – Limpiar datos

Borra todos los datos de la app (preferencias, base de datos, caché). Útil para probar el onboarding
o resetear estados.

| Target               | App que limpia |
|----------------------|----------------|
| `make clear-debug`   | debug          |
| `make clear-staging` | staging        |
| `make clear-qa`      | qa             |
| `make clear-release` | release        |

---

### ADB – Desinstalar

| Target                   | App que desinstala |
|--------------------------|--------------------|
| `make uninstall-debug`   | debug              |
| `make uninstall-staging` | staging            |
| `make uninstall-qa`      | qa                 |
| `make uninstall-release` | release            |

---

### ADB – Utilidades

| Target              | Descripción                                                           |
|---------------------|-----------------------------------------------------------------------|
| `make logcat`       | Muestra todo el logcat del dispositivo                                |
| `make logcat-debug` | Logcat filtrado solo por el proceso de la app debug                   |
| `make screenshot`   | Captura la pantalla y la guarda como `screenshot_YYYYMMDD_HHMMSS.png` |
| `make devices`      | Lista todos los dispositivos ADB conectados                           |

---

### Scripts

Estos targets son wrappers de los scripts existentes en `/scripts/`.

| Target                | Script que ejecuta                        |
|-----------------------|-------------------------------------------|
| `make new-feature`    | `scripts/new_dev_feature.sh`              |
| `make update-listing` | `scripts/update_listing.sh`               |
| `make deploy`         | `scripts/deploy_develop_to_play_store.sh` |

---

## 5. Flujos de trabajo comunes

### Desarrollo día a día

```bash
# Resetear datos y abrir app de cero
make clear-debug
make open-debug

# Build rápido después de cambios
make build-debug

# Ver logs de la app
make logcat-debug
```

### Probar una variante específica

```bash
make install-staging
make open-staging

# Algo no funciona, resetear datos
make clear-staging
```

### Antes de hacer push

```bash
make clean-build   # asegura que compila desde cero
make test          # corre los unit tests
```

### Preparar release para Play Store

```bash
make bundle-release   # genera el .aab
make deploy           # sube al Play Store vía script
```

---

## 6. Cómo agregar nuevos targets

### Estructura mínima

```makefile
mi-target:
	comando-a-ejecutar
```

### Con descripción en `help`

Para que aparezca en `make help`, agrega una línea en el bloque correspondiente dentro del target
`help`:

```makefile
help:
	@echo "    mi-target         Descripción de lo que hace"
```

Y decláralo en la lista `.PHONY` para evitar conflictos con archivos del mismo nombre:

```makefile
.PHONY: mi-target
```

### Ejemplo completo: instalar y abrir en un paso

```makefile
.PHONY: run-debug

run-debug: install open-debug
```

Este target depende de `install` y `open-debug`, así que `make` los ejecuta en orden
automáticamente.

### Ejemplo: target con parámetro de entorno

```makefile
.PHONY: record

record:
	adb shell screenrecord /sdcard/$(FILE).mp4
	@echo "Grabando... Ctrl+C para detener"
```

Uso: `make record FILE=demo_onboarding`

### Ejemplo: wrapper de Gradle task de un módulo específico

```makefile
.PHONY: assemble-ads

assemble-ads:
	$(GRADLEW) :ads:assembleDebug
```

---

## 7. Convenciones del proyecto

- Los targets de **build** usan el formato `build-<variante>`.
- Los targets de **ADB** usan el formato `<accion>-<variante>` (`open-debug`, `clear-staging`).
- Los **scripts** existentes en `/scripts/` se exponen como targets de una sola palabra.
- Todos los targets deben estar declarados en `.PHONY`.
- Las variables del proyecto se definen al inicio del archivo y en mayúsculas.

---

## 8. Referencia rápida

```bash
make help              # Ver todos los targets disponibles
make build-debug       # Compilar debug
make install           # Instalar debug en dispositivo
make clear-debug       # Borrar datos de debug
make open-debug        # Abrir debug en dispositivo
make test              # Correr unit tests
make clean             # Limpiar build outputs
make screenshot        # Capturar pantalla del dispositivo
make bundle-release    # Generar AAB para Play Store
make deploy            # Subir al Play Store
```
