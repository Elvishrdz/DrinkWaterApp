package com.eahm.drinkwaterapp.core.data.systemnotifications.model

sealed interface DrinkIntent {

    val action: String
    val code: Int
}

data object NotificationClicked : DrinkIntent {
    override val action = "ON_NOTIFICATION_CLICKED"
    override val code = 12
}

data object NotificationRemoved : DrinkIntent {
    override val action = "ON_NOTIFICATION_REMOVED"
    override val code = 789
}

data object SkipDrink : DrinkIntent {
    override val action = "ON_SKIP_DRINK"
    override val code = 123
}

data object Drink : DrinkIntent {
    override val action = "ON_DRINK"
    override val code = 456
}
