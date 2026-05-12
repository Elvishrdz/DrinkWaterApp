package com.eahm.drinkwaterapp.ui.awesometexts

class NotificationTextMessages {

    fun getText(
        category: MessageCategory,
    ): String? {
        return when (category) {
            MessageCategory.Start -> {
                "Time to get up with the right foot"
            }

            MessageCategory.End -> {
                "Open the app to check the times you drink today"
            }

            is MessageCategory.ActiveHours -> {
                determineByHoursOfDay(category.hour)
            }
        }
    }

    private fun determineByHoursOfDay(hour: Int): String {
        return when (MessageRanges.getByHourOfDay(hour)) {
            MessageRanges.FROM_0_UNTIL_4,
            MessageRanges.FROM_4_UNTIL_6,
            MessageRanges.FROM_6_UNTIL_8,
            MessageRanges.FROM_8_UNTIL_9,
            MessageRanges.FROM_9_UNTIL_10,
            MessageRanges.FROM_10_UNTIL_12,
            MessageRanges.FROM_12_UNTIL_13,
            MessageRanges.FROM_13_UNTIL_15,
            MessageRanges.FROM_15_UNTIL_16,
            MessageRanges.FROM_16_UNTIL_18,
            MessageRanges.FROM_18_UNTIL_20,
            MessageRanges.FROM_20_UNTIL_22,
            MessageRanges.FROM_22_UNTIL_0,
                -> {
                "Stay hydrated to feel energetic!"
            }
        }
    }
}
