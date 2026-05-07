package com.eahm.drinkwaterapp.core.data.datastore.data

import kotlinx.serialization.Serializable

@Serializable
data class DrinkSuggestion(
    val order: Int,
    val amountMl: Float,
)
