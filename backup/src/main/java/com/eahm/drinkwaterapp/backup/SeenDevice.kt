package com.eahm.drinkwaterapp.backup

import kotlinx.serialization.Serializable

@Serializable
data class SeenDevice(
    val deviceId: String,
    val deviceModel: String,
    val lastSeenAt: Long,
    val isCurrentDevice: Boolean = false,
)
