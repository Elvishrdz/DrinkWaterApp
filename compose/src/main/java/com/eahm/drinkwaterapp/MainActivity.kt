package com.eahm.drinkwaterapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.data.systemnotifications.handleNotificationIntentOnCreate
import com.eahm.drinkwaterapp.core.data.systemnotifications.handleNotificationIntentOnNewIntent
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.ConsentRoot
import com.eahm.drinkwaterapp.feature.home.HomeScreen
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingScreen
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {

    private val viewModel = GlobalContext.get().get<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { viewModel.state.value is MainState.Startup }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntentOnCreate(intent)
        setContent {
            DrinkWaterAppTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()

                when (val data = state) {
                    MainState.Startup -> {}
                    is MainState.Ready -> when {
                        !data.consentCompleted -> ConsentRoot(
                            onConsentCompleted = viewModel::onConsentCompleted,
                        )

                        !data.onboardingCompleted -> OnboardingScreen(
                            onComplete = viewModel::onOnboardingCompleted,
                        )

                        else -> HomeScreen()
                    }
                }
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
