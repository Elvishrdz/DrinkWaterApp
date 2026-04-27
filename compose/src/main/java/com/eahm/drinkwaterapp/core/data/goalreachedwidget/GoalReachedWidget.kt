package com.eahm.drinkwaterapp.core.data.goalreachedwidget

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
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
import kotlin.random.Random

class GoalReachedWidget : Service(),
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private lateinit var mWindowManager: WindowManager
    private var widgetView: View? = null
    private lateinit var params: WindowManager.LayoutParams

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

        // onHide
//        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        mWindowManager.addView(widgetView, params)

        val composeView = widgetView!!.findViewById<ComposeView>(R.id.composeView)
        val root = widgetView!!.findViewById<View>(R.id.root)

        composeView.apply {
            setContent {
                ButtonContent()
            }
        }


        //region LISTENERS
//        closeButtonCollapsed.setOnClickListener { stopSelf() }
//        closeButtonExpanded.setOnClickListener { closeButtonCollapsed.callOnClick() }

        root.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        val Xdiff = (event.rawX - initialTouchX).toInt()
                        val Ydiff = (event.rawY - initialTouchY).toInt()
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed) {
//                                if (layoutButtonCongratulations.getVisibility() == View.VISIBLE) {
//                                    layoutButtonCongratulations.setVisibility(View.GONE)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    params = WindowManager.LayoutParams(
                                        WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                        PixelFormat.TRANSLUCENT
                                    )
                                } else {
                                    params = WindowManager.LayoutParams(
                                        WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.TYPE_PHONE,
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                        PixelFormat.TRANSLUCENT
                                    )
                                }

                                params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                                params.x = 0
                                params.y = 0

                                mWindowManager.updateViewLayout(widgetView, params)

//                                    layoutCongratulations.setVisibility(View.VISIBLE)
//                                }
                            }
                        }
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        mWindowManager.updateViewLayout(widgetView, params)
                        return true
                    }
                }

                v.performClick()
                return false
            }
        })

        //endregion LISTENERS
//        layoutButtonCongratulations.setVisibility(View.VISIBLE)
//        layoutCongratulations.setVisibility(View.GONE)

        //region CUSTOM STYLE
        // TODO:
        val description = "Congratulations!"
//        val description =
//            getString(R.string.text_congratulations_01) + " " + Utils().getFinalProgressString(
//                false,
//                this
//            ) + " " + getString(R.string.text_congratulations_02) + " " + Utils().getGoalString(
//                false,
//                this
//            ) + " " + getString(R.string.text_congratulations_03)

        val random = Random(8).nextInt(1, 6)
//        when (random) {
//            1 -> {
//                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
//                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.blue_water))
//                textViewAppName.setTextColor(getResources().getColor(R.color.white))
//                imageViewTrophy.setImageResource(R.drawable.icon_trophy_01)
//                textViewTitle.setTextColor(getResources().getColor(R.color.white))
//                textViewTitle.setText(getString(R.string.text_congratulations))
//                textViewDescription.setTextColor(getResources().getColor(R.color.black))
//                textViewDescription.setText(description)
//            }
//
//            2 -> {
//                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
//                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.orange))
//                textViewAppName.setTextColor(getResources().getColor(R.color.white))
//                imageViewTrophy.setImageResource(R.drawable.icon_trophy_02)
//                textViewTitle.setTextColor(getResources().getColor(R.color.white))
//                textViewTitle.setText(getString(R.string.text_congratulations))
//                textViewDescription.setTextColor(getResources().getColor(R.color.black))
//                textViewDescription.setText(description)
//            }
//
//            3 -> {
//                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
//                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.dark_purple))
//                textViewAppName.setTextColor(getResources().getColor(R.color.white))
//                imageViewTrophy.setImageResource(R.drawable.icon_trophy_03)
//                textViewTitle.setTextColor(getResources().getColor(R.color.white))
//                textViewTitle.setText(getString(R.string.text_congratulations))
//                textViewDescription.setTextColor(getResources().getColor(R.color.black))
//                textViewDescription.setText(description)
//            }
//
//            4 -> {
//                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
//                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.green_2))
//                textViewAppName.setTextColor(getResources().getColor(R.color.white))
//                imageViewTrophy.setImageResource(R.drawable.icon_trophy_04)
//                textViewTitle.setTextColor(getResources().getColor(R.color.white))
//                textViewTitle.setText(getString(R.string.text_congratulations))
//                textViewDescription.setTextColor(getResources().getColor(R.color.black))
//                textViewDescription.setText(description)
//            }
//
//            5 -> {
//                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
//                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.black))
//                textViewAppName.setTextColor(getResources().getColor(R.color.white))
//                imageViewTrophy.setImageResource(R.drawable.icon_trophy_05)
//                textViewTitle.setTextColor(getResources().getColor(R.color.white))
//                textViewTitle.setText(getString(R.string.text_congratulations))
//                textViewDescription.setTextColor(getResources().getColor(R.color.grey))
//                textViewDescription.setText(description)
//            }
//
//            6 -> {
//                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
//                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.colorAccent))
//                textViewAppName.setTextColor(getResources().getColor(R.color.white))
//                imageViewTrophy.setImageResource(R.drawable.icon_trophy_06)
//                textViewTitle.setTextColor(getResources().getColor(R.color.white))
//                textViewTitle.setText(getString(R.string.text_congratulations))
//                textViewDescription.setTextColor(getResources().getColor(R.color.white))
//                textViewDescription.setText(description)
//            }
//        }

        //endregion CUSTOM STYLE
    }

    private val isViewCollapsed: Boolean
        get() = widgetView == null
            //|| widgetView!!.findViewById<View?>(R.id.layoutButtonCongratulations).getVisibility() == View.VISIBLE

    override fun onDestroy() {
        super.onDestroy()
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewModelStore.clear()
        if (widgetView != null) mWindowManager.removeView(widgetView)
    }
}
