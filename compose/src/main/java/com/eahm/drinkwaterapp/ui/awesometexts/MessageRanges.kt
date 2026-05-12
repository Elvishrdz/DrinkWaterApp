package com.eahm.drinkwaterapp.ui.awesometexts

enum class MessageRanges {
    FROM_0_UNTIL_4,
    FROM_4_UNTIL_6,
    FROM_6_UNTIL_8,
    FROM_8_UNTIL_9,
    FROM_9_UNTIL_10,
    FROM_10_UNTIL_12,
    FROM_12_UNTIL_13,
    FROM_13_UNTIL_15,
    FROM_15_UNTIL_16,
    FROM_16_UNTIL_18,
    FROM_18_UNTIL_20,
    FROM_20_UNTIL_22,
    FROM_22_UNTIL_0,
    ;

    companion object
}

fun MessageRanges.Companion.getByHourOfDay(hour: Int): MessageRanges {
    return when (hour) {
        in 0..<4 -> MessageRanges.FROM_0_UNTIL_4
        in 4..<6 -> MessageRanges.FROM_4_UNTIL_6
        in 6..<8 -> MessageRanges.FROM_6_UNTIL_8
        in 8..<9 -> MessageRanges.FROM_8_UNTIL_9
        in 9..<10 -> MessageRanges.FROM_9_UNTIL_10
        in 10..<12 -> MessageRanges.FROM_10_UNTIL_12
        in 12..<13 -> MessageRanges.FROM_12_UNTIL_13
        in 13..<15 -> MessageRanges.FROM_13_UNTIL_15
        in 15..<16 -> MessageRanges.FROM_15_UNTIL_16
        in 16..<18 -> MessageRanges.FROM_16_UNTIL_18
        in 18..<20 -> MessageRanges.FROM_18_UNTIL_20
        in 20..<22 -> MessageRanges.FROM_20_UNTIL_22
        else -> MessageRanges.FROM_22_UNTIL_0
    }
}
