package com.eahm.drinkwaterapp.ui.awesometexts

import android.content.Context
import com.eahm.drinkwaterapp.R
import kotlin.random.Random

class NotificationTitleMessages {

    fun getTitle(category: MessageCategory, context: Context): String? {
        return when (category) {
            MessageCategory.Start -> context.getString(
                if (Random.nextBoolean()) R.string.notif_title_start_a else R.string.notif_title_start_b
            )

            MessageCategory.End -> context.getString(
                if (Random.nextBoolean()) R.string.notif_title_end_a else R.string.notif_title_end_b
            )

            is MessageCategory.ActiveHours -> determineByHoursOfDay(category.hour, context)
        }
    }

    private fun determineByHoursOfDay(hour: Int, context: Context): String {
        return if (Random.nextBoolean()) {
            determineByHoursOfDayGroup1(hour, context)
        } else {
            determineByHoursOfDayGroup2(hour, context)
        }
    }

    /** Variant A - motivational / energetic tone */
    private fun determineByHoursOfDayGroup1(hour: Int, context: Context): String {
        return context.getString(
            when (MessageRanges.getByHourOfDay(hour)) {
                MessageRanges.FROM_0_UNTIL_4 -> R.string.notif_title_h0_a
                MessageRanges.FROM_4_UNTIL_6 -> R.string.notif_title_h4_a
                MessageRanges.FROM_6_UNTIL_8 -> R.string.notif_title_h6_a
                MessageRanges.FROM_8_UNTIL_9 -> R.string.notif_title_h8_a
                MessageRanges.FROM_9_UNTIL_10 -> R.string.notif_title_h9_a
                MessageRanges.FROM_10_UNTIL_12 -> R.string.notif_title_h10_a
                MessageRanges.FROM_12_UNTIL_13 -> R.string.notif_title_h12_a
                MessageRanges.FROM_13_UNTIL_15 -> R.string.notif_title_h13_a
                MessageRanges.FROM_15_UNTIL_16 -> R.string.notif_title_h15_a
                MessageRanges.FROM_16_UNTIL_18 -> R.string.notif_title_h16_a
                MessageRanges.FROM_18_UNTIL_20 -> R.string.notif_title_h18_a
                MessageRanges.FROM_20_UNTIL_22 -> R.string.notif_title_h20_a
                MessageRanges.FROM_22_UNTIL_0 -> R.string.notif_title_h22_a
            }
        )
    }

    /** Variant B - conversational / friendly tone */
    private fun determineByHoursOfDayGroup2(hour: Int, context: Context): String {
        return context.getString(
            when (MessageRanges.getByHourOfDay(hour)) {
                MessageRanges.FROM_0_UNTIL_4 -> R.string.notif_title_h0_b
                MessageRanges.FROM_4_UNTIL_6 -> R.string.notif_title_h4_b
                MessageRanges.FROM_6_UNTIL_8 -> R.string.notif_title_h6_b
                MessageRanges.FROM_8_UNTIL_9 -> R.string.notif_title_h8_b
                MessageRanges.FROM_9_UNTIL_10 -> R.string.notif_title_h9_b
                MessageRanges.FROM_10_UNTIL_12 -> R.string.notif_title_h10_b
                MessageRanges.FROM_12_UNTIL_13 -> R.string.notif_title_h12_b
                MessageRanges.FROM_13_UNTIL_15 -> R.string.notif_title_h13_b
                MessageRanges.FROM_15_UNTIL_16 -> R.string.notif_title_h15_b
                MessageRanges.FROM_16_UNTIL_18 -> R.string.notif_title_h16_b
                MessageRanges.FROM_18_UNTIL_20 -> R.string.notif_title_h18_b
                MessageRanges.FROM_20_UNTIL_22 -> R.string.notif_title_h20_b
                MessageRanges.FROM_22_UNTIL_0 -> R.string.notif_title_h22_b
            }
        )
    }
}
