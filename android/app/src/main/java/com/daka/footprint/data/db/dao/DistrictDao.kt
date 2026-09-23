package com.daka.footprint.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daka.footprint.data.db.entity.DistrictEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DistrictDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(district: DistrictEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(districts: List<DistrictEntity>)

    @Update
    suspend fun update(district: DistrictEntity)

    @Query("SELECT * FROM district WHERE code = :code")
    suspend fun getByCode(code: String): DistrictEntity?

    @Query("SELECT * FROM district WHERE level = :level")
    suspend fun getByLevel(level: Int): List<DistrictEntity>

    @Query("SELECT * FROM district WHERE level = 1")
    fun getAllProvincesFlow(): Flow<List<DistrictEntity>>

    @Query("SELECT * FROM district WHERE level = 1 AND isActivated = 1")
    suspend fun getActivatedProvinces(): List<DistrictEntity>

    @Query("SELECT * FROM district WHERE level = 2 AND parentCode = :provinceCode")
    suspend fun getCitiesByProvince(provinceCode: String): List<DistrictEntity>

    @Query("SELECT * FROM district WHERE level = 2 AND isActivated = 1")
    suspend fun getActivatedCities(): List<DistrictEntity>

    @Query("UPDATE district SET isActivated = 1 WHERE code = :code")
    suspend fun activate(code: String)

    @Query("UPDATE district SET isActivated = 0 WHERE code = :code")
    suspend fun deactivate(code: String)

    @Query("UPDATE district SET hasRevisitContributed = 1 WHERE code = :code")
    suspend fun markAsContributed(code: String)

    @Query("UPDATE district SET isRevisitFrozen = 1 WHERE code = :code")
    suspend fun freezeRevisit(code: String)

    @Query("UPDATE district SET boundary = :boundary WHERE code = :code")
    suspend fun updateBoundary(code: String, boundary: String)

    @Query("SELECT COUNT(*) FROM district WHERE level = 1 AND isActivated = 1")
    suspend fun getActivatedProvinceCount(): Int

    @Query("SELECT COUNT(*) FROM district WHERE level = 2 AND isActivated = 1")
    suspend fun getActivatedCityCount(): Int

    @Query("SELECT COUNT(*) FROM district WHERE level = 2 AND parentCode = :provinceCode AND isActivated = 1")
    suspend fun getActivatedCityCountInProvince(provinceCode: String): Int

    @Query("SELECT COUNT(*) FROM district WHERE level = 2 AND parentCode = :provinceCode")
    suspend fun getTotalCityCountInProvince(provinceCode: String): Int
}