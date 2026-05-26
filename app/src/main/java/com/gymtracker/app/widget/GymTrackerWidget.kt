package com.gymtracker.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.glance.action.clickable
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.gymtracker.app.MainActivity
import com.gymtracker.app.data.repository.WorkoutRepository
import com.gymtracker.app.ui.theme.DarkColorScheme
import com.gymtracker.app.ui.theme.LightColorScheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.ZoneId

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun workoutRepository(): WorkoutRepository
}

class GymTrackerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repo = entryPoint.workoutRepository()
        val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val thisWeekCount = repo.getWorkoutDaysInRange(weekAgo)
            .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct().size
        val total = repo.getTotalWorkoutCount()

        provideContent {
            GlanceTheme(colors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)) {
                WidgetContent(thisWeekCount = thisWeekCount, total = total)
            }
        }
    }

    companion object {
        suspend fun updateAll(context: Context) {
            GymTrackerWidget().updateAll(context)
        }
    }
}

@Composable
private fun WidgetContent(thisWeekCount: Int, total: Int) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "GYM Tracker",
            style = TextStyle(color = GlanceTheme.colors.primary)
        )
        Spacer(GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = thisWeekCount.toString(),
                    style = TextStyle(color = GlanceTheme.colors.onBackground)
                )
                Text(
                    text = "This week",
                    style = TextStyle(color = GlanceTheme.colors.onBackground)
                )
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = total.toString(),
                    style = TextStyle(color = GlanceTheme.colors.onBackground)
                )
                Text(
                    text = "Total",
                    style = TextStyle(color = GlanceTheme.colors.onBackground)
                )
            }
        }
    }
}
