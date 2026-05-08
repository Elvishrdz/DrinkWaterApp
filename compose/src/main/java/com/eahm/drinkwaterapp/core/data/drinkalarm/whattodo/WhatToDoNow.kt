package com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo

import kotlinx.datetime.LocalDateTime

sealed interface WhatToDoNow {

    /** Inside active time — alarm trigger passed without firing → remind immediately */
    data object RemindNow : WhatToDoNow

    /** Inside active time — alarm active but not at the expected next interval → reschedule */
    data class RescheduleInterval(
        val wrong: LocalDateTime?,
        val correct: LocalDateTime,
    ) : WhatToDoNow

    /** Alarm is correctly set — nothing to do */
    data object DoNothing : WhatToDoNow

    data object InvalidActiveHours : WhatToDoNow
}
