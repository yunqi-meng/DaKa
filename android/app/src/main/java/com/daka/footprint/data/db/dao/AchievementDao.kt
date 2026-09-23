package com.daka.footprint.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daka.footprint.data.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM achievement ORDER BY category, isUnlocked DESC")
    fun getAllFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievement WHERE id = :id")
    suspend fun getById(id: String): AchievementEntity?

    @Query("SELECT * FROM achievement WHERE isUnlocked = 1 ORDER BY unlockTime DESC")
    fun getUnlockedFlow(): Flow<List<AchievementEntity>>

    @Query("UPDATE achievement SET isUnlocked = 1, unlockTime = :time WHERE id = :id")
    suspend fun unlock(id: String, time: Long)

    @Query("UPDATE achievement SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("SELECT COUNT(*) FROM achievement")
    suspend fun getCount(): Int
}