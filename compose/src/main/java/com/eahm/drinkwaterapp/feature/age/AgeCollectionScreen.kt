package com.eahm.drinkwaterapp.feature.age

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import java.util.Calendar

private const val MIN_PLAUSIBLE_AGE = 0
private const val MAX_PLAUSIBLE_AGE = 120
private const val YEAR_INPUT_LENGTH = 4

@Composable
internal fun AgeCollectionRoot(
    onComplete: () -> Unit,
    viewModel: AgeCollectionViewModel,
) {
    val completed by viewModel.completed.collectAsStateWithLifecycle()

    LaunchedEffect(completed) {
        if (completed) onComplete()
    }

    AgeCollectionScreen(onSubmitBirthYear = viewModel::submitBirthYear)
}

@Suppress("Indentation")
@Composable
internal fun AgeCollectionScreen(
    onSubmitBirthYear: (Int) -> Unit,
    currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
) {
    var input by remember { mutableStateOf("") }
    val birthYear = input.toIntOrNull()
    val isValid = birthYear != null &&
            (currentYear - birthYear) in MIN_PLAUSIBLE_AGE..MAX_PLAUSIBLE_AGE

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "What's your year of birth?",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "We use your age only to keep ads appropriate for your age and to comply " +
                        "with the law. It is never shared, used for analytics, or used to target ads.",
                color = DwInk400,
            )
            OutlinedTextField(
                value = input,
                onValueChange = { new ->
                    input = new.filter(Char::isDigit).take(YEAR_INPUT_LENGTH)
                },
                label = { Text("Year of birth") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            DwPrimaryButton(
                text = "Continue",
                enabled = isValid,
                onClick = { birthYear?.let(onSubmitBirthYear) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgeCollectionScreenPreview() {
    DrinkWaterAppTheme {
        AgeCollectionScreen(onSubmitBirthYear = {}, currentYear = 2026)
    }
}
