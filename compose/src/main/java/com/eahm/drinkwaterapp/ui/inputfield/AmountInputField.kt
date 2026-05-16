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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.ui.inputfield.data.ValidationResult

@Composable
fun AmountInputField(
    label: String,
    value: String,
    onValueSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by rememberSaveable { mutableStateOf(false) }
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
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    // save only after leaving the focus
                    if (focused && !focusState.isFocused) {
                        val validationResult = validateAmountInput(inputValue)

                        if (validationResult.isValid) {
                            clearErrorMessage()
                            onValueSaved(inputValue)
                        } else {
                            errorMessage = validationResult.message
                        }
                    }

                    focused = focusState.isFocused
                },
            value = inputValue,
            onValueChange = { newValue ->
                clearErrorMessage()

                inputValue = newValue
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

fun validateAmountInput(value: String): ValidationResult {
    val trimmed = value.trim()

    if (trimmed.isEmpty()) {
        return ValidationResult(false, "Field can not be empty")
    }

    val amount = trimmed.toFloatOrNull()
        ?: return ValidationResult(false, "Provide a valid number")

    return validateAmount(amount, maxValue = 99999f)
}

fun validateAmount(
    amount: Float,
    minValue: Float = 0.0f,
    maxValue: Float = Float.MAX_VALUE,
): ValidationResult {
    if (amount < minValue) {
        return ValidationResult(false, "Value must be greater than $minValue")
    }

    if (amount > maxValue) {
        return ValidationResult(false, "Value must be less than $maxValue")
    }

    return ValidationResult(true, "")
}

fun Float.toDigitString(): String {
    return if (this % 1f == 0f) {
        toInt().toString()
    } else {
        this.toString()
    }
}
