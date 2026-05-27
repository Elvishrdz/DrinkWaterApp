package com.eahm.drinkwaterapp.backup

data class CloudUser(
    val displayName: String,
    val email: String = "",
    val providerType: CloudProviderType,
)
