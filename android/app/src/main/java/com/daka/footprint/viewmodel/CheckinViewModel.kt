package com.daka.footprint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daka.footprint.DakaApplication
import com.daka.footprint.data.db.entity.CheckinMediaEntity
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.logic.AchievementChecker
import com.daka.footprint.logic.DistrictLighter
import com.daka.footprint.logic.RevisitManager
import com.daka.footprint.map.GeofenceManager
import com.daka.footprint.map.LocationManager
import com.daka.footprint.map.ReverseGeocodeResult
import com.daka.footprint.map.reverseGeocode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CheckinViewModel : ViewModel() {

    private val app = DakaApplication.instance
    private val checkinRepo = app.checkinRepository
    private val districtRepo = app.districtRepository
    private val userRepo = app.userRepository
    private val achievementRepo = app.achievementRepository

    private val districtLighter = DistrictLighter(checkinRepo, districtRepo, userRepo)
    private val achievementChecker = AchievementChecker(checkinRepo, districtRepo, userRepo, achievementRepo)
    private val revisitManager = RevisitManager(checkinRepo, districtRepo, userRepo, achievementRepo)

    private val geofenceManager = GeofenceManager()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    private val _unlockMode = MutableStateFlow(CheckinPointEntity.UNLOCK_MODE_FREE)
    val unlockMode: StateFlow<Int> = _unlockMode

    private val _selectedLocation = MutableStateFlow<LocationManager.LatLng?>(null)
    val selectedLocation: StateFlow<LocationManager.LatLng?> = _selectedLocation

    private val _addressInfo = MutableStateFlow<ReverseGeocodeResult?>(null)
    val addressInfo: StateFlow<ReverseGeocodeResult?> = _addressInfo

    private val _mediaList = MutableStateFlow<List<CheckinMediaEntity>>(emptyList())
    val mediaList: StateFlow<List<CheckinMediaEntity>> = _mediaList

    private val _saveResult = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val saveResult: SharedFlow<String> = _saveResult

    private val _forceUnlockConfirm = MutableStateFlow<Boolean?>(null)
    val forceUnlockConfirm: StateFlow<Boolean?> = _forceUnlockConfirm

    fun updateTitle(value: String) { _title.value = value }
    fun updateContent(value: String) { _content.value = value }

    fun setUnlockMode(mode: Int) { _unlockMode.value = mode }

    fun setLocation(lat: Double, lng: Double) {
        _selectedLocation.value = LocationManager.LatLng(lat, lng)
        viewModelScope.launch {
            _addressInfo.value = reverseGeocode(lat, lng)
        }
    }

    fun addMedia(media: CheckinMediaEntity) {
        _mediaList.value = _mediaList.value + media
    }

    fun removeMedia(id: String) {
        _mediaList.value = _mediaList.value.filterNot { it.id == id }
    }

    fun saveCheckin() {
        val location = _selectedLocation.value ?: return
        val address = _addressInfo.value ?: return
        val mode = _unlockMode.value

        viewModelScope.launch {
            val photoCount = _mediaList.value.count { it.mediaType == CheckinMediaEntity.TYPE_IMAGE }

            val id = checkinRepo.createCheckin(
                latitude = location.latitude,
                longitude = location.longitude,
                province = address.province,
                cityCode = address.cityCode.ifEmpty { address.city },
                city = address.city,
                district = address.district,
                address = address.address,
                title = _title.value,
                content = _content.value,
                unlockMode = mode,
                mediaList = _mediaList.value
            )

            userRepo.incrementCheckinCount()
            if (photoCount > 0) userRepo.addPhotoCount(photoCount)

            // 点亮城市和省份(refreshLighting 会按所有打卡记录重算城市点亮与省份点亮)
            districtLighter.refreshLighting()

            // 限制模式创建地理围栏
            if (mode == CheckinPointEntity.UNLOCK_MODE_LIMIT) {
                geofenceManager.addFence(id, location.latitude, location.longitude)
                geofenceManager.startMonitoring()
            }

            // 成就校验
            achievementChecker.checkAll()

            _saveResult.emit(id)
        }
    }

    fun forceUnlock(checkinId: String) {
        viewModelScope.launch {
            val success = revisitManager.forceUnlock(checkinId)
            if (success) {
                geofenceManager.removeFence(checkinId)
                achievementChecker.checkAll()
                _forceUnlockConfirm.value = true
            }
        }
    }

    fun showForceUnlockDialog() {
        _forceUnlockConfirm.value = false
    }

    fun dismissForceUnlockDialog() {
        _forceUnlockConfirm.value = null
    }

    fun loadForEdit(checkin: CheckinPointEntity) {
        _title.value = checkin.title
        _content.value = checkin.content
        _unlockMode.value = checkin.unlockMode
        _selectedLocation.value = LocationManager.LatLng(checkin.latitude, checkin.longitude)
        viewModelScope.launch {
            _addressInfo.value = ReverseGeocodeResult(
                province = checkin.province,
                city = checkin.city,
                district = checkin.district,
                address = checkin.address,
                cityCode = checkin.cityCode
            )
            _mediaList.value = checkinRepo.getMediaByCheckin(checkin.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        geofenceManager.destroy()
    }
}