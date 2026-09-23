package com.daka.footprint.logic

import com.daka.footprint.data.db.entity.AchievementEntity
import com.daka.footprint.data.repository.AchievementRepository
import com.daka.footprint.data.repository.CheckinRepository
import com.daka.footprint.data.repository.DistrictRepository
import com.daka.footprint.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class AchievementChecker(
    private val checkinRepository: CheckinRepository,
    private val districtRepository: DistrictRepository,
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository
) {

    private val _unlockedEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val unlockedEvents: SharedFlow<String> = _unlockedEvents

    suspend fun checkAll() {
        val checkinCount = checkinRepository.getCount()
        val photoCount = checkinRepository.getPhotoCount()
        val revisitProgress = userRepository.getRevisitProgress()
        val activatedProvinces = districtRepository.getActivatedProvinces().map { it.name }
        val allProvinces = checkinRepository.getAllProvinces().toSet()
        val allCities = checkinRepository.getAllCities().toSet()

        checkAchievement(AchievementEntity.ID_FIRST_CHECKIN, checkinCount, 1)
        checkAchievement(AchievementEntity.ID_HUNDRED, checkinCount, 100)

        val northChinaCount = AchievementRepository.NORTH_CHINA_PROVINCES.count { it in activatedProvinces }
        achievementRepository.updateProgress(AchievementEntity.ID_NORTH_CHINA, northChinaCount)
        if (northChinaCount >= 5) unlock(AchievementEntity.ID_NORTH_CHINA)

        achievementRepository.updateProgress(AchievementEntity.ID_ALL_CHINA, activatedProvinces.size)
        if (activatedProvinces.size >= 34) unlock(AchievementEntity.ID_ALL_CHINA)

        checkAchievement(AchievementEntity.ID_REVISIT_ONE, revisitProgress, 1)
        checkAchievement(AchievementEntity.ID_REVISIT_FULL, revisitProgress, 10)

        val tier1Count = AchievementRepository.TIER1_CITIES.count { it in allCities || it in allProvinces }
        achievementRepository.updateProgress(AchievementEntity.ID_TIER1, tier1Count)
        if (tier1Count >= 4) unlock(AchievementEntity.ID_TIER1)

        checkAchievement(AchievementEntity.ID_PHOTO, photoCount, 100)

        val borderCount = AchievementRepository.BORDER_PROVINCES.count { it in allProvinces }
        achievementRepository.updateProgress(AchievementEntity.ID_BORDER, borderCount)
        if (borderCount >= 5) unlock(AchievementEntity.ID_BORDER)
    }

    private suspend fun checkAchievement(id: String, current: Int, target: Int) {
        achievementRepository.updateProgress(id, current)
        if (current >= target) unlock(id)
    }

    private suspend fun unlock(id: String) {
        if (achievementRepository.unlock(id)) {
            _unlockedEvents.emit(id)
        }
    }
}