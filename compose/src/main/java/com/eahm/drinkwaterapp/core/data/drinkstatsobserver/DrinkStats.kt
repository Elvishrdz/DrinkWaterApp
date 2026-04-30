package com.eahm.drinkwaterapp.core.data.drinkstatsobserver

data class DrinkStats(
    val goalAmount: Float,
    val drinkAmount: Float,
    val goalReached: Boolean,
) {
    val remainingAmount = (goalAmount - drinkAmount).coerceAtLeast(0f)
}
