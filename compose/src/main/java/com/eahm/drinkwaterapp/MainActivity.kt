package com.eahm.drinkwaterapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.eahm.drinkwaterapp.core.data.systemnotifications.handleNotificationIntentOnCreate
import com.eahm.drinkwaterapp.core.data.systemnotifications.handleNotificationIntentOnNewIntent
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.home.HomeScreen
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {

    private val viewModel = GlobalContext.get().get<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntentOnCreate(intent)
        setContent {
            DrinkWaterAppTheme {
                HomeScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntentOnNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}
