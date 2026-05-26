package com.eahm.drinkwaterapp.feature.consent.legal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.config.PrivacyPolicyTermsOfServiceConfig
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
internal fun LegalHeaderContent(
    showStepCounter: Boolean,
    centered: Boolean,
    showNewBadge: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start
    val horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    val horizontalPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(LayoutDirection.Ltr),
        end = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
    )

    Column(
        modifier = modifier
            .padding(horizontalPadding),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(contentPadding.calculateTopPadding()))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.30f),
                    shape = RoundedCornerShape(20.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DwIcons.ShieldCheck,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        if (showStepCounter) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .width(30.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White),
                )
                Box(
                    Modifier
                        .width(22.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.30f)),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.consent_legal_step),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.14.em,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
        } else {
            Spacer(Modifier.height(16.dp))
        }

        Text(
            text = stringResource(R.string.consent_legal_title),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.consent_legal_updated_at,
                    PrivacyPolicyTermsOfServiceConfig.effectiveDate.formatted(),
                ),
                color = Color.White.copy(alpha = 0.80f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 0.04.em,
            )
            // Badge only for returning users re-accepting a new policy version
            if (showNewBadge) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFB300),
                            shape = RoundedCornerShape(999.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.consent_legal_new_badge),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
    }
}

private fun LocalDate.formatted(): String {
    return toJavaLocalDate().format(
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
    )
}

private class CenteredPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview(widthDp = 360)
@Composable
private fun LegalHeaderContentPreview(
    @PreviewParameter(CenteredPreviewProvider::class) centered: Boolean,
) {
    LegalHeaderContent(
        modifier = Modifier
            .background(ConsentDefaults.legalGradient),
        showStepCounter = true,
        showNewBadge = true,
        centered = centered
    )
}
