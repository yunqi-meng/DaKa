package com.daka.footprint.data.repository

import com.daka.footprint.data.db.dao.CheckinDao
import com.daka.footprint.data.db.dao.MediaDao
import com.daka.footprint.data.db.entity.CheckinMediaEntity
import com.daka.footprint.data.db.entity.CheckinPointEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CheckinRepository(
    private val checkinDao: CheckinDao,
    private val mediaDao: MediaDao
) {

    fun getAllCheckinFlow(): Flow<List<CheckinPointEntity>> = checkinDao.getAllFlow()

    suspend fun getAllCheckin(): List<CheckinPointEntity> = checkinDao.getAll()

    suspend fun getCheckinById(id: String): CheckinPointEntity? = checkinDao.getById(id)

    fun getCheckinByIdFlow(id: String): Flow<CheckinPointEntity?> = checkinDao.getByIdFlow(id)

    suspend fun getMediaByCheckin(checkinId: String): List<CheckinMediaEntity> = mediaDao.getByCheckin(checkinId)

    fun getMediaByCheckinFlow(checkinId: String): Flow<List<CheckinMediaEntity>> = mediaDao.getByCheckinFlow(checkinId)

    suspend fun createCheckin(
        latitude: Double,
        longitude: Double,
        province: String,
        cityCode: String,
        city: String,
        district: String,
        address: String,
        title: String,
        content: String,
        unlockMode: Int,
        mediaList: List<CheckinMediaEntity> = emptyList()
    ): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val point = CheckinPointEntity(
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
            createTime = now,
            updateTime = now
        )
        checkinDao.insert(point)
        if (mediaList.isNotEmpty()) {
            mediaDao.insertAll(mediaList)
        }
        return id
    }

    suspend fun updateCheckin(point: CheckinPointEntity) {
        checkinDao.update(point.copy(updateTime = System.currentTimeMillis(), syncStatus = 2))
    }

    suspend fun deleteCheckin(id: String) {
        checkinDao.delete(id)
    }

    suspend fun incrementRevisitCount(id: String) {
        checkinDao.incrementRevisitCount(id, System.currentTimeMillis())
    }

    suspend fun updateLastEnterTime(id: String, time: Long) {
        checkinDao.updateLastEnterTime(id, time)
    }

    suspend fun forceUnlock(id: String) {
        checkinDao.forceUnlock(id, System.currentTimeMillis())
    }

    suspend fun getUnsynced(): List<CheckinPointEntity> = checkinDao.getUnsynced()

    suspend fun updateSyncStatus(id: String, status: Int) {
        checkinDao.updateSyncStatus(id, status)
    }

    suspend fun getCount(): Int = checkinDao.getCount()

    suspend fun getCountByCity(cityCode: String): Int = checkinDao.getCountByCity(cityCode)

    suspend fun getAllCityCodes(): List<String> = checkinDao.getAllCityCodes()

    suspend fun getAllProvinces(): List<String> = checkinDao.getAllProvinces()

    suspend fun getAllCities(): List<String> = checkinDao.getAllCities()

    suspend fun getPhotoCount(): Int = mediaDao.getPhotoCount()

    suspend fun getByCityCode(cityCode: String): List<CheckinPointEntity> = checkinDao.getByCityCode(cityCode)
}