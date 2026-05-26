package com.gymtracker.app.ui.screens.workout

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.app.R
import com.gymtracker.app.data.db.entity.WorkoutSetEntity
import com.gymtracker.app.ui.viewmodel.WorkoutExercise
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    sessionId: Long,
    onFinish: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }

    val exercises by viewModel.exercises.collectAsState()
    val restTimerRunning by viewModel.restTimerRunning.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showFinishConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.active_workout)) },
                actions = {
                    IconButton(onClick = { showAddExerciseDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercise))
                    }
                    Button(
                        onClick = { showFinishConfirm = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.finish))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (restTimerRunning) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.rest_timer_running),
                            style = MaterialTheme.typography.labelLarge
                        )
                        TextButton(onClick = { viewModel.stopRestTimer() }) {
                            Text(stringResource(R.string.skip_rest))
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(exercises) { workoutExercise ->
                    ExerciseCard(
                        workoutExercise = workoutExercise,
                        onAddSet = { weight, reps ->
                            viewModel.addSet(
                                workoutExercise.exerciseId,
                                workoutExercise.exerciseName,
                                weight, reps
                            )
                        },
                        onToggleComplete = { set, restSeconds ->
                            viewModel.toggleSetComplete(set, restSeconds)
                        },
                        onDeleteSet = viewModel::deleteSet
                    )
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        var searchQuery by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text(stringResource(R.string.add_exercise)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    val filtered = allExercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    Column(Modifier.heightIn(max = 300.dp)) {
                        filtered.forEach { exercise ->
                            TextButton(
                                onClick = {
                                    viewModel.addExerciseToWorkout(exercise.id, exercise.name)
                                    showAddExerciseDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(exercise.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showFinishConfirm) {
        AlertDialog(
            onDismissRequest = { showFinishConfirm = false },
            title = { Text(stringResource(R.string.finish_workout)) },
            text = { Text(stringResource(R.string.finish_workout_confirm)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.finishWorkout(onFinish)
                    showFinishConfirm = false
                }) {
                    Text(stringResource(R.string.finish))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ExerciseCard(
    workoutExercise: WorkoutExercise,
    onAddSet: (Double, Int) -> Unit,
    onToggleComplete: (WorkoutSetEntity, Int) -> Unit,
    onDeleteSet: (WorkoutSetEntity) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(workoutExercise.exerciseName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            workoutExercise.sets.forEach { set ->
                SetRow(
                    set = set,
                    previousSet = workoutExercise.previousSets.getOrNull(set.setNumber - 1),
                    onToggleComplete = { onToggleComplete(set, 90) },
                    onDelete = { onDeleteSet(set) }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(stringResource(R.string.reps)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        val w = weight.toDoubleOrNull() ?: return@IconButton
                        val r = reps.toIntOrNull() ?: return@IconButton
                        onAddSet(w, r)
                        weight = ""
                        reps = ""
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_set))
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    set: WorkoutSetEntity,
    previousSet: WorkoutSetEntity?,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${stringResource(R.string.set)} ${set.setNumber}: ${set.weight}kg × ${set.reps}",
                style = MaterialTheme.typography.bodyMedium
            )
            previousSet?.let {
                Text(
                    text = "Prev: ${it.weight}kg × ${it.reps}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Checkbox(checked = set.isCompleted, onCheckedChange = { onToggleComplete() })
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
