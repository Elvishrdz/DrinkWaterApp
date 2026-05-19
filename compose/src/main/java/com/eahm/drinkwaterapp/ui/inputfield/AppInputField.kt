package com.eahm.drinkwaterapp.ui.inputfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AppInputField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    onHasError: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var inputValue by rememberSaveable { mutableStateOf(value) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val clearErrorMessage: () -> Unit = {
        if (errorMessage.isNotEmpty()) {
            onHasError(false)
            errorMessage = ""
        }
    }

    val onError: (String) -> Unit = { message ->
        onHasError(true)
        errorMessage = message
    }

    val validateInput: (String) -> Unit = { newValue ->
        val validationResult = validateAmountInput(newValue)

        if (validationResult.isValid) {
            clearErrorMessage()
            onValueChanged(newValue)
        } else {
            onError(validationResult.resolveMessage(context))
        }

        if (newValue.isEmpty()) {
            // emit empty values
            onValueChanged(newValue)
        }

        inputValue = newValue
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = inputValue,
            onValueChange = validateInput,
            label = { Text(label) },
            isError = errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(true)
                    // validate input and if empty clear error
                    if (inputValue.isEmpty()) {
                        clearErrorMessage()
                    }
                },
            ),
            singleLine = true,
            enabled = enabled,
        )

        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .padding(top = 4.dp, start = 16.dp)
                .alpha(if (errorMessage.isEmpty()) 0f else 1f)
        )
    }
}
