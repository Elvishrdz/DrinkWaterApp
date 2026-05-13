package com.eahm.drinkwaterapp.system.service.goalcelebration

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.OverlayConfigurations
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.getStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.extension.getSerializableCelebrationStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.extension.shareBitmap
import com.eahm.drinkwaterapp.system.service.goalcelebration.ui.DragConstraints
import com.eahm.drinkwaterapp.system.service.goalcelebration.ui.GoalCelebrationScreen

class GoalCelebrationOverlay : Service(),
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private var mWindowManager: WindowManager? = null
    private var widgetView: View? = null

    private val style = mutableStateOf<CelebrationStyle?>(null)
    private val goalMlState = mutableFloatStateOf(2400f)
    private val drinkCountState = mutableIntStateOf(1)
    private val streakDaysState = mutableIntStateOf(1)

    private val _savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()

    override val lifecycle = _lifecycleRegistry
    override val savedStateRegistry = _savedStateRegistryController.savedStateRegistry
    override val viewModelStore = _viewModelStore

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            style.value = it.getSerializableCelebrationStyle(OVERLAY_STYLE)
            goalMlState.floatValue = it.getFloatExtra(OVERLAY_GOAL_ML, goalMlState.floatValue)
            drinkCountState.intValue = it.getIntExtra(OVERLAY_DRINK_COUNT, drinkCountState.intValue)
            streakDaysState.intValue = it.getIntExtra(OVERLAY_STREAK_DAYS, streakDaysState.intValue)
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        widgetView = LayoutInflater.from(this).inflate(R.layout.layout_goal_reached_widget, null)

        widgetView?.let { view ->
            view.setViewTreeLifecycleOwner(this)
            view.setViewTreeSavedStateRegistryOwner(this)
            view.setViewTreeViewModelStoreOwner(this)

            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            mWindowManager?.addView(widgetView, OverlayConfigurations.getLayoutParamsForOverlay())

            val composeView = view.findViewById<ComposeView>(R.id.composeView)

            composeView.apply {
                setContent {
                    DrinkWaterAppTheme {
                        GoalCelebrationScreen(
                            variant = getStyle(style.value),
                            goalMl = goalMlState.floatValue,
                            drinkCount = drinkCountState.intValue,
                            streakDays = streakDaysState.intValue,
                            draggable = true,
                            dragConstraints = DragConstraints(
                                top = 0.dp,
                                bottom = 30.dp,
                                start = 7.dp,
                                end = 7.dp,
                            ),
                            showScrim = false,
                            onClose = ::stopSelf,
                            onShare = ::shareGoalCelebration,
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        widgetView?.let { mWindowManager?.removeView(it) }
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewModelStore.clear()
    }

    private fun shareGoalCelebration(bitmap: Bitmap) {
        val liters = "%.1f".format(goalMlState.floatValue / 1000f)
        val shareText =
            "I just hit my daily water goal! ${liters}L in ${drinkCountState.intValue} glasses — day ${streakDaysState.intValue} streak!\n\nGet DrinkWater: https://drinkwaterapp.page.link/isR1"

        shareBitmap(
            bitmap = bitmap,
            shareText = shareText,
            fileName = "goal_progress.png",
        )
    }

    companion object {

        const val OVERLAY_STYLE = "OVERLAY_STYLE"
        const val OVERLAY_GOAL_ML = "OVERLAY_GOAL_ML"
        const val OVERLAY_DRINK_COUNT = "OVERLAY_DRINK_COUNT"
        const val OVERLAY_STREAK_DAYS = "OVERLAY_STREAK_DAYS"
    }
}
