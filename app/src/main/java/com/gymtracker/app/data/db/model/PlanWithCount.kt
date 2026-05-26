package com.gymtracker.app.data.db.model

import androidx.room.Embedded
import com.gymtracker.app.data.db.entity.WorkoutPlanEntity

data class PlanWithCount(
    @Embedded val plan: WorkoutPlanEntity,
    val exerciseCount: Int
)
