package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle

@Composable
internal fun stepIntro(
    onNext: () -> Unit,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Drop) },
    title = stringResource(R.string.onb_intro_title),
    subtitle = stringResource(R.string.onb_intro_subtitle),
    footer = {
        DwPrimaryButtonAnimated(
            text = stringResource(R.string.onb_intro_cta_text),
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IntroFeatureCards()
        }
    },
)

@Composable
private fun IntroFeatureCards() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IntroFeatureCard(
            icon = DwIcons.Drop,
            cardBackground = DwTintWater,
            iconBackground = DwWater,
            title = stringResource(R.string.onb_intro_card_goal_title),
            description = stringResource(R.string.onb_intro_card_goal_description),
        )
        IntroFeatureCard(
            icon = DwIcons.Bell,
            cardBackground = DwTintMagenta,
            iconBackground = DwMagenta,
            title = stringResource(R.string.onb_intro_card_reminders_title),
            description = stringResource(R.string.onb_intro_card_reminders_description),
        )
        IntroFeatureCard(
            icon = DwIcons.Wave,
            cardBackground = DwTintWater,
            iconBackground = DwWaterDeep,
            title = stringResource(R.string.onb_intro_card_stats_title),
            description = stringResource(R.string.onb_intro_card_stats_description),
        )
    }
}

@Composable
private fun IntroFeatureCard(
    icon: ImageVector,
    cardBackground: Color,
    iconBackground: Color,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = DwInk500,
            )
        }
    }
}

@Composable
private fun StepIntroBasePreview() {
    OnboardingStepBasePreview(
        config = stepIntro(
            onNext = {},
        ),
    )
}

@DwPreview
@Composable
private fun StepIntroPreview() {
    DrinkWaterAppTheme {
        StepIntroBasePreview()
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepIntroLandscapePreview() {
    DrinkWaterAppTheme {
        StepIntroBasePreview()
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepIntroTabletPreview() {
    DrinkWaterAppTheme {
        StepIntroBasePreview()
    }
}
