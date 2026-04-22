package com.eahm.drinkwaterapp.core.data.systemnotifications.model

sealed interface DrinkIntent {

    val action: String
    val code: Int
    val intentCode: Int
}

data object NotificationClicked : DrinkIntent {
    override val action = "ON_NOTIFICATION_CLICKED"
    override val code = 100
    override val intentCode = 101
}

data object NotificationRemoved : DrinkIntent {
    override val action = "ON_NOTIFICATION_REMOVED"
    override val code = 200
    override val intentCode = 201
}

data object SkipDrink : DrinkIntent {
    override val action = "ON_SKIP_DRINK"
    override val code = 300
    override val intentCode = 301
}

data object Drink : DrinkIntent {
    override val action = "ON_DRINK"
    override val code = 400
    override val intentCode = 401
}
