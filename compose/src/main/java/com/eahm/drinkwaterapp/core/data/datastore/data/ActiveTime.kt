package com.eahm.drinkwaterapp.core.data.datastore.data

import kotlinx.serialization.Serializable

@Serializable
data class ActiveTime(
    val hour: Int,
    val minute: Int,
)
