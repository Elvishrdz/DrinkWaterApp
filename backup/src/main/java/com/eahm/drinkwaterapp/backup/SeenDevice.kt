package com.eahm.drinkwaterapp.backup

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SeenDevice(
    val deviceId: String,
    val deviceModel: String,
    val lastSeenAt: Long,
    val isCurrentDevice: Boolean = false,
)
