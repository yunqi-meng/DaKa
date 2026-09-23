package com.daka.footprint.data.repository

import com.daka.footprint.data.db.dao.DistrictDao
import com.daka.footprint.data.db.entity.DistrictEntity
import kotlinx.coroutines.flow.Flow

class DistrictRepository(private val districtDao: DistrictDao) {

    fun getAllProvincesFlow(): Flow<List<DistrictEntity>> = districtDao.getAllProvincesFlow()

    suspend fun getByCode(code: String): DistrictEntity? = districtDao.getByCode(code)

    suspend fun getByLevel(level: Int): List<DistrictEntity> = districtDao.getByLevel(level)

    suspend fun getActivatedProvinces(): List<DistrictEntity> = districtDao.getActivatedProvinces()

    suspend fun getActivatedCities(): List<DistrictEntity> = districtDao.getActivatedCities()

    suspend fun getCitiesByProvince(provinceCode: String): List<DistrictEntity> = districtDao.getCitiesByProvince(provinceCode)

    suspend fun activate(code: String) = districtDao.activate(code)

    suspend fun deactivate(code: String) = districtDao.deactivate(code)

    suspend fun markAsContributed(code: String) = districtDao.markAsContributed(code)

    suspend fun freezeRevisit(code: String) = districtDao.freezeRevisit(code)

    suspend fun updateBoundary(code: String, boundary: String) = districtDao.updateBoundary(code, boundary)

    suspend fun getActivatedProvinceCount(): Int = districtDao.getActivatedProvinceCount()

    suspend fun getActivatedCityCount(): Int = districtDao.getActivatedCityCount()

    suspend fun getActivatedCityCountInProvince(provinceCode: String): Int =
        districtDao.getActivatedCityCountInProvince(provinceCode)

    suspend fun getTotalCityCountInProvince(provinceCode: String): Int =
        districtDao.getTotalCityCountInProvince(provinceCode)

    suspend fun insertAll(districts: List<DistrictEntity>) = districtDao.insertAll(districts)

    suspend fun insert(district: DistrictEntity) = districtDao.insert(district)
}