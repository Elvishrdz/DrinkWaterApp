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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.ui.inputfield.data.ValidationResult

@Composable
fun TextInputField(
    label: String,
    value: String,
    onValueSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
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
                        val validationResult = validateTextInput(inputValue)

                        if (validationResult.isValid) {
                            clearErrorMessage()
                            onValueSaved(inputValue)
                        } else {
                            errorMessage = validationResult.resolveMessage(context)
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

private fun validateTextInput(value: String): ValidationResult {
    val trimmed = value.trim()

    if (trimmed.isEmpty()) {
        return ValidationResult(false, R.string.validation_error_empty)
    }

    return validateText(
        value = trimmed,
        minLength = 3,
        maxLength = 15,
    )
}

@Suppress("SameParameterValue")
fun validateText(
    value: String,
    minLength: Int = 0,
    maxLength: Int = Int.MAX_VALUE,
): ValidationResult {
    if (value.length < minLength) {
        return ValidationResult(false, R.string.validation_error_min_length, minLength.toString())
    }

    if (value.length > maxLength) {
        return ValidationResult(false, R.string.validation_error_max_length, maxLength.toString())
    }

    return ValidationResult(true)
}
