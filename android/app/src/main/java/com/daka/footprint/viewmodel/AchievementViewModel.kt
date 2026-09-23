package com.daka.footprint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daka.footprint.DakaApplication
import com.daka.footprint.data.db.entity.AchievementEntity
import com.daka.footprint.data.db.entity.UserProgressEntity
import kotlinx.coroutines.flow.*

class AchievementViewModel : ViewModel() {

    private val app = DakaApplication.instance
    private val achievementRepo = app.achievementRepository
    private val userRepo = app.userRepository

    val achievements: StateFlow<List<AchievementEntity>> =
        achievementRepo.getAllFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userProgress: StateFlow<UserProgressEntity?> =
        userRepo.getProgressFlow().stateIn(viewModelScope, SharingStarted.Lazily, null)

    val unlockedAchievements: StateFlow<List<AchievementEntity>> =
        achievementRepo.getUnlockedFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun groupedByCategory(): Map<String, List<AchievementEntity>> {
        return achievements.value.groupBy { it.category }
    }
}