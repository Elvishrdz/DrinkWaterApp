package com.eahm.drinkwaterapp.core.data.drinkalarm

sealed class NextAlarmResult {

    /** Daily goal already reached — cancel alarm, do not reschedule. */
    data object GoalReached : NextAlarmResult()

    /** Before the active window opens. Delay until window start. */
    data class BeforeWindow(val delayMillis: Long) : NextAlarmResult()

    /** Active window has closed for today. Delay until tomorrow's window start. */
    data class AfterWindow(val delayMillis: Long) : NextAlarmResult()

    /** Within window; next drink scheduled at [nextSlot]. */
    data class Schedule(val delayMillis: Long, val nextSlot: DrinkSlot) : NextAlarmResult()

    /** Last scheduled drink is happening now. Done for today; delay until tomorrow's window start. */
    data class LastDrink(val delayMillis: Long) : NextAlarmResult()
}
