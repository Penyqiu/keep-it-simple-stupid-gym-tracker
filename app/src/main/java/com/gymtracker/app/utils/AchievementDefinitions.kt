package com.gymtracker.app.utils

import androidx.compose.ui.graphics.Color
import com.gymtracker.app.R

enum class AchievementTier { BRONZE, SILVER, GOLD }

data class AchievementDefinition(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val tier: AchievementTier,
    val category: String
)

object AchievementDefinitions {
    val all = listOf(
        AchievementDefinition("workout_1",   R.string.ach_workout_1_title,   R.string.ach_workout_1_desc,   AchievementTier.BRONZE, "Workouts"),
        AchievementDefinition("workout_10",  R.string.ach_workout_10_title,  R.string.ach_workout_10_desc,  AchievementTier.SILVER, "Workouts"),
        AchievementDefinition("workout_50",  R.string.ach_workout_50_title,  R.string.ach_workout_50_desc,  AchievementTier.GOLD,   "Workouts"),
        AchievementDefinition("workout_100", R.string.ach_workout_100_title, R.string.ach_workout_100_desc, AchievementTier.GOLD,   "Workouts"),

        AchievementDefinition("streak_3",  R.string.ach_streak_3_title,  R.string.ach_streak_3_desc,  AchievementTier.BRONZE, "Streak"),
        AchievementDefinition("streak_7",  R.string.ach_streak_7_title,  R.string.ach_streak_7_desc,  AchievementTier.SILVER, "Streak"),
        AchievementDefinition("streak_30", R.string.ach_streak_30_title, R.string.ach_streak_30_desc, AchievementTier.GOLD,   "Streak"),

        AchievementDefinition("volume_10k",  R.string.ach_volume_10k_title,  R.string.ach_volume_10k_desc,  AchievementTier.BRONZE, "Volume"),
        AchievementDefinition("volume_100k", R.string.ach_volume_100k_title, R.string.ach_volume_100k_desc, AchievementTier.SILVER, "Volume"),
        AchievementDefinition("volume_1m",   R.string.ach_volume_1m_title,   R.string.ach_volume_1m_desc,   AchievementTier.GOLD,   "Volume"),

        AchievementDefinition("weight_100kg", R.string.ach_weight_100kg_title, R.string.ach_weight_100kg_desc, AchievementTier.GOLD,   "Special"),
        AchievementDefinition("weekly_5",     R.string.ach_weekly_5_title,     R.string.ach_weekly_5_desc,     AchievementTier.SILVER, "Special")
    )

    fun tierColor(tier: AchievementTier): Color = when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32)
        AchievementTier.SILVER -> Color(0xFFC0C0C0)
        AchievementTier.GOLD   -> Color(0xFFFFD700)
    }
}
