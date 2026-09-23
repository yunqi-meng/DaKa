package com.daka.footprint.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daka.footprint.data.db.entity.CheckinMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: CheckinMediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(media: List<CheckinMediaEntity>)

    @Query("SELECT * FROM checkin_media WHERE checkinId = :checkinId")
    fun getByCheckinFlow(checkinId: String): Flow<List<CheckinMediaEntity>>

    @Query("SELECT * FROM checkin_media WHERE checkinId = :checkinId")
    suspend fun getByCheckin(checkinId: String): List<CheckinMediaEntity>

    @Query("SELECT COUNT(*) FROM checkin_media WHERE mediaType = 1")
    suspend fun getPhotoCount(): Int

    @Query("DELETE FROM checkin_media WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM checkin_media WHERE checkinId = :checkinId")
    suspend fun deleteByCheckin(checkinId: String)
}