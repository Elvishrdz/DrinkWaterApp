package com.eahm.drinkwaterapp.core.billing

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PurchaseState {

    data object Idle : PurchaseState
    data object Loading : PurchaseState
    data object Success : PurchaseState
    data object Restored : PurchaseState
    data object NothingToRestore : PurchaseState
    data class Error(val message: String) : PurchaseState
}
