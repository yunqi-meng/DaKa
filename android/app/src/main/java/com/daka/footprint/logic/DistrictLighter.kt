package com.daka.footprint.logic

import com.daka.footprint.data.db.entity.DistrictEntity
import com.daka.footprint.data.repository.CheckinRepository
import com.daka.footprint.data.repository.DistrictRepository
import com.daka.footprint.data.repository.UserRepository

class DistrictLighter(
    private val checkinRepository: CheckinRepository,
    private val districtRepository: DistrictRepository,
    private val userRepository: UserRepository
) {

    suspend fun refreshLighting() {
        val cityCodes = checkinRepository.getAllCityCodes()
        for (cityCode in cityCodes) {
            val count = checkinRepository.getCountByCity(cityCode)
            if (count > 0) {
                districtRepository.activate(cityCode)
                val city = districtRepository.getByCode(cityCode)
                city?.parentCode?.let { provinceCode ->
                    checkProvinceLighting(provinceCode)
                }
            }
        }
        updateProgressCounts()
    }

    suspend fun onCheckinCreated(cityCode: String, provinceCode: String?) {
        districtRepository.activate(cityCode)
        provinceCode?.let { checkProvinceLighting(it) }
        updateProgressCounts()
    }

    suspend fun onCheckinDeleted(cityCode: String, provinceCode: String?) {
        val count = checkinRepository.getCountByCity(cityCode)
        if (count == 0) {
            districtRepository.deactivate(cityCode)
            provinceCode?.let { checkProvinceLighting(it) }
        }
        updateProgressCounts()
    }

    private suspend fun checkProvinceLighting(provinceCode: String) {
        val activatedCityCount = districtRepository.getActivatedCityCountInProvince(provinceCode)
        if (activatedCityCount > 0) {
            districtRepository.activate(provinceCode)
        } else {
            districtRepository.deactivate(provinceCode)
        }
    }

    private suspend fun updateProgressCounts() {
        val provinceCount = districtRepository.getActivatedProvinceCount()
        val cityCount = districtRepository.getActivatedCityCount()
        userRepository.updateProvinceCount(provinceCount)
        userRepository.updateCityCount(cityCount)
    }

    suspend fun isProvinceFullyLit(provinceCode: String): Boolean {
        val activated = districtRepository.getActivatedCityCountInProvince(provinceCode)
        val total = districtRepository.getTotalCityCountInProvince(provinceCode)
        return total > 0 && activated >= total
    }

    suspend fun getActivatedProvinces(): List<DistrictEntity> = districtRepository.getActivatedProvinces()
    suspend fun getActivatedCities(): List<DistrictEntity> = districtRepository.getActivatedCities()
}