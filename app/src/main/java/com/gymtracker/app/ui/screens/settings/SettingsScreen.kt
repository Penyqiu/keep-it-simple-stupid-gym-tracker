@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.screens.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.gymtracker.app.R
import com.gymtracker.app.ui.viewmodel.BackupViewModel
import com.gymtracker.app.ui.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

private val WEEKDAYS = listOf(
    1 to R.string.day_mon,
    2 to R.string.day_tue,
    3 to R.string.day_wed,
    4 to R.string.day_thu,
    5 to R.string.day_fri,
    6 to R.string.day_sat,
    7 to R.string.day_sun
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    backupViewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val darkTheme by settingsViewModel.darkTheme.collectAsState()
    val useKg by settingsViewModel.useKg.collectAsState()
    val restTimerSeconds by settingsViewModel.restTimerSeconds.collectAsState()
    val reminderEnabled by settingsViewModel.reminderEnabled.collectAsState()
    val reminderHour by settingsViewModel.reminderHour.collectAsState()
    val reminderMinute by settingsViewModel.reminderMinute.collectAsState()
    val reminderDays by settingsViewModel.reminderDays.collectAsState()
    var showPlatesCalculator by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) backupViewModel.exportBackup(context, uri) { success ->
            Toast.makeText(context, if (success) context.getString(R.string.backup_success)
                else context.getString(R.string.restore_error), Toast.LENGTH_SHORT).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) { pendingImportUri = uri; showImportConfirm = true }
    }

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
                SectionHeader(stringResource(R.string.appearance))
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

            item { HorizontalDivider(); SectionHeader(stringResource(R.string.units), topPadding = true) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.use_kg)) },
                    supportingContent = { Text(if (useKg) "kg" else "lbs") },
                    trailingContent = {
                        Switch(checked = useKg, onCheckedChange = { settingsViewModel.setUseKg(it) })
                    }
                )
            }

            item { HorizontalDivider(); SectionHeader(stringResource(R.string.rest_timer), topPadding = true) }
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.rest_duration))
                        Text("${restTimerSeconds / 60}:${String.format("%02d", restTimerSeconds % 60)}")
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

            item { HorizontalDivider(); SectionHeader(stringResource(R.string.reminders), topPadding = true) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.reminders_enable)) },
                    supportingContent = { Text(stringResource(R.string.reminders_enable_desc)) },
                    trailingContent = {
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { settingsViewModel.setReminderEnabled(it) }
                        )
                    }
                )
            }
            if (reminderEnabled) {
                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.reminder_time)) },
                        supportingContent = { Text(String.format("%02d:%02d", reminderHour, reminderMinute)) },
                        leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        modifier = Modifier.clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> settingsViewModel.setReminderTime(hour, minute) },
                                reminderHour,
                                reminderMinute,
                                true
                            ).show()
                        }
                    )
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            stringResource(R.string.reminder_days),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WEEKDAYS.forEach { (day, labelRes) ->
                                val selected = day in reminderDays
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val newDays = if (selected) reminderDays - day else reminderDays + day
                                        settingsViewModel.setReminderDays(newDays)
                                    },
                                    label = { Text(stringResource(labelRes)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(); SectionHeader(stringResource(R.string.tools), topPadding = true) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.plates_calculator)) },
                    supportingContent = { Text(stringResource(R.string.plates_calculator_desc)) },
                    leadingContent = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                    modifier = Modifier.clickable { showPlatesCalculator = true }
                )
            }

            item { HorizontalDivider(); SectionHeader(stringResource(R.string.backup_restore), topPadding = true) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.export_backup)) },
                    supportingContent = { Text(stringResource(R.string.export_backup_desc)) },
                    leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                    modifier = Modifier.clickable { exportLauncher.launch("gymtracker_backup.json") }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.import_backup)) },
                    supportingContent = { Text(stringResource(R.string.import_backup_desc)) },
                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier.clickable { importLauncher.launch(arrayOf("application/json", "*/*")) }
                )
            }
        }
    }

    if (showPlatesCalculator) {
        PlatesCalculatorDialog(useKg = useKg, onDismiss = { showPlatesCalculator = false })
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text(stringResource(R.string.import_warning_title)) },
            text = { Text(stringResource(R.string.import_warning_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirm = false
                        pendingImportUri?.let { uri ->
                            backupViewModel.importBackup(context, uri) { success ->
                                Toast.makeText(context,
                                    if (success) context.getString(R.string.restore_success)
                                    else context.getString(R.string.restore_error),
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.import_backup)) }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String, topPadding: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = if (topPadding) 8.dp else 0.dp)
    )
}
