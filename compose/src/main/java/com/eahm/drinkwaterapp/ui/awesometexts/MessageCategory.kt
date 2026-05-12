package com.eahm.drinkwaterapp.ui.awesometexts

sealed interface MessageCategory {

    data object Start : MessageCategory

    data object End : MessageCategory

    data class ActiveHours(val hour: Int) : MessageCategory
}
