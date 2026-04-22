package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

@Composable
internal fun Adaptive(
    modifier: Modifier = Modifier,
    stats: @Composable () -> Unit,
    drink: @Composable () -> Unit,
    history: @Composable () -> Unit,
) {
    val layoutType = getLayoutType()

    when (layoutType) {
        LayoutType.TABLE_TOP -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    stats()
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    history()
                    drink()
                }
            }
        }

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_EXPANDED,
            -> {
            Row(
                modifier = modifier,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    stats()
                    history()
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    drink()
                }
            }
        }

        LayoutType.PORTRAIT -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                stats()
                drink()
                history()
            }
        }
    }
}