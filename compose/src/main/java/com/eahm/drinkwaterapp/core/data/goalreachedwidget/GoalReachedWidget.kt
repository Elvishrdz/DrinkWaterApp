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
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.eahm.drinkwaterapp.R
import kotlin.random.Random

class GoalReachedWidget : Service() {
    private lateinit var mWindowManager: WindowManager
    private var mFloatingWidget: View? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mFloatingWidget =
            LayoutInflater.from(this).inflate(R.layout.layout_goal_reached_widget, null)
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager

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

        mWindowManager.addView(mFloatingWidget, params)

        //region FIND VIEWS
        val root = mFloatingWidget!!.findViewById<View>(R.id.root)

        val layoutButtonCongratulations =
            mFloatingWidget!!.findViewById<View>(R.id.layoutButtonCongratulations)
        val layoutCongratulations = mFloatingWidget!!.findViewById<View>(R.id.layoutCongratulations)

        val imageViewIcon = mFloatingWidget!!.findViewById<ImageView>(R.id.imageViewIcon)
        val closeButtonCollapsed =
            mFloatingWidget!!.findViewById<ImageView>(R.id.imageButtonBCClose)


        val cardViewCongratulations: CardView =
            mFloatingWidget!!.findViewById(R.id.cardViewCongratulations)
        val closeButtonExpanded = mFloatingWidget!!.findViewById<ImageView>(R.id.imageButtonLCClose)
        val imageViewTrophy = mFloatingWidget!!.findViewById<ImageView>(R.id.imageViewTrophy)
        val textViewTitle = mFloatingWidget!!.findViewById<TextView>(R.id.textViewTitle)
        val textViewDescription = mFloatingWidget!!.findViewById<TextView>(R.id.textViewDescription)
        val textViewAppName = mFloatingWidget!!.findViewById<TextView>(R.id.textViewAppName)

        //endregion FIND VIEWS

        //region LISTENERS
        closeButtonCollapsed.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                stopSelf()
            }
        })
        closeButtonExpanded.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                //collapsedView.setVisibility(View.VISIBLE);
                //expandedView.setVisibility(View.GONE);
                closeButtonCollapsed.callOnClick()
            }
        })

        root.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.getRawX()
                        initialTouchY = event.getRawY()
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        val Xdiff = (event.getRawX() - initialTouchX).toInt()
                        val Ydiff = (event.getRawY() - initialTouchY).toInt()
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed) {
                                if (layoutButtonCongratulations.getVisibility() == View.VISIBLE) {
                                    layoutButtonCongratulations.setVisibility(View.GONE)

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

                                    mWindowManager.updateViewLayout(mFloatingWidget, params)

                                    layoutCongratulations.setVisibility(View.VISIBLE)
                                }
                            }
                        }
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.getRawX() - initialTouchX).toInt()
                        params.y = initialY + (event.getRawY() - initialTouchY).toInt()
                        mWindowManager.updateViewLayout(mFloatingWidget, params)
                        return true
                    }
                }

                v.performClick()
                return false
            }
        })

        //endregion LISTENERS
        layoutButtonCongratulations.setVisibility(View.VISIBLE)
        layoutCongratulations.setVisibility(View.GONE)

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
        when (random) {
            1 -> {
                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.blue_water))
                textViewAppName.setTextColor(getResources().getColor(R.color.white))
                imageViewTrophy.setImageResource(R.drawable.icon_trophy_01)
                textViewTitle.setTextColor(getResources().getColor(R.color.white))
                textViewTitle.setText(getString(R.string.text_congratulations))
                textViewDescription.setTextColor(getResources().getColor(R.color.black))
                textViewDescription.setText(description)
            }

            2 -> {
                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.orange))
                textViewAppName.setTextColor(getResources().getColor(R.color.white))
                imageViewTrophy.setImageResource(R.drawable.icon_trophy_02)
                textViewTitle.setTextColor(getResources().getColor(R.color.white))
                textViewTitle.setText(getString(R.string.text_congratulations))
                textViewDescription.setTextColor(getResources().getColor(R.color.black))
                textViewDescription.setText(description)
            }

            3 -> {
                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.dark_purple))
                textViewAppName.setTextColor(getResources().getColor(R.color.white))
                imageViewTrophy.setImageResource(R.drawable.icon_trophy_03)
                textViewTitle.setTextColor(getResources().getColor(R.color.white))
                textViewTitle.setText(getString(R.string.text_congratulations))
                textViewDescription.setTextColor(getResources().getColor(R.color.black))
                textViewDescription.setText(description)
            }

            4 -> {
                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.green_2))
                textViewAppName.setTextColor(getResources().getColor(R.color.white))
                imageViewTrophy.setImageResource(R.drawable.icon_trophy_04)
                textViewTitle.setTextColor(getResources().getColor(R.color.white))
                textViewTitle.setText(getString(R.string.text_congratulations))
                textViewDescription.setTextColor(getResources().getColor(R.color.black))
                textViewDescription.setText(description)
            }

            5 -> {
                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.black))
                textViewAppName.setTextColor(getResources().getColor(R.color.white))
                imageViewTrophy.setImageResource(R.drawable.icon_trophy_05)
                textViewTitle.setTextColor(getResources().getColor(R.color.white))
                textViewTitle.setText(getString(R.string.text_congratulations))
                textViewDescription.setTextColor(getResources().getColor(R.color.grey))
                textViewDescription.setText(description)
            }

            6 -> {
                imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_glass_of_water_01))
                cardViewCongratulations.setCardBackgroundColor(getResources().getColor(R.color.colorAccent))
                textViewAppName.setTextColor(getResources().getColor(R.color.white))
                imageViewTrophy.setImageResource(R.drawable.icon_trophy_06)
                textViewTitle.setTextColor(getResources().getColor(R.color.white))
                textViewTitle.setText(getString(R.string.text_congratulations))
                textViewDescription.setTextColor(getResources().getColor(R.color.white))
                textViewDescription.setText(description)
            }
        }

        //endregion CUSTOM STYLE
    }

    private val isViewCollapsed: Boolean
        get() = mFloatingWidget == null || mFloatingWidget!!.findViewById<View?>(R.id.layoutButtonCongratulations)
            .getVisibility() == View.VISIBLE

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget)
    }
}
