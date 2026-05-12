package com.eahm.drinkwaterapp.ui.awesometexts

class NotificationTextMessages {

    fun getText(
        category: MessageCategory,
    ): String? {
        return when (category) {
            MessageCategory.Start -> {
                "Kick off the day with a refreshing glass of water."
            }

            MessageCategory.End -> {
                "Open the app to see how well you did today."
            }

            is MessageCategory.ActiveHours -> {
                determineByHoursOfDay(category.hour)
            }
        }
    }

    private fun determineByHoursOfDay(hour: Int): String {
        return when (MessageRanges.getByHourOfDay(hour)) {
            MessageRanges.FROM_0_UNTIL_4 -> "Even at night, your body needs water."
            MessageRanges.FROM_4_UNTIL_6 -> "Kick off your morning with a glass of water."
            MessageRanges.FROM_6_UNTIL_8 -> "A glass of water gets your metabolism going."
            MessageRanges.FROM_8_UNTIL_9 -> "Hydrate before your day picks up."
            MessageRanges.FROM_9_UNTIL_10 -> "Keep sipping to stay sharp and focused."
            MessageRanges.FROM_10_UNTIL_12 -> "Don't let the morning pass without hydrating."
            MessageRanges.FROM_12_UNTIL_13 -> "Pair your lunch with a glass of water."
            MessageRanges.FROM_13_UNTIL_15 -> "Stay sharp through the afternoon with water."
            MessageRanges.FROM_15_UNTIL_16 -> "A glass of water can lift your energy right now."
            MessageRanges.FROM_16_UNTIL_18 -> "Finishing up? Drink some water before you head out."
            MessageRanges.FROM_18_UNTIL_20 -> "Relax and rehydrate after a busy day."
            MessageRanges.FROM_20_UNTIL_22 -> "One more glass helps your body wind down."
            MessageRanges.FROM_22_UNTIL_0 -> "A small glass before bed keeps you refreshed overnight."
        }
    }
}
