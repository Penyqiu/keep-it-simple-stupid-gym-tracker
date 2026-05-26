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
                        value = uiState.totalWorkouts.toString()
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.current_streak),
                        value = "${uiState.currentStreak}d"
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
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecentSessionCard(session: com.gymtracker.app.data.db.entity.WorkoutSessionEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(session.planName ?: stringResource(R.string.quick_workout)) },
            supportingContent = { Text(dateFormat.format(Date(session.startedAt))) },
            leadingContent = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
        )
    }
}
