package com.eahm.drinkwaterapp.feature.age

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
internal fun AgeCollectionRoot(
    onComplete: () -> Unit,
    viewModel: AgeCollectionViewModel,
) {
    val completed by viewModel.completed.collectAsStateWithLifecycle()

    LaunchedEffect(completed) {
        if (completed) onComplete()
    }

    AgeCollectionScreen(onContinue = viewModel::submitSelection)
}

@Composable
internal fun AgeCollectionScreen(
    onContinue: (AgeBracketOption) -> Unit,
) {
    var selected by remember { mutableStateOf<AgeBracketOption?>(null) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.age_collection_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.age_collection_description),
                color = DwInk400,
            )

            AgeBracketOption.entries.forEach { option ->
                AgeOptionRow(
                    option = option,
                    selected = selected == option,
                    onClick = { selected = option },
                )
            }

            DwPrimaryButton(
                text = stringResource(R.string.age_collection_continue),
                enabled = selected != null,
                onClick = { selected?.let(onContinue) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AgeOptionRow(
    option: AgeBracketOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Text(
                text = stringResource(option.labelRes),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgeCollectionScreenPreview() {
    DrinkWaterAppTheme {
        AgeCollectionScreen(onContinue = {})
    }
}
