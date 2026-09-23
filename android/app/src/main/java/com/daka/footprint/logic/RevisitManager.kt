package com.daka.footprint.logic

import android.util.Log
import com.daka.footprint.data.db.entity.AchievementEntity
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.data.db.entity.DistrictEntity
import com.daka.footprint.data.repository.AchievementRepository
import com.daka.footprint.data.repository.CheckinRepository
import com.daka.footprint.data.repository.DistrictRepository
import com.daka.footprint.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class RevisitManager(
    private val checkinRepository: CheckinRepository,
    private val districtRepository: DistrictRepository,
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository
) {

    companion object {
        private const val TAG = "RevisitManager"
        const val FENCE_EXIT_THRESHOLD_MS = 60_000L
    }

    sealed class RevisitEvent {
        data class ContentUnlocked(val checkinId: String) : RevisitEvent()
        data class ContentLocked(val checkinId: String) : RevisitEvent()
        data class SelfRevisitIncremented(val checkinId: String, val count: Int) : RevisitEvent()
        data class CityFirstRevisit(val cityName: String, val totalProgress: Int) : RevisitEvent()
        data class FullAchievementUnlocked(val totalProgress: Int) : RevisitEvent()
        data class CityFrozen(val cityName: String) : RevisitEvent()
    }

    private val _events = MutableSharedFlow<RevisitEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<RevisitEvent> = _events

    suspend fun onEnterFence(checkinId: String) {
        val checkin = checkinRepository.getCheckinById(checkinId) ?: run {
            Log.w(TAG, "打卡点不存在: $checkinId")
            return
        }

        // 1. 解锁打卡内容
        _events.emit(RevisitEvent.ContentUnlocked(checkinId))

        // 无限制模式不处理重游逻辑
        if (checkin.unlockMode != CheckinPointEntity.UNLOCK_MODE_LIMIT) return

        // 2. 防重复触发：短时间内重复进入不计入
        val now = System.currentTimeMillis()
        if (now - checkin.lastEnterTime < FENCE_EXIT_THRESHOLD_MS) {
            Log.d(TAG, "防重复触发，跳过")
            return
        }
        checkinRepository.updateLastEnterTime(checkinId, now)

        // 3. 打卡点自身重游次数+1（仅展示）
        checkinRepository.incrementRevisitCount(checkinId)
        val updated = checkinRepository.getCheckinById(checkinId)
        updated?.let { _events.emit(RevisitEvent.SelfRevisitIncremented(checkinId, it.revisitCount)) }

        // 4. 城市级进度校验
        val city = districtRepository.getByCode(checkin.cityCode)
        if (city == null) {
            Log.w(TAG, "城市不存在: ${checkin.cityCode}")
            return
        }
        if (city.hasRevisitContributed || city.isRevisitFrozen) {
            Log.d(TAG, "城市已贡献或已冻结，不处理总进度")
            return
        }

        // 5. 首次贡献：总进度+1，标记城市
        userRepository.incrementRevisitProgress()
        districtRepository.markAsContributed(checkin.cityCode)

        val totalProgress = userRepository.getRevisitProgress()
        if (totalProgress >= 1) {
            achievementRepository.unlock(AchievementEntity.ID_REVISIT_ONE)
        }

        // 6. 校验完整成就
        if (totalProgress >= UserRepository.MAX_REVISIT_PROGRESS) {
            achievementRepository.unlock(AchievementEntity.ID_REVISIT_FULL)
            _events.emit(RevisitEvent.FullAchievementUnlocked(totalProgress))
        } else {
            _events.emit(RevisitEvent.CityFirstRevisit(checkin.city, totalProgress))
        }
    }

    suspend fun onExitFence(checkinId: String) {
        val checkin = checkinRepository.getCheckinById(checkinId) ?: return
        if (checkin.unlockMode == CheckinPointEntity.UNLOCK_MODE_LIMIT) {
            _events.emit(RevisitEvent.ContentLocked(checkinId))
        }
    }

    suspend fun forceUnlock(checkinId: String): Boolean {
        val checkin = checkinRepository.getCheckinById(checkinId) ?: return false
        if (checkin.unlockMode != CheckinPointEntity.UNLOCK_MODE_LIMIT) return false

        // 切换为无限制模式，删除对应地理围栏
        checkinRepository.forceUnlock(checkinId)

        // 标记所属城市重游资格为「永久冻结」
        districtRepository.freezeRevisit(checkin.cityCode)
        _events.emit(RevisitEvent.CityFrozen(checkin.city))

        return true
    }
}