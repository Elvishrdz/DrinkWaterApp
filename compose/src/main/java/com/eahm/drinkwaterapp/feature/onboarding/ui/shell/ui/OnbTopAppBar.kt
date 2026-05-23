package com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.LocalOnboardingProgress

private val backButtonSizeDp = 48.dp

@Composable
internal fun OnbTopAppBar(
    step: Int,
    total: Int,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // Use the animated value provided by OnboardingScreen (persists across step changes).
    // Fall back to a static fraction in previews where no provider exists.
    val progress = LocalOnboardingProgress.current ?: (step.toFloat() / total.toFloat())

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(
                    modifier = Modifier.size(backButtonSizeDp),
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = OnbIcons.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                Spacer(Modifier.size(backButtonSizeDp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.onb_step_counter, step, total),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
        Spacer(Modifier.height(7.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(horizontal = 7.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.30f),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1565C0)
@Composable
private fun OnbTopAppBarWithBackPreview() {
    MaterialTheme {
        OnbTopAppBar(
            step = 3,
            total = 6,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1565C0)
@Composable
private fun OnbTopAppBarNoBackPreview() {
    MaterialTheme {
        OnbTopAppBar(
            step = 1,
            total = 6,
            onBack = null,
        )
    }
}
