package com.eahm.drinkwaterapp.core.domain.extension

fun Float.sanitizeZeroDecimals(): String {
    return if (this % 1f == 0f) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}
