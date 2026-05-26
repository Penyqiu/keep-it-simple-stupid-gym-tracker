package com.gymtracker.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gymtracker.app.R
import com.gymtracker.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val darkTheme by settingsViewModel.darkTheme.collectAsState()
    val useKg by settingsViewModel.useKg.collectAsState()
    val restTimerSeconds by settingsViewModel.restTimerSeconds.collectAsState()
    var showPlatesCalculator by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.dark_theme)) },
                    trailingContent = {
                        Switch(
                            checked = darkTheme ?: false,
                            onCheckedChange = { settingsViewModel.setDarkTheme(it) }
                        )
                    }
                )
            }
            item {
                HorizontalDivider()
                Text(stringResource(R.string.units), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.use_kg)) },
                    supportingContent = { Text(if (useKg) "kg" else "lbs") },
                    trailingContent = {
                        Switch(
                            checked = useKg,
                            onCheckedChange = { settingsViewModel.setUseKg(it) }
                        )
                    }
                )
            }
            item {
                HorizontalDivider()
                Text(stringResource(R.string.rest_timer), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.rest_duration))
                        Text("$restTimerSeconds s")
                    }
                    Slider(
                        value = restTimerSeconds.toFloat(),
                        onValueChange = { settingsViewModel.setRestTimerSeconds(it.toInt()) },
                        valueRange = 15f..300f,
                        steps = 57,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                HorizontalDivider()
                Text(stringResource(R.string.tools), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.plates_calculator)) },
                    supportingContent = { Text(stringResource(R.string.plates_calculator_desc)) },
                    leadingContent = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                    modifier = Modifier.clickable { showPlatesCalculator = true }
                )
            }
        }
    }

    if (showPlatesCalculator) {
        PlatesCalculatorDialog(
            useKg = useKg,
            onDismiss = { showPlatesCalculator = false }
        )
    }
}

