package com.eahm.drinkwaterapp.ui.awesometexts

class NotificationBigTextMessages {

    fun getBigText(
        category: MessageCategory,
    ): String? {
        return when (category) {
            MessageCategory.Start -> {
                "After a full night of sleep, your body is naturally low on fluids. Starting your morning with water rehydrates your cells, boosts your metabolism, and sets a healthy tone for the rest of the day. You've got this!"
            }

            MessageCategory.End -> {
                "You made it through the day! Open the app to see a summary of your hydration. Celebrate the wins and find chances to do even better tomorrow."
            }

            is MessageCategory.ActiveHours -> {
                determineByHoursOfDay(category.hour)
            }
        }
    }

    private fun determineByHoursOfDay(hour: Int): String {
        return when (MessageRanges.getByHourOfDay(hour)) {
            MessageRanges.FROM_0_UNTIL_4 ->
                "You're still up and your body is still working hard. A glass of water right now helps keep your focus sharp, supports your organs, and replaces fluids even through the night."

            MessageRanges.FROM_4_UNTIL_6 ->
                "Early mornings are powerful. After hours of sleep, your body is ready to be refreshed. Starting with a glass of water rehydrates your cells and sets a healthy, energizing tone for your whole day."

            MessageRanges.FROM_6_UNTIL_8 ->
                "Good morning! After 7-8 hours of sleep, your body wakes up naturally dehydrated. Drinking water first thing boosts your metabolism, activates your organs, and helps you feel alert and ready to go."

            MessageRanges.FROM_8_UNTIL_9 ->
                "Your morning is in full swing! Staying hydrated sharpens focus, keeps your energy steady, and helps your brain perform at its best. Grab a glass and power through your morning."

            MessageRanges.FROM_9_UNTIL_10 ->
                "Did you know? Even mild dehydration, as little as 1-2%, can noticeably reduce concentration and mood. Keep sipping water throughout the morning to stay on top of your game."

            MessageRanges.FROM_10_UNTIL_12 ->
                "The late morning is the perfect time to build healthy habits. Regular hydration helps flush out toxins, supports your immune system, and keeps your skin looking great. Time for another glass!"

            MessageRanges.FROM_12_UNTIL_13 ->
                "Lunchtime! Water aids digestion and helps your body absorb the nutrients from your meal more effectively. Drink a glass before or during lunch for the best results."

            MessageRanges.FROM_13_UNTIL_15 ->
                "The early afternoon can feel slow, energy dips after lunch are very common. Staying well hydrated helps regulate your body temperature and keeps your mind alert. Drink up and power through!"

            MessageRanges.FROM_15_UNTIL_16 ->
                "Feeling a bit sluggish this afternoon? It might just be dehydration. A glass of water is often all you need to refresh your mind and body and push through the rest of the day with energy."

            MessageRanges.FROM_16_UNTIL_18 ->
                "You're in the home stretch! Consistent hydration throughout the day helps prevent headaches, maintain energy levels, and keep your kidneys healthy. Don't forget your water you're almost done!"

            MessageRanges.FROM_18_UNTIL_20 ->
                "The evening is a great time to reflect and recharge. Drinking water after dinner supports digestion, helps your body recover from the day, and prepares you for restful, restorative sleep."

            MessageRanges.FROM_20_UNTIL_22 ->
                "As you wind down for the night, gentle hydration helps your body recover from everything you've done today. A glass now is just right, your body will thank you in the morning."

            MessageRanges.FROM_22_UNTIL_0 ->
                "Before you drift off, a small glass of water helps your body stay hydrated through the night. You did a great job today rest well and wake up refreshed!"
        }
    }
}
