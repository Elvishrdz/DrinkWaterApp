# Plan: Restaurar Compras via Android Auto Backup

## Contexto

La app tiene un pago único (lifetime) para quitar anuncios, implementado con RevenueCat. La política de restauración es:

- **Mismo dispositivo** (desinstalar + reinstalar): la compra se restaura automáticamente vía Android Auto Backup
- **Dispositivo nuevo**: no hay restauración automática — se requiere nueva compra o usar el botón "Restore Purchase"

**Problema actual:** `android:allowBackup="false"` está configurado en el manifiesto del módulo `compose/`, lo que significa que RevenueCat nunca respalda su ID de usuario anónimo (almacenado en SharedPreferences). Al reinstalar, RevenueCat genera un nuevo ID y no reconoce la compra previa.

**Módulo objetivo:** `compose/` (Kotlin + Jetpack Compose — módulo activo)

**Nota sobre módulo legacy:** El módulo `app/` tiene `android:allowBackup="false"` con `tools:replace="android:allowBackup"`. Este módulo es legacy (usa `.ThisApplication`). No se modifica.

---

## Arquitectura de restauración

```
Reinstalación mismo dispositivo
        |
        v
Android Auto Backup restaura SharedPreferences
        |
        v
RevenueCat recupera su anonymous user ID
        |
        v
PurchaseManager.refreshCustomerInfo() → isAdFree = true
        ↑
        (se llama en initialize() al arrancar la app)

Dispositivo nuevo / sin backup
        |
        v
Usuario toca "Restore Purchase"
        |
        v
Purchases.sharedInstance.restorePurchases()  ← vía cuenta Google Play
        |
        v
PurchaseState.Restored / NothingToRestore
```

---

## Pasos de implementación

### 1. Habilitar Android Auto Backup en el manifiesto

**`compose/src/main/AndroidManifest.xml`**

Cambiar:
```xml
android:allowBackup="false"
```
Por:
```xml
android:allowBackup="true"
android:fullBackupContent="@xml/backup_rules"
android:dataExtractionRules="@xml/data_extraction_rules"
```

---

### 2. Crear reglas de backup para Android 6–11 (nuevo archivo)

**`compose/src/main/res/xml/backup_rules.xml`**

Incluir solo SharedPreferences (donde RevenueCat almacena su ID anónimo).
Excluir DataStore y Room DB para evitar que datos viejos de agua sobreescriban datos frescos al reinstalar.

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="." />
    <exclude domain="database" path="." />
    <exclude domain="file" path="datastore" />
</full-backup-content>
```

---

### 3. Crear reglas de extracción para Android 12+ (nuevo archivo)

**`compose/src/main/res/xml/data_extraction_rules.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="." />
        <exclude domain="database" path="." />
        <exclude domain="file" path="datastore" />
    </cloud-backup>
    <device-transfer>
        <include domain="sharedpref" path="." />
    </device-transfer>
</data-extraction-rules>
```

---

### 4. Conservar el botón "Restore Purchase" — sin cambios de código

El botón se conserva. Ambas estrategias coexisten para el usuario:

- **Auto-backup**: restauración automática al reinstalar en el mismo dispositivo (sin acción del usuario)
- **Botón Restaurar**: funciona vía cuenta de Google Play, puede restaurar en dispositivos nuevos también

No se eliminan `PurchaseManager.restore()`, `PurchaseState.Restored`, ni `PurchaseState.NothingToRestore`.

---

## Archivos a modificar/crear

| Archivo | Cambio |
|---|---|
| `compose/src/main/AndroidManifest.xml` | Habilitar `allowBackup` + referenciar XMLs de reglas |
| `compose/src/main/res/xml/backup_rules.xml` | **CREAR** — reglas de backup Android 6–11 |
| `compose/src/main/res/xml/data_extraction_rules.xml` | **CREAR** — reglas de backup Android 12+ |

**2 archivos nuevos, 1 modificación. No se toca código Kotlin.**

---

## Escenarios de prueba

### Escenario 1 — Auto-backup: Reinstalación mismo dispositivo

1. Comprar con sandbox de Google Play en el dispositivo
2. Verificar que `isAdFree = true` (banner desaparece, Settings muestra "Ads removed")
3. Forzar backup manual:
   ```
   adb shell bmgr backup com.eahm.drinkwaterapp
   ```
4. Desinstalar la app
5. Reinstalar la app
6. **Resultado esperado:** `isAdFree = true` automáticamente al abrir la app, sin tocar "Restore Purchase"

### Escenario 2 — Botón Restaurar: mismo dispositivo sin backup previo

1. Desinstalar y reinstalar la app (sin haber hecho backup)
2. Tocar "Restore Purchase" en Settings
3. **Resultado esperado:** `PurchaseState.Restored` y `isAdFree = true`

### Escenario 3 — Botón Restaurar: dispositivo nuevo / cuenta Google Play

1. Instalar en un dispositivo nuevo con la misma cuenta de Google Play
2. Tocar "Restore Purchase" en Settings
3. **Resultado esperado:** documentar comportamiento real (puede restaurar o no según estado de la cuenta en RevenueCat)

### Escenario 4 — Sin compra previa

1. Tocar "Restore Purchase" sin haber comprado nunca
2. **Resultado esperado:** `PurchaseState.NothingToRestore` — UI muestra mensaje informativo

---

## Herramientas de prueba

```bash
# Forzar backup inmediato (útil para prueba rápida)
adb shell bmgr backup com.eahm.drinkwaterapp

# Simular restauración desde backup
adb shell bmgr restore com.eahm.drinkwaterapp

# Verificar estado del backup manager
adb shell bmgr enabled

# Activar el backup manager si está desactivado
adb shell bmgr enable true
```

Para compras de prueba usar Google Play sandbox (licencia de prueba configurada en Play Console).

---

## Verificación end-to-end

1. Build debug con `allowBackup="true"` → verificar que `bmgr backup` funciona (no arroja error)
2. Escenario 1 completo en dispositivo físico o emulador con cuenta Google
3. Escenario 4 sin cuenta de compra → confirmar `NothingToRestore`
4. Verificar en Android Studio → App Inspection → que SharedPreferences de RevenueCat se respaldan y no las DBs
