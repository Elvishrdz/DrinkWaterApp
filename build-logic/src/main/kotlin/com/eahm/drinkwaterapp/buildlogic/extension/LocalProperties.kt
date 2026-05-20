package com.eahm.drinkwaterapp.buildlogic.extension

import org.gradle.api.Project
import java.util.Properties

internal val Project.localProperties: Properties
    get() = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) load(file.inputStream())
    }
