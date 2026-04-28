package com.eahm.drinkwaterapp.core.data.goalreachedwidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R

@Composable
fun ButtonContent(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
) {
    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(50.dp),
            painter = painterResource(id = R.drawable.icon_trophy_06),
            contentDescription = null
        )

        Row {
            Column(
                modifier = Modifier
                    .weight(1f),
            ) {
                Text(
                    text = "Congratulations!",
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = "Has alcanzado 1500 mililitros de agua durante esta jornada. Has logrado tu meta de 2.4 litros y has mantenido hidratado tu cuerpo. Sigue asi!",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(70.dp),
                painter = painterResource(id = R.drawable.icon_glass_of_water_01),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            onClick = onClose,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Image(
                    modifier = Modifier
                        .size(12.dp),
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null
                )

                Text("Close")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonContentPreview() {
    ButtonContent(
        onClose = {},
        onDrag = { _, _ -> },
    )
}
