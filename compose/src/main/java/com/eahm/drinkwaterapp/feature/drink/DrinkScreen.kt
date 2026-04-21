package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

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

        val database = koinInject<AppDatabase>()
        val dao = database.getHistoryDao()
        val records by dao.getAll().collectAsStateWithLifecycle(
            initialValue = emptyList(),
        )

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Content(
                modifier = Modifier.padding(innerPadding),
                records = records,
                onDrink = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val record = HistoryEntity(
                                amount = 200f,
                                status = Status.IN_APP,
                            )

                            dao.insertAll(record)
                        }
                    }
                },
                onDelete = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            dao.delete(it)
                        }
                    }
                }
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
private fun Content(
    records: List<HistoryEntity>,
    modifier: Modifier = Modifier,
    onDrink: () -> Unit,
    onDelete: (HistoryEntity) -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        val alarm = koinInject<DrinkAlarmManager>()
        var time by remember { mutableStateOf("") }
        Text(
            text = "is active? ${alarm.active}",
        )

        alarm.interval?.let {
            Text(
                text = "interval: ${it.inWholeMilliseconds}",
            )
        }
        Text(
            text = "time: $time",
        )

        Button(
            onClick = {
                alarm.start(context)
            },
        ) {
            Text(
                text = "Start Alarm",
            )
        }

        Button(
            onClick = {
                alarm.stop()
            },
        ) {
            Text(
                text = "Stop Alarm",
            )
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(scope) {
            scope.launch {
                while (true) {
                    delay(1.seconds)
                    time = alarm.remainingTime()
                }
            }
        }


        Button(
            onClick = onDrink,
        ) {
            Text(
                text = "Drink Water",
            )
        }

        LazyColumn {
            items(records) { item ->
                Row(modifier = Modifier.animateItem()) {
                    Column {
                        Text(
                            text = item.id.takeLast(7),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = item.amount.toString(),
                        )
                        Text(
                            text = item.timestamp.toString(),
                        )
                    }
                    Button(
                        onClick = { onDelete(item) },
                    ) {
                        Text(
                            text = "Delete",
                        )
                    }
                }
            }
        }
    }
}

@PreviewScreenSizes
@Preview
@Composable
fun DrinkScreenPreview() {
    DrinkWaterAppTheme {
        Content(
            records = recordsMock,
            onDrink = {},
            onDelete = {},
        )
    }
}

val localDateTimeMock = LocalDateTime(
    year = 2023,
    month = 1,
    day = 1,
    hour = 10,
    minute = 5,
)

val recordsMock = listOf(
    HistoryEntity(
        amount = 200f,
        status = Status.IN_APP,
        timestamp = localDateTimeMock,
    ),
    HistoryEntity(
        amount = 200f,
        status = Status.IN_APP,
        timestamp = localDateTimeMock,
    )
)
