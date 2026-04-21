package com.eahm.drinkwaterapp

import android.app.Application
import com.eahm.drinkwaterapp.di.startDI

internal class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startDI(this@BaseApplication)
    }
}
