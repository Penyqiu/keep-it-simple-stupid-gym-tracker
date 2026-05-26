@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.gymtracker.app.data.db.model.SessionWithStats
import com.gymtracker.app.ui.components.AdBannerView
import com.gymtracker.app.ui.components.EmptyState
import com.gymtracker.app.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartWorkout: (Long) -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentSessions by viewModel.recentSessions.collectAsState()
    val allPlans by viewModel.allPlans.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showPlanPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshStats() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenAchievements) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = stringResource(R.string.achievements))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        },
        bottomBar = { AdBannerView(Modifier.fillMaxWidth()) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.total_workouts),
                        value = uiState.totalWorkouts.toString(),
                        icon = Icons.Default.FitnessCenter
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.current_streak),
                        value = "${uiState.currentStreak}d",
                        icon = Icons.Default.Whatshot
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.startQuickWorkout(onStartWorkout) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.quick_start))
                    }
                    OutlinedButton(
                        onClick = { showPlanPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.from_plan))
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.recent_workouts),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (recentSessions.isEmpty()) {
                item { EmptyState(stringResource(R.string.no_workouts_yet)) }
            } else {
                items(recentSessions) { session ->
                    RecentSessionCard(session = session)
                }
            }
        }
    }

    if (showPlanPicker) {
        AlertDialog(
            onDismissRequest = { showPlanPicker = false },
            title = { Text(stringResource(R.string.select_plan)) },
            text = {
                if (allPlans.isEmpty()) {
                    Text(stringResource(R.string.no_plans_yet))
                } else {
                    Column {
                        allPlans.forEach { plan ->
                            TextButton(
                                onClick = {
                                    viewModel.startPlanWorkout(plan.id, plan.name, onStartWorkout)
                                    showPlanPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(plan.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlanPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecentSessionCard(session: SessionWithStats) {
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val durationMin = session.finishedAt?.let { (it - session.startedAt) / 60_000 } ?: 0L

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.planName ?: stringResource(R.string.quick_workout),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${dateFormat.format(Date(session.startedAt))} · ${timeFormat.format(Date(session.startedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(icon = Icons.Default.Timer, text = "$durationMin min")
                    StatChip(icon = Icons.Default.FitnessCenter,
                        text = "${session.exerciseCount} ${stringResource(R.string.exercises_short)}")
                    if (session.volume > 0) {
                        StatChip(icon = Icons.Default.Scale,
                            text = "${"%.0f".format(session.volume)} kg")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(icon, contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
