package com.daka.footprint.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daka.footprint.data.db.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getFlow(userId: String): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    suspend fun get(userId: String): UserProgressEntity?

    @Query("UPDATE user_progress SET totalCheckinCount = totalCheckinCount + 1, updateTime = :time WHERE userId = :userId")
    suspend fun incrementCheckinCount(userId: String, time: Long)

    @Query("UPDATE user_progress SET totalCheckinCount = totalCheckinCount - 1, updateTime = :time WHERE userId = :userId")
    suspend fun decrementCheckinCount(userId: String, time: Long)

    @Query("UPDATE user_progress SET totalRevisitProgress = totalRevisitProgress + 1, updateTime = :time WHERE userId = :userId")
    suspend fun incrementRevisitProgress(userId: String, time: Long)

    @Query("UPDATE user_progress SET activatedProvinceCount = :count, updateTime = :time WHERE userId = :userId")
    suspend fun updateProvinceCount(userId: String, count: Int, time: Long)

    @Query("UPDATE user_progress SET activatedCityCount = :count, updateTime = :time WHERE userId = :userId")
    suspend fun updateCityCount(userId: String, count: Int, time: Long)

    @Query("UPDATE user_progress SET totalPhotoCount = totalPhotoCount + :delta, updateTime = :time WHERE userId = :userId")
    suspend fun addPhotoCount(userId: String, delta: Int, time: Long)

    @Query("SELECT totalRevisitProgress FROM user_progress WHERE userId = :userId")
    suspend fun getRevisitProgress(userId: String): Int
}