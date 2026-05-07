package com.eahm.drinkwaterapp.core.data.notificationpermission

import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notificationPermissionModule = module {
    singleOf(::NotificationPermissionObserver) bind AppInitializer::class
}
