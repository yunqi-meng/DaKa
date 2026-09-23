package com.daka.footprint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daka.footprint.DakaApplication
import com.daka.footprint.data.db.entity.UserProgressEntity
import com.daka.footprint.logic.DistrictLighter
import com.daka.footprint.service.SyncService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val app = DakaApplication.instance
    private val userRepo = app.userRepository
    private val checkinRepo = app.checkinRepository
    private val districtRepo = app.districtRepository

    private val districtLighter = DistrictLighter(checkinRepo, districtRepo, userRepo)

    val userProgress: StateFlow<UserProgressEntity?> =
        userRepo.getProgressFlow().stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _activatedProvinces = MutableStateFlow(0)
    val activatedProvinces: StateFlow<Int> = _activatedProvinces

    private val _activatedCities = MutableStateFlow(0)
    val activatedCities: StateFlow<Int> = _activatedCities

    init {
        viewModelScope.launch {
            districtLighter.refreshLighting()
            _activatedProvinces.value = districtLighter.getActivatedProvinces().size
            _activatedCities.value = districtLighter.getActivatedCities().size
        }
    }

    fun triggerSync() {
        SyncService.triggerImmediateSync(app)
    }
}