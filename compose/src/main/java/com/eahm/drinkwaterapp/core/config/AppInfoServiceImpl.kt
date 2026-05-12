package com.eahm.drinkwaterapp.core.config

import com.eahm.drinkwaterapp.BuildConfig
import org.koin.dsl.module

val appInfoModule = module {
    single<AppInfoService> { AppInfoServiceImpl() }
}

internal class AppInfoServiceImpl : AppInfoService {

    override val versionName: String = BuildConfig.VERSION_NAME
    override val buildVariant: String = BuildConfig.BUILD_TYPE
}
