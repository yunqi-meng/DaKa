package com.daka.footprint.network

import android.content.Context
import android.util.Log
import com.daka.footprint.DakaApplication
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.network.dto.CheckinDto
import com.daka.footprint.network.dto.MediaDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SyncManager {

    private const val TAG = "SyncManager"

    suspend fun syncCheckins(context: Context): Boolean = withContext(Dispatchers.IO) {
        val api = ApiClient.apiService
        if (api == null || !ApiClient.hasToken()) {
            Log.d(TAG, "未登录或API未初始化，跳过同步")
            return@withContext false
        }

        val app = context.applicationContext as DakaApplication
        val unsynced = app.checkinRepository.getUnsynced()
        if (unsynced.isEmpty()) return@withContext true

        var allSuccess = true
        for (point in unsynced) {
            try {
                val media = app.checkinRepository.getMediaByCheckin(point.id)
                val dto = point.toDto(media.map { it.toDto() })
                val response = api.saveCheckin(ApiClient.authHeader() ?: "", dto)
                if (response.isSuccess) {
                    app.checkinRepository.updateSyncStatus(point.id, 1)
                } else {
                    Log.e(TAG, "同步打卡点 ${point.id} 被服务端拒绝: ${response.message}")
                    allSuccess = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "同步打卡点 ${point.id} 失败", e)
                allSuccess = false
            }
        }
        allSuccess
    }

    private fun CheckinPointEntity.toDto(media: List<MediaDto>) = CheckinDto(
        id = id,
        latitude = latitude,
        longitude = longitude,
        province = province,
        cityCode = cityCode,
        city = city,
        district = district,
        address = address,
        title = title,
        content = content,
        unlockMode = unlockMode,
        revisitCount = revisitCount,
        isForceUnlocked = isForceUnlocked,
        createTime = createTime,
        updateTime = updateTime,
        media = media
    )

    private fun com.daka.footprint.data.db.entity.CheckinMediaEntity.toDto() = MediaDto(
        id = id,
        checkinId = checkinId,
        mediaType = mediaType,
        remoteUrl = remoteUrl
    )
}