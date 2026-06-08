package com.eahm.drinkwaterapp.backup.domain.model

enum class CloudProviderType(
    val id: String,
    val displayName: String,
) {
    GOOGLE_DRIVE("google_drive", "Google Drive"),
    DROPBOX("dropbox", "Dropbox");

    companion object {
        fun fromId(id: String): CloudProviderType? = entries.firstOrNull { it.id == id }
    }
}
