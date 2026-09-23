package com.daka.footprint.map

import android.content.Context
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationManager(private val context: Context) {

    private var locationClient: LocationClient? = null

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _currentAddress = MutableStateFlow<AddressInfo?>(null)
    val currentAddress: StateFlow<AddressInfo?> = _currentAddress

    data class LatLng(val latitude: Double, val longitude: Double)
    data class AddressInfo(
        val province: String,
        val city: String,
        val district: String,
        val address: String,
        val cityCode: String
    )

    fun startLocation() {
        if (locationClient == null) {
            try {
                com.baidu.location.LocationClient.setAgreePrivacy(true)
                locationClient = LocationClient(context.applicationContext)
            } catch (e: Exception) {
                android.util.Log.e("LocationManager", "LocationClient init failed", e)
                return
            }
            locationClient?.locOption = LocationClientOption().apply {
                setOpenGps(true)
                locationMode = LocationClientOption.LocationMode.Hight_Accuracy
                setCoorType("bd09ll")
                setScanSpan(5000)
                setIsNeedAddress(true)
                setIsNeedLocationDescribe(true)
            }
            locationClient?.registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation?) {
                    location ?: return
                    if (location.locType == BDLocation.TypeGpsLocation ||
                        location.locType == BDLocation.TypeNetWorkLocation) {
                        _currentLocation.value = LatLng(location.latitude, location.longitude)
                        _currentAddress.value = AddressInfo(
                            province = location.province ?: "",
                            city = location.city ?: "",
                            district = location.district ?: "",
                            address = location.addrStr ?: "",
                            cityCode = location.cityCode ?: ""
                        )
                    }
                }
            })
        }
        locationClient?.start()
    }

    fun requestSingleLocation() {
        if (locationClient == null) {
            startLocation()
        }
        locationClient?.let {
            val opt = it.locOption
            opt.scanSpan = 0
            it.locOption = opt
            it.start()
        }
    }

    fun stopLocation() {
        locationClient?.stop()
    }

    fun destroy() {
        locationClient?.let {
            it.stop()

        }
        locationClient = null
    }
}