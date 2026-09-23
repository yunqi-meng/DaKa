package com.daka.footprint.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: String,
    val totalCheckinCount: Int = 0,
    /** 故地重游总进度（上限10） */
    val totalRevisitProgress: Int = 0,
    val activatedProvinceCount: Int = 0,
    val activatedCityCount: Int = 0,
    val totalPhotoCount: Int = 0,
    val updateTime: Long = System.currentTimeMillis()
)