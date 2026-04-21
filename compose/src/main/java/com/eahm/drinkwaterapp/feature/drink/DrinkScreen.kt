package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import org.koin.compose.koinInject

@PreviewScreenSizes
@Composable
internal fun DrinkScreen() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        val prefsStorage = koinInject<PrefsStorage>()
        val coroutineScope = rememberCoroutineScope()
        val firstSetup by prefsStorage.getFirstSetupDone()
            .collectAsState(
                initial = false,
                context = coroutineScope.coroutineContext,
            )

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Content(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
private fun Content(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Button(
            onClick = {}
        ) {
            Text(
                text = "Drink Water",
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrinkScreenPreview() {
    DrinkWaterAppTheme {
        Content()
    }
}