package com.eahm.drinkwaterapp.backup.domain.model

data class CloudUser(
    val displayName: String,
    val email: String = "",
    val providerType: CloudProviderType,
)
