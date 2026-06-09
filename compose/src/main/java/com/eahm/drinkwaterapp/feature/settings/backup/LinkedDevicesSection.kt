package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.backup.domain.model.SeenDevice
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shows the list of devices that have contributed to the shared backup.
 * Visible only when the user is connected to a cloud provider.
 */
@Composable
internal fun LinkedDevicesSection(
    devices: List<SeenDevice>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Collapsed by default — tap the header to reveal the device list.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.sync_linked_devices_title) + " (${devices.size})",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = TextUnit(0.10f, TextUnitType.Em),
                modifier = Modifier.weight(1f),
            )
            @Suppress("MagicNumber")
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (expanded) 90f else 0f),
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .outlineBorderStyle()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                if (devices.isEmpty()) {
                    Text(
                        text = stringResource(R.string.sync_linked_devices_empty),
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                } else {
                    devices.forEach { device ->
                        DeviceRow(device = device)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: SeenDevice) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = DwIcons.ShieldCheck,
            contentDescription = null,
            tint = if (device.isCurrentDevice) DwMagenta else DwInk400,
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.deviceModel.ifBlank { device.deviceId },
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val subLabel = if (device.isCurrentDevice) {
                stringResource(R.string.sync_device_this_device)
            } else {
                val formatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    .format(Date(device.lastSeenAt))
                stringResource(R.string.sync_device_last_seen, formatted)
            }
            Text(
                text = subLabel,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(0.dp))
    }
}

@DwPreview
@Composable
private fun LinkedDevicesSectionPreview() {
    DrinkWaterAppTheme {
        LinkedDevicesSection(
            devices = listOf(
                SeenDevice(
                    deviceId = "abc",
                    deviceModel = "Pixel 8 Pro",
                    lastSeenAt = System.currentTimeMillis(),
                    isCurrentDevice = true,
                ),
                SeenDevice(
                    deviceId = "def",
                    deviceModel = "Galaxy S24",
                    lastSeenAt = System.currentTimeMillis() - 3_600_000L,
                    isCurrentDevice = false,
                ),
            ),
        )
    }
}
