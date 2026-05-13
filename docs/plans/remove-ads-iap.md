# Plan: Compra única para quitar anuncios (RevenueCat)

## Contexto

La app monetiza con AdMob (banner en `HomeScreen`). El objetivo es agregar un pago único (lifetime) para que los usuarios puedan quitar los anuncios permanentemente.

**Solución elegida: RevenueCat** — abstrae la lógica de facturación para Google Play, Amazon Appstore y otras tiendas. El SDK se encarga de verificar compras, caché offline, restaurar compras, etc. sin que sea necesario manejar el billing directamente.

**Módulo objetivo:** `compose/` (Kotlin + Jetpack Compose — módulo principal activo)

---

## Arquitectura

```
RevenueCat SDK
     |
     v
PurchaseManager  (singleton, AppInitializer, Koin)
     |  StateFlow<Boolean> isAdFree
     +---> HomeScreen         → oculta DrinkWaterBannerAd
     +---> SettingsViewModel  → drives sección PREMIUM + acciones de compra
```

---

## Pasos de implementación

### 1. Agregar dependencia

**`gradle/libs.versions.toml`**
```toml
[versions]
revenuecat = "8.12.4"

[libraries]
revenuecat = { module = "com.revenuecat.purchases:purchases", version.ref = "revenuecat" }
```

**`compose/build.gradle.kts`** — dentro de `dependencies { }`:
```kotlin
implementation(libs.revenuecat)
```

---

### 2. Guardar la API key

**`compose/src/main/res/values/untranslated.xml`** — igual que las keys de AdMob:
```xml
<string name="revenuecat_api_key" translatable="false">PONER_API_KEY_AQUI</string>
```

---

### 3. Crear `PurchaseManager` (nuevo archivo)

**`compose/src/main/java/com/eahm/drinkwaterapp/core/billing/PurchaseManager.kt`**

Sigue el mismo patrón que `NotificationPermissionObserver`:
- Implementa `AppInitializer` → `BaseApplication` lo inicializa automáticamente vía `getAll<AppInitializer>().forEach { it.initialize() }`. **No se modifica `BaseApplication.kt`.**
- Expone `StateFlow<Boolean> isAdFree` (fuente única de verdad — RevenueCat cachea `CustomerInfo` localmente)
- Expone `StateFlow<PurchaseState> purchaseState` para feedback en la UI

```kotlin
private const val ENTITLEMENT_AD_FREE = "ad_free"  // debe coincidir con el dashboard RC

internal class PurchaseManager(
    private val application: Application,
) : AppInitializer {

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    override fun initialize() {
        val apiKey = application.getString(R.string.revenuecat_api_key)
        Purchases.configure(PurchasesConfiguration.Builder(application, apiKey).build())
        refreshCustomerInfo()
    }

    fun refreshCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isAdFree.value = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
            }
            override fun onError(error: PurchasesError) { /* mantener estado anterior */ }
        })
    }

    // Busca offering "remove_ads" en RC, fallback a current offering
    fun fetchOfferings(onResult: (Package?) -> Unit) {
        Purchases.sharedInstance.getOfferings(object : GetOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                val pkg = offerings["remove_ads"]?.availablePackages?.firstOrNull()
                    ?: offerings.current?.availablePackages?.firstOrNull()
                onResult(pkg)
            }
            override fun onError(error: PurchasesError) { onResult(null) }
        })
    }

    fun purchase(activity: Activity, pkg: Package) {
        _purchaseState.value = PurchaseState.Loading
        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, pkg).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    _isAdFree.value = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
                    _purchaseState.value = if (_isAdFree.value) PurchaseState.Success else PurchaseState.Idle
                }
                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    _purchaseState.value = if (userCancelled) PurchaseState.Idle else PurchaseState.Error(error.message)
                }
            }
        )
    }

    fun restore() {
        _purchaseState.value = PurchaseState.Loading
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isAdFree.value = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
                _purchaseState.value = if (_isAdFree.value) PurchaseState.Success else PurchaseState.NothingToRestore
            }
            override fun onError(error: PurchasesError) {
                _purchaseState.value = PurchaseState.Error(error.message)
            }
        })
    }
}

sealed interface PurchaseState {
    data object Idle : PurchaseState
    data object Loading : PurchaseState
    data object Success : PurchaseState
    data object NothingToRestore : PurchaseState
    data class Error(val message: String) : PurchaseState
}
```

---

### 4. Crear `BillingModule` (nuevo archivo)

**`compose/src/main/java/com/eahm/drinkwaterapp/core/billing/BillingModule.kt`**
```kotlin
internal val billingModule = module {
    singleOf(::PurchaseManager) bind AppInitializer::class
}
```

Idéntico en estructura a `notificationPermissionModule`.

---

### 5. Registrar en Koin

**`compose/src/main/java/com/eahm/drinkwaterapp/di/DataModule.kt`** — agregar una línea:
```kotlin
includes(billingModule)
```

---

### 6. Ocultar el banner cuando se compró

