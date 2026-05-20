package com.eahm.drinkwaterapp.buildlogic.domain

internal enum class BuildVariant(val buildTypeName: String) {
    DEBUG("debug"),
    STAGING("staging"),
    QA("qa"),
    RELEASE("release"),
}