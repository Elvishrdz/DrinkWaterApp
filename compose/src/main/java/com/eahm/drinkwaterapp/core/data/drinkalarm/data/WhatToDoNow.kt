package com.eahm.drinkwaterapp.core.data.drinkalarm.data

internal sealed interface WhatToDoNow {
    /** Inside active time — alarm trigger passed without firing → remind immediately */
    data object RemindNow : WhatToDoNow

    /** Inside active time — no alarm set, trigger time hasn't arrived yet → schedule next interval */
    data object ScheduleNextInterval : WhatToDoNow

    /** Inside active time — alarm active but not at the expected next interval → reschedule */
    data object RescheduleInterval : WhatToDoNow

    /** Outside active time — schedule alarm for the start of the next active window */
    data object ScheduleForActiveStart : WhatToDoNow

    /** Alarm is correctly set — nothing to do */
    data object DoNothing : WhatToDoNow
}