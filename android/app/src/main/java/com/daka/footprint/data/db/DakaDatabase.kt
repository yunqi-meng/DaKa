package com.daka.footprint.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.daka.footprint.data.db.dao.AchievementDao
import com.daka.footprint.data.db.dao.CheckinDao
import com.daka.footprint.data.db.dao.DistrictDao
import com.daka.footprint.data.db.dao.MediaDao
import com.daka.footprint.data.db.dao.UserProgressDao
import com.daka.footprint.data.db.entity.AchievementEntity
import com.daka.footprint.data.db.entity.CheckinMediaEntity
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.data.db.entity.DistrictEntity
import com.daka.footprint.data.db.entity.UserProgressEntity

@Database(
    entities = [
        CheckinPointEntity::class,
        CheckinMediaEntity::class,
        DistrictEntity::class,
        UserProgressEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DakaDatabase : RoomDatabase() {

    abstract fun checkinDao(): CheckinDao
    abstract fun mediaDao(): MediaDao
    abstract fun districtDao(): DistrictDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: DakaDatabase? = null

        fun getInstance(context: Context): DakaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DakaDatabase::class.java,
                    "daka_footprint.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}