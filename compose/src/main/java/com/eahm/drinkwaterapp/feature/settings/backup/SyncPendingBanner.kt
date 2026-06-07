package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

/**
 * Animated banner shown when a sync is pending (data changed, waiting for connectivity).
 */
@Composable
internal fun SyncPendingBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DwTintWater)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = DwIcons.Repeat,
                contentDescription = null,
                tint = DwWater,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.sync_pending_banner),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwWaterDeep,
            )
        }
    }
}

@DwPreview
@Composable
private fun SyncPendingBannerPreview() {
    DrinkWaterAppTheme {
        SyncPendingBanner(visible = true)
    }
}
