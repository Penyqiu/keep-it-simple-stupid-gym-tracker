package com.gymtracker.app.ui.screens.plans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.app.R
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.ui.viewmodel.PlanDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: Long,
    onStartWorkout: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PlanDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(planId) { viewModel.loadPlan(planId) }

    val plan by viewModel.plan.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.startWorkout(planId, plan?.name ?: "") { sessionId ->
                                onStartWorkout(sessionId)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.start))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercise))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            itemsIndexed(exercises) { index, exercise ->
                PlanExerciseItem(
                    exercise = exercise,
                    index = index,
                    totalCount = exercises.size,
                    onMoveUp = {
                        if (index > 0) {
                            val reordered = exercises.toMutableList().apply { add(index - 1, removeAt(index)) }
                            viewModel.reorderExercises(reordered)
                        }
                    },
                    onMoveDown = {
                        if (index < exercises.lastIndex) {
                            val reordered = exercises.toMutableList().apply { add(index + 1, removeAt(index)) }
                            viewModel.reorderExercises(reordered)
                        }
                    },
                    onRemove = { viewModel.removeExercise(exercise.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            allExercises = allExercises.filter { candidate ->
                exercises.none { it.id == candidate.id }
            },
            onAdd = { exerciseId ->
                viewModel.addExercise(exerciseId)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun PlanExerciseItem(
    exercise: ExerciseEntity,
    index: Int,
    totalCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(exercise.name) },
            supportingContent = { Text(exercise.muscleGroup) },
            leadingContent = { Text("${index + 1}.", style = MaterialTheme.typography.titleMedium) },
            trailingContent = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (index > 0) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.move_up)) },
                                leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, null) },
                                onClick = { onMoveUp(); showMenu = false }
                            )
                        }
                        if (index < totalCount - 1) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.move_down)) },
                                leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                onClick = { onMoveDown(); showMenu = false }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove)) },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { onRemove(); showMenu = false }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun AddExerciseDialog(
    allExercises: List<ExerciseEntity>,
    onAdd: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
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
                            onClick = { onAdd(exercise.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(exercise.name)
                                Text(
                                    exercise.muscleGroup,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
