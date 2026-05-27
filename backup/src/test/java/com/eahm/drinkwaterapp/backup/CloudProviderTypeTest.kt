package com.eahm.drinkwaterapp.backup

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CloudProviderTypeTest {

    @Test
    fun `fromId returns GOOGLE_DRIVE for its id`() {
        assertEquals(CloudProviderType.GOOGLE_DRIVE, CloudProviderType.fromId("google_drive"))
    }

    @Test
    fun `fromId returns DROPBOX for its id`() {
        assertEquals(CloudProviderType.DROPBOX, CloudProviderType.fromId("dropbox"))
    }

    @Test
    fun `fromId returns null for unknown id`() {
        assertNull(CloudProviderType.fromId("unknown_provider"))
    }

    @Test
    fun `fromId returns null for empty string`() {
        assertNull(CloudProviderType.fromId(""))
    }

    @Test
    fun `each provider has a non-blank displayName`() {
        CloudProviderType.entries.forEach { type ->
            assert(type.displayName.isNotBlank()) {
                "${type.name} has blank displayName"
            }
        }
    }

    @Test
    fun `each provider id round-trips through fromId`() {
        CloudProviderType.entries.forEach { type ->
            assertEquals(type, CloudProviderType.fromId(type.id))
        }
    }
}
