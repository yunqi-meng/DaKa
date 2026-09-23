package com.daka.footprint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daka.footprint.DakaApplication
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.logic.AchievementChecker
import com.daka.footprint.logic.DistrictLighter
import com.daka.footprint.map.LocationManager
import com.daka.footprint.map.ReverseGeocodeResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val app = DakaApplication.instance
    private val checkinRepo = app.checkinRepository
    private val districtRepo = app.districtRepository
    private val userRepo = app.userRepository
    private val achievementRepo = app.achievementRepository

    private val districtLighter = DistrictLighter(checkinRepo, districtRepo, userRepo)
    private val achievementChecker = AchievementChecker(checkinRepo, districtRepo, userRepo, achievementRepo)

    val locationManager = LocationManager(app)

    val checkinPoints: StateFlow<List<CheckinPointEntity>> =
        checkinRepo.getAllCheckinFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentLocation: StateFlow<LocationManager.LatLng?> = locationManager.currentLocation

    private val _selectedCheckin = MutableStateFlow<CheckinPointEntity?>(null)
    val selectedCheckin: StateFlow<CheckinPointEntity?> = _selectedCheckin

    private val _reverseGeocode = MutableStateFlow<ReverseGeocodeResult?>(null)
    val reverseGeocode: StateFlow<ReverseGeocodeResult?> = _reverseGeocode

    private val _mapType = MutableStateFlow(MapType.NORMAL)
    val mapType: StateFlow<MapType> = _mapType

    enum class MapType { NORMAL, SATELLITE }

    fun selectCheckin(checkin: CheckinPointEntity?) {
        _selectedCheckin.value = checkin
    }

    fun toggleMapType() {
        _mapType.value = if (_mapType.value == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
    }

    fun startLocation() {
        locationManager.startLocation()
    }

    fun requestReverseGeocode(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _reverseGeocode.value = com.daka.footprint.map.reverseGeocode(latitude, longitude)
        }
    }

    fun refreshLighting() {
        viewModelScope.launch {
            districtLighter.refreshLighting()
            achievementChecker.checkAll()
        }
    }

    fun deleteCheckin(id: String, cityCode: String, provinceCode: String?) {
        viewModelScope.launch {
            checkinRepo.deleteCheckin(id)
            districtLighter.onCheckinDeleted(cityCode, provinceCode)
            userRepo.decrementCheckinCount()
            achievementChecker.checkAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationManager.destroy()
    }
}