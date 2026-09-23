package com.daka.footprint.data.repository

import com.daka.footprint.data.db.dao.AchievementDao
import com.daka.footprint.data.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

class AchievementRepository(private val achievementDao: AchievementDao) {

    fun getAllFlow(): Flow<List<AchievementEntity>> = achievementDao.getAllFlow()

    fun getUnlockedFlow(): Flow<List<AchievementEntity>> = achievementDao.getUnlockedFlow()

    suspend fun initAchievementsIfNeeded() {
        if (achievementDao.getCount() > 0) return
        achievementDao.insertAll(defaultAchievements())
    }

    private fun defaultAchievements(): List<AchievementEntity> = listOf(
        AchievementEntity(AchievementEntity.ID_FIRST_CHECKIN, "初见足迹", "完成第1个打卡点", AchievementEntity.CAT_BASIC, 1),
        AchievementEntity(AchievementEntity.ID_HUNDRED, "百步达人", "累计打卡100个地点", AchievementEntity.CAT_BASIC, 100),
        AchievementEntity(AchievementEntity.ID_NORTH_CHINA, "踏遍华北", "点亮华北地区所有省份", AchievementEntity.CAT_REGION, 5),
        AchievementEntity(AchievementEntity.ID_ALL_CHINA, "环游中国", "点亮全国34个省级行政区", AchievementEntity.CAT_REGION, 34),
        AchievementEntity(AchievementEntity.ID_REVISIT_ONE, "故地重游・一城", "完成首个城市的重游进度贡献", AchievementEntity.CAT_REVISIT, 1),
        AchievementEntity(AchievementEntity.ID_REVISIT_FULL, "故地重游・十城", "累计10个城市完成重游进度贡献", AchievementEntity.CAT_REVISIT, 10),
        AchievementEntity(AchievementEntity.ID_TIER1, "一线城市打卡", "打卡北上广深全部4个城市", AchievementEntity.CAT_CITY, 4),
        AchievementEntity(AchievementEntity.ID_PHOTO, "摄影达人", "累计上传100张打卡图片", AchievementEntity.CAT_MEDIA, 100),
        AchievementEntity(AchievementEntity.ID_BORDER, "边疆探索者", "打卡新疆、西藏、内蒙古、黑龙江、云南", AchievementEntity.CAT_SPECIAL, 5)
    )

    suspend fun unlock(id: String): Boolean {
        val achievement = achievementDao.getById(id) ?: return false
        if (achievement.isUnlocked) return false
        achievementDao.unlock(id, System.currentTimeMillis())
        return true
    }

    suspend fun updateProgress(id: String, progress: Int) {
        achievementDao.updateProgress(id, progress)
    }

    suspend fun getById(id: String): AchievementEntity? = achievementDao.getById(id)

    companion object {
        val NORTH_CHINA_PROVINCES = setOf("北京市", "天津市", "河北省", "山西省", "内蒙古自治区")
        val TIER1_CITIES = setOf("北京市", "上海市", "广州市", "深圳市")
        val BORDER_PROVINCES = setOf("新疆维吾尔自治区", "西藏自治区", "内蒙古自治区", "黑龙江省", "云南省")
    }
}