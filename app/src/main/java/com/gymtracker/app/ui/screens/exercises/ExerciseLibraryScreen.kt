@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.screens.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.app.R
import com.gymtracker.app.ui.components.EmptyState
import com.gymtracker.app.ui.viewmodel.ExerciseDetail
import com.gymtracker.app.ui.viewmodel.ExerciseLibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseLibraryViewModel = hiltViewModel()
) {
    val exercises by viewModel.filteredExercises.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()
    val selectedGroup by viewModel.selectedMuscleGroup.collectAsState()
    val selectedDetail by viewModel.selectedDetail.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_exercises)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercise))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGroup == null,
                        onClick = { viewModel.selectMuscleGroup(null) },
                        label = { Text(stringResource(R.string.all)) }
                    )
                }
                items(muscleGroups) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { viewModel.selectMuscleGroup(group) },
                        label = { Text(group) }
                    )
                }
            }
            if (exercises.isEmpty()) {
                EmptyState(stringResource(R.string.no_exercises))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(exercises) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = { Text(exercise.muscleGroup) },
                            leadingContent = {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                if (exercise.isCustom) {
                                    IconButton(onClick = { viewModel.deleteExercise(exercise) }) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                                    }
                                } else {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            modifier = Modifier.clickable { viewModel.showDetail(exercise) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    selectedDetail?.let { detail ->
        ExerciseDetailSheet(
            detail = detail,
            onDismiss = { viewModel.clearDetail() },
            onDelete = if (detail.exercise.isCustom) {
                { viewModel.deleteExercise(detail.exercise); viewModel.clearDetail() }
            } else null
        )
    }

    if (showAddDialog) {
        AddCustomExerciseDialog(
            muscleGroups = muscleGroups,
            onConfirm = { name, group ->
                viewModel.addCustomExercise(name, group)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun ExerciseDetailSheet(
    detail: ExerciseDetail,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(detail.exercise.name, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(detail.exercise.muscleGroup) }
                    )
                }
                if (detail.exercise.isCustom && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatBox(
                    label = stringResource(R.string.personal_record),
                    value = if (detail.personalRecord != null) "${detail.personalRecord} kg"
                            else stringResource(R.string.no_record_yet),
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = stringResource(R.string.sessions_done),
                    value = detail.sessionCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            if (detail.exercise.description.isNotBlank()) {
                HorizontalDivider()
                Text(
                    text = detail.exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AddCustomExerciseDialog(
    muscleGroups: List<String>,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf(muscleGroups.firstOrNull() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_custom_exercise)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.exercise_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = muscleGroup,
                        onValueChange = { muscleGroup = it },
                        label = { Text(stringResource(R.string.muscle_group)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        muscleGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = { muscleGroup = group; expanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && muscleGroup.isNotBlank()) onConfirm(name.trim(), muscleGroup) },
                enabled = name.isNotBlank() && muscleGroup.isNotBlank()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
