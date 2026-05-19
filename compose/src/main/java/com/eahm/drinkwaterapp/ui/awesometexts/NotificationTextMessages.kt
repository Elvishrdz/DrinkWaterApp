package com.eahm.drinkwaterapp.ui.awesometexts

import android.content.Context
import com.eahm.drinkwaterapp.R
import kotlin.random.Random

// TODO: display messages with the amount of drinks so far, goal and remaining to reach the goal.
class NotificationTextMessages {

    fun getText(category: MessageCategory, context: Context): String? {
        return when (category) {
            MessageCategory.Start -> determineByStart(context)
            MessageCategory.End -> determineByEnd(context)
            is MessageCategory.ActiveHours -> determineByHoursOfDay(category.hour, context)
        }
    }

    private fun determineByStart(context: Context): String {
        val random = Random.nextInt(until = 4)
        return context.getString(
            when (random) {
                1 -> R.string.notif_msg_start_2
                2 -> R.string.notif_msg_start_3
                else -> R.string.notif_msg_start_1
            }
        )
    }

    private fun determineByEnd(context: Context): String {
        val random = Random.nextInt(until = 4)
        return context.getString(
            when (random) {
                1 -> R.string.notif_msg_end_2
                2 -> R.string.notif_msg_end_3
                else -> R.string.notif_msg_end_1
            }
        )
    }

    private fun determineByHoursOfDay(hour: Int, context: Context): String {
        val random = Random.nextInt(until = 3)
        return when (random) {
            1 -> determineByHoursOfDayGroup1(hour, context)
            else -> determineByHoursOfDayGroup2(hour, context)
        }
    }

    /** Variant A — functional / motivational tone */
    private fun determineByHoursOfDayGroup1(hour: Int, context: Context): String {
        return context.getString(
            when (MessageRanges.getByHourOfDay(hour)) {
                MessageRanges.FROM_0_UNTIL_4 -> R.string.notif_msg_h0_a
                MessageRanges.FROM_4_UNTIL_6 -> R.string.notif_msg_h4_a
                MessageRanges.FROM_6_UNTIL_8 -> R.string.notif_msg_h6_a
                MessageRanges.FROM_8_UNTIL_9 -> R.string.notif_msg_h8_a
                MessageRanges.FROM_9_UNTIL_10 -> R.string.notif_msg_h9_a
                MessageRanges.FROM_10_UNTIL_12 -> R.string.notif_msg_h10_a
                MessageRanges.FROM_12_UNTIL_13 -> R.string.notif_msg_h12_a
                MessageRanges.FROM_13_UNTIL_15 -> R.string.notif_msg_h13_a
                MessageRanges.FROM_15_UNTIL_16 -> R.string.notif_msg_h15_a
                MessageRanges.FROM_16_UNTIL_18 -> R.string.notif_msg_h16_a
                MessageRanges.FROM_18_UNTIL_20 -> R.string.notif_msg_h18_a
                MessageRanges.FROM_20_UNTIL_22 -> R.string.notif_msg_h20_a
                MessageRanges.FROM_22_UNTIL_0 -> R.string.notif_msg_h22_a
            }
        )
    }

    /** Variant B — casual / conversational tone */
    private fun determineByHoursOfDayGroup2(hour: Int, context: Context): String {
        return context.getString(
            when (MessageRanges.getByHourOfDay(hour)) {
                MessageRanges.FROM_0_UNTIL_4 -> R.string.notif_msg_h0_b
                MessageRanges.FROM_4_UNTIL_6 -> R.string.notif_msg_h4_b
                MessageRanges.FROM_6_UNTIL_8 -> R.string.notif_msg_h6_b
                MessageRanges.FROM_8_UNTIL_9 -> R.string.notif_msg_h8_b
                MessageRanges.FROM_9_UNTIL_10 -> R.string.notif_msg_h9_b
                MessageRanges.FROM_10_UNTIL_12 -> R.string.notif_msg_h10_b
                MessageRanges.FROM_12_UNTIL_13 -> R.string.notif_msg_h12_b
                MessageRanges.FROM_13_UNTIL_15 -> R.string.notif_msg_h13_b
                MessageRanges.FROM_15_UNTIL_16 -> R.string.notif_msg_h15_b
                MessageRanges.FROM_16_UNTIL_18 -> R.string.notif_msg_h16_b
                MessageRanges.FROM_18_UNTIL_20 -> R.string.notif_msg_h18_b
                MessageRanges.FROM_20_UNTIL_22 -> R.string.notif_msg_h20_b
                MessageRanges.FROM_22_UNTIL_0 -> R.string.notif_msg_h22_b
            }
        )
    }
}
