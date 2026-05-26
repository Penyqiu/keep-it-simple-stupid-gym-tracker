@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.app.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.app.R
import com.gymtracker.app.data.db.entity.WorkoutSessionEntity
import com.gymtracker.app.ui.components.EmptyState
import com.gymtracker.app.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onRepeatWorkout: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.list), stringResource(R.string.calendar))
    val context = LocalContext.current

    val sessionsByDate by viewModel.sessionsByDate.collectAsState()
    val workoutsByDate by viewModel.workoutsByDate.collectAsState()

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) viewModel.exportToCsv(context, uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_history)) },
                actions = {
                    IconButton(onClick = { csvLauncher.launch("workouts.csv") }) {
                        Icon(Icons.Default.Download, contentDescription = stringResource(R.string.export_csv))
                    }
                }
            )
        }
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
                0 -> HistoryListTab(
                    sessionsByDate = sessionsByDate,
                    onRepeat = { session -> viewModel.repeatWorkout(session, onRepeatWorkout) },
                    onDelete = viewModel::deleteSession
                )
                1 -> CalendarTab(workoutsByDate = workoutsByDate)
            }
        }
    }
}

@Composable
private fun HistoryListTab(
    sessionsByDate: Map<String, List<WorkoutSessionEntity>>,
    onRepeat: (WorkoutSessionEntity) -> Unit,
    onDelete: (WorkoutSessionEntity) -> Unit
) {
    if (sessionsByDate.isEmpty()) {
        EmptyState(stringResource(R.string.no_workouts_yet))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sessionsByDate.forEach { (date, sessions) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(sessions) { session ->
                    SessionHistoryCard(
                        session = session,
                        onRepeat = { onRepeat(session) },
                        onDelete = { onDelete(session) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(
    session: WorkoutSessionEntity,
    onRepeat: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(session.planName ?: stringResource(R.string.quick_workout)) },
            supportingContent = {
                val duration = session.finishedAt?.let { finish ->
                    val minutes = ((finish - session.startedAt) / 60_000).toInt()
                    "${minutes} min"
                } ?: ""
                Text("${timeFormat.format(Date(session.startedAt))} · $duration")
            },
            leadingContent = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
            trailingContent = {
                Row {
                    IconButton(onClick = onRepeat) {
                        Icon(Icons.Default.Replay, contentDescription = stringResource(R.string.repeat))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                }
            }
        )
    }
}

@Composable
private fun CalendarTab(workoutsByDate: Map<LocalDate, List<String?>>) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth = currentMonth.minusMonths(1)
                selectedDate = null
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }
            val monthName = currentMonth.month
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                .replaceFirstChar(Char::uppercaseChar)
            Text(
                "$monthName ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                currentMonth = currentMonth.plusMonths(1)
                selectedDate = null
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
        Spacer(Modifier.height(8.dp))
        val daysOfWeek = listOf(
            stringResource(R.string.day_mon), stringResource(R.string.day_tue),
            stringResource(R.string.day_wed), stringResource(R.string.day_thu),
            stringResource(R.string.day_fri), stringResource(R.string.day_sat),
            stringResource(R.string.day_sun)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value - 1
        val totalDays = currentMonth.lengthOfMonth()
        val rows = (firstDayOfWeek + totalDays + 6) / 7
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - firstDayOfWeek + 1
                    if (dayNum in 1..totalDays) {
                        val date = currentMonth.atDay(dayNum)
                        val isWorkout = date in workoutsByDate
                        val isToday = date == LocalDate.now()
                        val isSelected = date == selectedDate
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            shape = MaterialTheme.shapes.small,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.tertiary
                                isWorkout -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.secondaryContainer
                                else -> Color.Transparent
                            },
                            onClick = { selectedDate = if (isSelected) null else date }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        isSelected || isWorkout -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        val selectedWorkouts = selectedDate?.let { workoutsByDate[it] }
        if (selectedDate != null) {
            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = selectedDate!!.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    if (selectedWorkouts.isNullOrEmpty()) {
                        Text(
                            text = stringResource(R.string.no_workouts_on_day),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        selectedWorkouts.forEach { name ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    name ?: stringResource(R.string.quick_workout),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
