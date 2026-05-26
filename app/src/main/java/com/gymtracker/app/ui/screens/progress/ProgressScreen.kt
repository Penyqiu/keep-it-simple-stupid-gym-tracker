@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.screens.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.gymtracker.app.data.db.entity.BodyWeightEntity
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.ui.components.EmptyState
import com.gymtracker.app.ui.viewmodel.DetailedStats
import com.gymtracker.app.ui.viewmodel.ExerciseProgress
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
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val exercisesWithHistory by viewModel.exercisesWithHistory.collectAsState()
    val selectedExerciseId by viewModel.selectedExerciseId.collectAsState()
    val selectedProgress by viewModel.selectedExerciseProgress.collectAsState()
    val bodyWeightEntries by viewModel.bodyWeightEntries.collectAsState()
    val detailedStats by viewModel.detailedStats.collectAsState()
    var showBodyWeightDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.exercise_tab), stringResource(R.string.stats))

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_progress)) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                        Text(title, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
            when (selectedTab) {
                0 -> ExerciseProgressTab(
                    exercisesWithHistory = exercisesWithHistory,
                    selectedExerciseId = selectedExerciseId,
                    selectedProgress = selectedProgress,
                    bodyWeightEntries = bodyWeightEntries,
                    onShowBodyWeightDialog = { showBodyWeightDialog = true },
                    onSelectExercise = viewModel::selectExercise,
                    onDeleteBodyWeight = viewModel::deleteBodyWeightEntry
                )
                1 -> StatsTab(stats = detailedStats)
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
                ) { Text(stringResource(R.string.add)) }
            },
            dismissButton = {
                TextButton(onClick = { showBodyWeightDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun ExerciseProgressTab(
    exercisesWithHistory: List<ExerciseEntity>,
    selectedExerciseId: Long?,
    selectedProgress: ExerciseProgress?,
    bodyWeightEntries: List<BodyWeightEntity>,
    onShowBodyWeightDialog: () -> Unit,
    onSelectExercise: (Long) -> Unit,
    onDeleteBodyWeight: (BodyWeightEntity) -> Unit
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val bwModelProducer = remember { CartesianChartModelProducer() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedProgress) {
        val weights = selectedProgress?.sets?.map { it.weight.toFloat() } ?: return@LaunchedEffect
        if (weights.isNotEmpty()) scope.launch { modelProducer.runTransaction { lineSeries { series(weights) } } }
    }
    LaunchedEffect(bodyWeightEntries) {
        val weights = bodyWeightEntries.map { it.weight.toFloat() }.reversed()
        if (weights.isNotEmpty()) scope.launch { bwModelProducer.runTransaction { lineSeries { series(weights) } } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(stringResource(R.string.exercise_progress),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (exercisesWithHistory.isEmpty()) {
            item { Text(stringResource(R.string.no_history_yet), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            item {
                var showPicker by remember { mutableStateOf(false) }
                val selectedName = exercisesWithHistory.find { it.id == selectedExerciseId }?.name
                OutlinedTextField(
                    value = selectedName ?: stringResource(R.string.select_exercise),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                if (showPicker) {
                    ExercisePickerSheet(
                        exercises = exercisesWithHistory,
                        onSelect = { onSelectExercise(it); showPicker = false },
                        onDismiss = { showPicker = false }
                    )
                }
            }
            selectedProgress?.let { progress ->
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.personal_record), style = MaterialTheme.typography.labelLarge)
                            Text("${progress.personalRecord} kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.estimated_1rm), style = MaterialTheme.typography.labelLarge)
                            Text("${"%.1f".format(progress.estimatedOneRepMax)} kg",
                                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    CartesianChartHost(
                        chart = rememberCartesianChart(rememberLineCartesianLayer(),
                            startAxis = rememberStartAxis(), bottomAxis = rememberBottomAxis()),
                        modelProducer = modelProducer,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.body_weight),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onShowBodyWeightDialog) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
        if (bodyWeightEntries.isNotEmpty()) {
            item {
                CartesianChartHost(
                    chart = rememberCartesianChart(rememberLineCartesianLayer(),
                        startAxis = rememberStartAxis(), bottomAxis = rememberBottomAxis()),
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
                        IconButton(onClick = { onDeleteBodyWeight(entry) }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StatsTab(stats: DetailedStats?) {
    if (stats == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatsCard(stringResource(R.string.total_workouts), stats.totalWorkouts.toString(), Modifier.weight(1f))
                StatsCard(stringResource(R.string.longest_streak),
                    "${stats.longestStreak} d", Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatsCard(stringResource(R.string.total_volume),
                    "${"%.0f".format(stats.totalVolume)} kg", Modifier.weight(1f))
                StatsCard(stringResource(R.string.favourite_day),
                    stats.favouriteDayOfWeek ?: "—", Modifier.weight(1f))
            }
        }
        if (stats.topExercises.isNotEmpty()) {
            item {
                Text(stringResource(R.string.top_exercises),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            items(stats.topExercises) { (name, count) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(name) },
                        trailingContent = {
                            Text("$count ${stringResource(R.string.times)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerSheet(
    exercises: List<com.gymtracker.app.data.db.entity.ExerciseEntity>,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, exercises) {
        if (query.isBlank()) exercises else exercises.filter { it.name.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                filtered.forEach { exercise ->
                    ListItem(
                        headlineContent = { Text(exercise.name) },
                        supportingContent = { Text(exercise.muscleGroup) },
                        modifier = Modifier.clickable { onSelect(exercise.id) }
                    )
                    HorizontalDivider()
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatsCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
