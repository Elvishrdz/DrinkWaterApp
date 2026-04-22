package com.eahm.drinkwaterapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eahm.drinkwaterapp.core.data.systemnotifications.handleNotificationIntentOnCreate
import com.eahm.drinkwaterapp.core.data.systemnotifications.handleNotificationIntentOnNewIntent
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.DrinkRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntentOnCreate(intent)
        setContent {
            DrinkWaterAppTheme {
                DrinkRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntentOnNewIntent(intent)
    }
}