**`compose/src/main/java/com/eahm/drinkwaterapp/feature/home/HomeScreen.kt`**
```kotlin
val purchaseManager = koinInject<PurchaseManager>()
val isAdFree by purchaseManager.isAdFree.collectAsStateWithLifecycle()

// Envolver el banner existente (líneas ~112-114):
if (!isAdFree) {
    DrinkWaterBannerAd(modifier = Modifier.fillMaxWidth())
}
```

---

### 7. UI de compra en Settings

#### `feature/settings/SettingsState.kt` — agregar 2 campos:
```kotlin
val isAdFree: Boolean = false,
val purchaseState: PurchaseState = PurchaseState.Idle,
```

#### `feature/settings/SettingsViewModel.kt`

Agregar `PurchaseManager` al constructor (Koin lo inyecta automáticamente vía `viewModelOf`):
```kotlin
internal class SettingsViewModel(
    // ...campos existentes sin cambios...
    val purchaseManager: PurchaseManager,   // agregar al final
) : ViewModel()
```

Incluir los nuevos flows en el `combine` existente:
```kotlin
// Agregar a la cadena de combine:
purchaseManager.isAdFree,
purchaseManager.purchaseState,
// Y mapearlos en el .copy(...):
isAdFree = isAdFree,
purchaseState = purchaseState,
```

Agregar funciones de acción:
```kotlin
fun onPurchase(activity: Activity, pkg: Package) = purchaseManager.purchase(activity, pkg)
fun onRestorePurchase() = purchaseManager.restore()
```

#### Nuevo archivo: `feature/settings/ui/RemoveAdsBottomSheet.kt`

Sigue el patrón de `GoalBottomSheet.kt` — usa `AppBottomSheet`, `DwPrimaryButton`, `DwSecondaryButton`.

Contenido:
- Título "Remove Ads"
- Párrafo explicativo sobre pago único y restauración
- `DwPrimaryButton`: "Remove Ads — $X.XX" (precio desde RevenueCat)
- `DwSecondaryButton`: "Restore Purchase"
- Feedback inline: `CircularProgressIndicator` en loading, mensaje de error/nada-que-restaurar

#### `feature/settings/SettingsScreen.kt`

Agregar sección "PREMIUM" encima de la sección "ABOUT":

```kotlin
// Si !isAdFree: fila "Remove Ads" con chevron → abre RemoveAdsBottomSheet
// Si isAdFree: fila estática "Ads removed · Active" (no clickeable)
```

En el composable `SettingsScreen` (stateful), manejar el estado del sheet:
```kotlin
var showRemoveAdsSheet by rememberSaveable { mutableStateOf(false) }
var rcPackage by remember { mutableStateOf<Package?>(null) }
var rcPriceString by remember { mutableStateOf<String?>(null) }

fun handleRemoveAdsTap() {
    purchaseManager.fetchOfferings { pkg ->
        rcPackage = pkg
        rcPriceString = pkg?.product?.price?.formatted
        showRemoveAdsSheet = true
    }
}
```

---

## Configuración en dashboards (pasos externos al código)

1. **Google Play Console** → crear producto *Non-consumable (one-time)* con ID `remove_ads_lifetime`
2. **RevenueCat dashboard**:
   - Conectar app de Google Play
   - Crear entitlement: ID = `ad_free`
   - Crear producto: adjuntar ID de Google Play `remove_ads_lifetime`
   - Crear offering: ID = `remove_ads`, paquete tipo `Lifetime`
   - Copiar la API key (Google Play) → pegar en `untranslated.xml`

---

## Archivos a modificar/crear

| Archivo | Cambio |
|---|---|
| `gradle/libs.versions.toml` | Agregar versión y librería de RevenueCat |
| `compose/build.gradle.kts` | `implementation(libs.revenuecat)` |
| `compose/.../res/values/untranslated.xml` | Agregar `revenuecat_api_key` |
| `core/billing/PurchaseManager.kt` | **CREAR** — adapter RevenueCat + `AppInitializer` |
| `core/billing/BillingModule.kt` | **CREAR** — módulo Koin |
| `di/DataModule.kt` | `includes(billingModule)` |
| `feature/home/HomeScreen.kt` | Envolver banner con `if (!isAdFree)` |
| `feature/settings/SettingsState.kt` | Agregar `isAdFree` y `purchaseState` |
| `feature/settings/SettingsViewModel.kt` | Inyectar `PurchaseManager`, exponer acciones |
| `feature/settings/SettingsScreen.kt` | Sección PREMIUM + trigger del sheet |
| `feature/settings/ui/RemoveAdsBottomSheet.kt` | **CREAR** — UI de compra/restaurar |

**3 archivos nuevos, 8 modificaciones. No se modifica `BaseApplication.kt`, `StartDI.kt` ni `MainActivity.kt`.**

---

## Verificación end-to-end

1. Build debug → el banner responde a `isAdFree` (testeable con mock)
2. Build staging/release con [producto de prueba de Google Play](https://developer.android.com/google/play/billing/test) → completar compra → banner desaparece, sección PREMIUM muestra "Ads removed"
3. Desinstalar y reinstalar → "Restore Purchase" recupera el estado
4. Lanzar sin red → `isAdFree` mantiene el último estado (RevenueCat cachea `CustomerInfo` en disco)
5. Verificar en Settings que la sección PREMIUM muestra el estado correcto antes y después de comprar
