package com.eahm.drinkwaterapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.di.startDI
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import org.koin.core.context.GlobalContext

internal class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        restoreLocale()

        NotificationService.setup(this)

        startDI(this@BaseApplication)

        GlobalContext.get().getAll<AppInitializer>().forEach { it.initialize() }
    }

    private fun restoreLocale() {
        val tag = getSharedPreferences("locale_prefs", MODE_PRIVATE)
            .getString("locale_tag", null)
        if (tag.isNullOrEmpty()) return
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
