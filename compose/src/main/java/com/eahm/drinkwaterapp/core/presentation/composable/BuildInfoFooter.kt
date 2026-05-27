package com.eahm.drinkwaterapp.core.presentation.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono

@Composable
fun BuildInfoFooter(
    appVersion: String,
    buildVariant: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = StringBuilder().apply {
            append(appVersion)
            if (buildVariant.isNotEmpty()) {
                append(" · ")
                append(buildVariant)
            }
        }.toString(),
        fontFamily = JetBrainsMono,
        fontSize = 11.sp,
        color = DwInk400,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
    )
}

@Preview
@Composable
private fun BuildInfoFooterPreview() {
    DrinkWaterAppTheme {
        BuildInfoFooter(
            appVersion = "1.0.0",
            buildVariant = "Debug",
        )
    }
}