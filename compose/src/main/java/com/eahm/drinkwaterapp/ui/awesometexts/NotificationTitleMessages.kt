package com.eahm.drinkwaterapp.ui.awesometexts

class NotificationTitleMessages {

    fun getTitle(
        category: MessageCategory,
    ): String? {
        return when (category) {
            MessageCategory.Start -> {
                "Good morning! Let's start hydrated."
            }

            MessageCategory.End -> {
                "Great job today! See your progress."
            }

            is MessageCategory.ActiveHours -> {
                determineByHoursOfDay(category.hour)
            }
        }
    }

    private fun determineByHoursOfDay(hour: Int): String {
        return when (MessageRanges.getByHourOfDay(hour)) {
            MessageRanges.FROM_0_UNTIL_4 -> "Still up? Stay hydrated!"
            MessageRanges.FROM_4_UNTIL_6 -> "Early riser! Time to hydrate."
            MessageRanges.FROM_6_UNTIL_8 -> "Good morning! Start your day right."
            MessageRanges.FROM_8_UNTIL_9 -> "Morning boost time!"
            MessageRanges.FROM_9_UNTIL_10 -> "Mid-morning check-in!"
            MessageRanges.FROM_10_UNTIL_12 -> "Keep the momentum going!"
            MessageRanges.FROM_12_UNTIL_13 -> "Lunchtime hydration!"
            MessageRanges.FROM_13_UNTIL_15 -> "Afternoon reminder!"
            MessageRanges.FROM_15_UNTIL_16 -> "Beat the afternoon slump!"
            MessageRanges.FROM_16_UNTIL_18 -> "Almost there, keep drinking!"
            MessageRanges.FROM_18_UNTIL_20 -> "Evening hydration check!"
            MessageRanges.FROM_20_UNTIL_22 -> "Winding down? Keep sipping."
            MessageRanges.FROM_22_UNTIL_0 -> "Last sip of the day!"
        }
    }
}
