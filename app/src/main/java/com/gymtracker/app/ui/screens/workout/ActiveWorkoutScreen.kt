@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.screens.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    onWorkoutFinished: (Long) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }

    val exercises by viewModel.exercises.collectAsState()
    val restTimerRunning by viewModel.restTimerRunning.collectAsState()
    val restSecondsLeft by viewModel.restSecondsLeft.collectAsState()
    val restTotalSeconds by viewModel.restTotalSeconds.collectAsState()
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
                RestTimerBanner(
                    secondsLeft = restSecondsLeft,
                    totalSeconds = restTotalSeconds,
                    onSkip = { viewModel.stopRestTimer() }
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(exercises, key = { it.exerciseId }) { workoutExercise ->
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
        AddExerciseSheet(
            allExercises = allExercises,
            onAdd = { exercise ->
                viewModel.addExerciseToWorkout(exercise.id, exercise.name)
                showAddExerciseDialog = false
            },
            onDismiss = { showAddExerciseDialog = false }
        )
    }

    if (showFinishConfirm) {
        AlertDialog(
            onDismissRequest = { showFinishConfirm = false },
            title = { Text(stringResource(R.string.finish_workout)) },
            text = { Text(stringResource(R.string.finish_workout_confirm)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.finishWorkout(onWorkoutFinished)
                    showFinishConfirm = false
                }) { Text(stringResource(R.string.finish)) }
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
private fun RestTimerBanner(secondsLeft: Int, totalSeconds: Int, onSkip: () -> Unit) {
    val progress = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds.toFloat() else 0f
    val timeText = "${secondsLeft / 60}:${String.format("%02d", secondsLeft % 60)}"

    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Timer, contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "${stringResource(R.string.rest_timer_running)}  $timeText",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.skip_rest),
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    workoutExercise: WorkoutExercise,
    onAddSet: (Double, Int) -> Unit,
    onToggleComplete: (WorkoutSetEntity, Int) -> Unit,
    onDeleteSet: (WorkoutSetEntity) -> Unit
) {
    val repsFocusRequester = remember { FocusRequester() }
    var weight by remember(workoutExercise.exerciseId) {
        mutableStateOf(workoutExercise.suggestedWeight?.let {
            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
        } ?: "")
    }
    var reps by remember { mutableStateOf("") }

    fun tryAddSet() {
        val w = weight.toDoubleOrNull() ?: return
        val r = reps.toIntOrNull() ?: return
        onAddSet(w, r)
        reps = ""
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    workoutExercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                workoutExercise.suggestedWeight?.let { suggestion ->
                    val prevMax = workoutExercise.previousSets.maxOfOrNull { it.weight }
                    val isProgression = prevMax != null && suggestion > prevMax
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isProgression) MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (isProgression) "↑ $suggestion kg" else "$suggestion kg",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isProgression) MaterialTheme.colorScheme.onTertiaryContainer
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Previous session preview (before any sets logged)
            if (workoutExercise.sets.isEmpty() && workoutExercise.previousSets.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                PreviousSessionPreview(workoutExercise.previousSets)
            }

            // Logged sets
            if (workoutExercise.sets.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                workoutExercise.sets.forEach { set ->
                    SetRow(
                        set = set,
                        previousSet = workoutExercise.previousSets.getOrNull(set.setNumber - 1),
                        onToggleComplete = { onToggleComplete(set, 90) },
                        onDelete = { onDeleteSet(set) }
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Input area
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("kg") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { repsFocusRequester.requestFocus() }
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "×",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(stringResource(R.string.reps)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { tryAddSet() }),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f).focusRequester(repsFocusRequester)
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { tryAddSet() },
                modifier = Modifier.fillMaxWidth(),
                enabled = weight.toDoubleOrNull() != null && reps.toIntOrNull() != null
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.add_set))
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
    val containerColor = if (set.isCompleted)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set number badge
            Surface(
                shape = CircleShape,
                color = if (set.isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        set.setNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (set.isCompleted) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            // Weight × reps + previous
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${set.weight} kg  ×  ${set.reps}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                previousSet?.let {
                    Text(
                        stringResource(R.string.prev_set, it.weight, it.reps),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Complete checkbox
            Checkbox(
                checked = set.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PreviousSessionPreview(previousSets: List<WorkoutSetEntity>) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                stringResource(R.string.previous_session),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                previousSets.forEach { set ->
                    Text(
                        "${set.weight} kg × ${set.reps}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddExerciseSheet(
    allExercises: List<com.gymtracker.app.data.db.entity.ExerciseEntity>,
    onAdd: (com.gymtracker.app.data.db.entity.ExerciseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, allExercises) {
        if (query.isBlank()) allExercises
        else allExercises.filter { it.name.contains(query, ignoreCase = true) }
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
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                filtered.forEach { exercise ->
                    ListItem(
                        headlineContent = { Text(exercise.name) },
                        supportingContent = { Text(exercise.muscleGroup) },
                        leadingContent = {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.clickable { onAdd(exercise) }
                    )
                    HorizontalDivider()
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
