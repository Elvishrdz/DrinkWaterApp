package com.eahm.drinkwaterapp.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import java.util.Locale

private val SUPPORTED_LOCALES = listOf("en-US", "es-ES", "de-DE")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguageDialog(
    current: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingTag by remember { mutableStateOf(current) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_row_language),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(8.dp))

            LanguageOption(
                label = stringResource(R.string.settings_language_system),
                selected = pendingTag.isEmpty(),
                onClick = { pendingTag = "" },
            )

            SUPPORTED_LOCALES.forEach { tag ->
                LanguageOption(
                    label = Locale.forLanguageTag(tag)
                        .getDisplayLanguage(Locale.forLanguageTag(tag))
                        .replaceFirstChar { it.uppercase() },
                    selected = pendingTag == tag,
                    onClick = { pendingTag = tag },
                )
            }

            Spacer(Modifier.height(24.dp))

            DwPrimaryButton(
                text = stringResource(R.string.settings_language_apply),
                onClick = {
                    onLanguageSelected(pendingTag)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            DwSecondaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
            ),
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (selected) MaterialTheme.colorScheme.onSurface else DwInk500,
        )
    }
}
