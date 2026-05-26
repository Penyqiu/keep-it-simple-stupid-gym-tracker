package com.gymtracker.app.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.app.R
import com.gymtracker.app.ui.components.EmptyState
import com.gymtracker.app.ui.viewmodel.ProgressViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val exercisesWithHistory by viewModel.exercisesWithHistory.collectAsState()
    val selectedExerciseId by viewModel.selectedExerciseId.collectAsState()
    val selectedProgress by viewModel.selectedExerciseProgress.collectAsState()
    val bodyWeightEntries by viewModel.bodyWeightEntries.collectAsState()
    var showBodyWeightDialog by remember { mutableStateOf(false) }

    val modelProducer = remember { CartesianChartModelProducer() }
    val bwModelProducer = remember { CartesianChartModelProducer() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedProgress) {
        val progress = selectedProgress ?: return@LaunchedEffect
        val weights = progress.sets.map { it.weight.toFloat() }
        if (weights.isNotEmpty()) {
            scope.launch {
                modelProducer.runTransaction {
                    lineSeries { series(weights) }
                }
            }
        }
    }

    LaunchedEffect(bodyWeightEntries) {
        val weights = bodyWeightEntries.map { it.weight.toFloat() }.reversed()
        if (weights.isNotEmpty()) {
            scope.launch {
                bwModelProducer.runTransaction {
                    lineSeries { series(weights) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_progress)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(stringResource(R.string.exercise_progress), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (exercisesWithHistory.isEmpty()) {
                item { Text(stringResource(R.string.no_history_yet), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                item {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = exercisesWithHistory.find { it.id == selectedExerciseId }?.name ?: stringResource(R.string.select_exercise),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            exercisesWithHistory.forEach { exercise ->
                                DropdownMenuItem(
                                    text = { Text(exercise.name) },
                                    onClick = { viewModel.selectExercise(exercise.id); expanded = false }
                                )
                            }
                        }
                    }
                }
                selectedProgress?.let { progress ->
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.personal_record), style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "${progress.personalRecord} kg",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.estimated_1rm), style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "${"%.1f".format(progress.estimatedOneRepMax)} kg",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    item {
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis()
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.body_weight), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showBodyWeightDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
            if (bodyWeightEntries.isNotEmpty()) {
                item {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        ),
                        modelProducer = bwModelProducer,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
                items(bodyWeightEntries.take(5)) { entry ->
                    ListItem(
                        headlineContent = { Text("${entry.weight} kg") },
                        supportingContent = {
                            Text(java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                .format(java.util.Date(entry.recordedAt)))
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteBodyWeightEntry(entry) }) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showBodyWeightDialog) {
        var weightInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBodyWeightDialog = false },
            title = { Text(stringResource(R.string.add_body_weight)) },
            text = {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text(stringResource(R.string.weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        weightInput.toDoubleOrNull()?.let { viewModel.addBodyWeight(it) }
                        showBodyWeightDialog = false
                    },
                    enabled = weightInput.toDoubleOrNull() != null
                ) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBodyWeightDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
