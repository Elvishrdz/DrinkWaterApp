package com.eahm.drinkwaterapp.core.data.systemnotifications.model

enum class NotificationIntentOrigin {
    // called when the app is in background and a notification is clicked
    OnCreate,

    // called when the app is in foreground and a notification is clicked
    OnNewIntent,
}