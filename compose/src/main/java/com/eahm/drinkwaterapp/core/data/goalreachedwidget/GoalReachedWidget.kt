package com.eahm.drinkwaterapp.core.data.goalreachedwidget

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

class GoalReachedWidget : Service(),
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private lateinit var mWindowManager: WindowManager
    private var widgetView: View? = null
    private lateinit var params: WindowManager.LayoutParams

    private val style = mutableStateOf<CelebrationStyle?>(null)
    private val goalMlState = mutableFloatStateOf(2400f)
    private val drinkCountState = mutableIntStateOf(1)
    private val streakDaysState = mutableIntStateOf(1)

    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    private val _viewModelStore = ViewModelStore()

    override val savedStateRegistry: SavedStateRegistry =
        _savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle = _lifecycleRegistry
    override val viewModelStore: ViewModelStore = _viewModelStore

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            style.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializableExtra("style", CelebrationStyle::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializableExtra("style") as? CelebrationStyle
            }

            goalMlState.floatValue = it.getFloatExtra("goalMl", goalMlState.floatValue)
            drinkCountState.intValue = it.getIntExtra("drinkCount", drinkCountState.intValue)
            streakDaysState.intValue = it.getIntExtra("streakDays", streakDaysState.intValue)
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        widgetView = LayoutInflater.from(this)
            .inflate(R.layout.layout_goal_reached_widget, null)

        widgetView?.let {
            it.setViewTreeLifecycleOwner(this)
            it.setViewTreeSavedStateRegistryOwner(this)
            it.setViewTreeViewModelStoreOwner(this)
        }

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
        } else {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.x = 0
        params.y = 0

        mWindowManager.addView(widgetView, params)

        val composeView = widgetView!!.findViewById<ComposeView>(R.id.composeView)

        composeView.apply {
            setContent {
                DrinkWaterAppTheme {
                    GoalCelebrationOverlay(
                        variant = getStyle(style.value),
                        goalMl = goalMlState.floatValue,
                        drinkCount = drinkCountState.intValue,
                        streakDays = streakDaysState.intValue,
                        onClose = { stopSelf() },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (widgetView != null) mWindowManager.removeView(widgetView)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewModelStore.clear()
    }
}
