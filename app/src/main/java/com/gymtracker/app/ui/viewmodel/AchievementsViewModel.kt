package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.repository.AchievementRepository
import com.gymtracker.app.data.repository.WorkoutRepository
import com.gymtracker.app.utils.AchievementDefinition
import com.gymtracker.app.utils.AchievementDefinitions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementState(
    val definition: AchievementDefinition,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val achievements: StateFlow<List<AchievementState>> =
        achievementRepository.getAllAchievements().map { unlocked ->
            val unlockedMap = unlocked.associateBy { it.id }
            AchievementDefinitions.all.map { definition ->
                AchievementState(
                    definition = definition,
                    isUnlocked = definition.id in unlockedMap,
                    unlockedAt = unlockedMap[definition.id]?.unlockedAt
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unlockedCount = achievementRepository.getUnlockedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalCount = AchievementDefinitions.all.size

    init {
        viewModelScope.launch {
            workoutRepository.checkAndUnlockAchievements(achievementRepository)
        }
    }
}
