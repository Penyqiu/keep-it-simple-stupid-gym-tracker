package com.gymtracker.app.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class GymTrackerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = GymTrackerWidget()
}
