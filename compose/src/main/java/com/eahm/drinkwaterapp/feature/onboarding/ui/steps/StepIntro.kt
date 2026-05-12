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
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

@Composable
internal fun StepIntro(
    step: Int,
    onNext: () -> Unit,
) {
    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = "Let's build a great habit",
        subtitle = "In a few quick steps, we'll set up your hydration goal and reminders.",
        art = { OnbArtCircle(icon = DwIcons.Drop) },
        footer = {
            DwPrimaryButtonAnimated(
                text = "Get started",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IntroFeatureCards()
        }
    }
}

@Composable
private fun IntroFeatureCards() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IntroFeatureCard(
            icon = DwIcons.Drop,
            cardBackground = DwTintWater,
            iconBackground = DwWater,
            title = "Your daily goal",
            description = "Tailored to your body and lifestyle.",
        )
        IntroFeatureCard(
            icon = DwIcons.Bell,
            cardBackground = DwTintMagenta,
            iconBackground = DwMagenta,
            title = "Smart reminders",
            description = "Gentle nudges so you never miss a sip.",
        )
        IntroFeatureCard(
            icon = DwIcons.Wave,
            cardBackground = DwTintWater,
            iconBackground = DwWaterDeep,
            title = "Track your streak",
            description = "Build a healthy habit, one day at a time.",
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@DwPreview
@Composable
private fun StepIntroPreview() {
    DrinkWaterAppTheme {
        StepIntro(
            step = 1,
            onNext = {},
        )
    }
}
