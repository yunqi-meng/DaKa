package com.daka.footprint.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daka.footprint.data.db.entity.CheckinPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: CheckinPointEntity)

    @Update
    suspend fun update(point: CheckinPointEntity)

    @Query("SELECT * FROM checkin_point ORDER BY createTime DESC")
    fun getAllFlow(): Flow<List<CheckinPointEntity>>

    @Query("SELECT * FROM checkin_point ORDER BY createTime DESC")
    suspend fun getAll(): List<CheckinPointEntity>

    @Query("SELECT * FROM checkin_point WHERE id = :id")
    suspend fun getById(id: String): CheckinPointEntity?

    @Query("SELECT * FROM checkin_point WHERE id = :id")
    fun getByIdFlow(id: String): Flow<CheckinPointEntity?>

    @Query("SELECT * FROM checkin_point WHERE cityCode = :cityCode")
    suspend fun getByCityCode(cityCode: String): List<CheckinPointEntity>

    @Query("SELECT * FROM checkin_point WHERE syncStatus != 1")
    suspend fun getUnsynced(): List<CheckinPointEntity>

    @Query("DELETE FROM checkin_point WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE checkin_point SET revisitCount = revisitCount + 1, lastEnterTime = :time, updateTime = :time WHERE id = :id")
    suspend fun incrementRevisitCount(id: String, time: Long)

    @Query("UPDATE checkin_point SET lastEnterTime = :time WHERE id = :id")
    suspend fun updateLastEnterTime(id: String, time: Long)

    @Query("UPDATE checkin_point SET unlockMode = 0, isForceUnlocked = 1, updateTime = :time WHERE id = :id")
    suspend fun forceUnlock(id: String, time: Long)

    @Query("UPDATE checkin_point SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("SELECT COUNT(*) FROM checkin_point")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM checkin_point WHERE cityCode = :cityCode")
    suspend fun getCountByCity(cityCode: String): Int

    @Query("SELECT DISTINCT cityCode FROM checkin_point")
    suspend fun getAllCityCodes(): List<String>

    @Query("SELECT DISTINCT province FROM checkin_point")
    suspend fun getAllProvinces(): List<String>

    @Query("SELECT DISTINCT city FROM checkin_point")
    suspend fun getAllCities(): List<String>
}