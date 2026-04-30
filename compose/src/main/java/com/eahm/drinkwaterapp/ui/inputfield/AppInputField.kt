package com.eahm.drinkwaterapp.ui.inputfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AppInputField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputValue by rememberSaveable { mutableStateOf(value) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val clearErrorMessage: () -> Unit = {
        if (errorMessage.isNotEmpty()) {
            errorMessage = ""
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = inputValue,
            onValueChange = { newValue ->
                val validationResult = validateAmountInput(newValue)

                if (validationResult.isValid) {
                    clearErrorMessage()
                } else {
                    errorMessage = validationResult.message
                }

                inputValue = newValue
                onValueChanged(newValue)
            },
            label = { Text(label) },
            isError = errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
            ),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}
