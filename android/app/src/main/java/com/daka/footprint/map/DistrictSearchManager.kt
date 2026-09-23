package com.daka.footprint.map

import com.baidu.mapapi.search.district.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DistrictSearchManager {

    private var districtSearch: DistrictSearch? = null

    data class DistrictBoundary(
        val name: String,
        val center: LatLng,
        val polygonPoints: List<List<LatLng>>
    )

    suspend fun searchDistrict(keyword: String): DistrictBoundary? {
        districtSearch = DistrictSearch.newInstance()

        return suspendCancellableCoroutine { cont ->
            districtSearch?.setOnDistrictSearchListener(object : OnGetDistricSearchResultListener {
                override fun onGetDistrictResult(result: DistrictResult?) {
                    if (result == null || result.centerPt == null) {
                        cont.resume(null)
                        return
                    }
                    val polygons = mutableListOf<List<LatLng>>()
                    result.polylines?.forEach { polyline ->
                        val points = polyline.map { LatLng(it.latitude, it.longitude) }
                        if (points.isNotEmpty()) polygons.add(points)
                    }
                    val boundary = DistrictBoundary(
                        name = keyword,
                        center = LatLng(result.centerPt.latitude, result.centerPt.longitude),
                        polygonPoints = polygons
                    )
                    cont.resume(boundary)
                }
            })

            val option = DistrictSearchOption()
            option.cityName(keyword)
            districtSearch?.searchDistrict(option)
        }
    }

    fun destroy() {
        districtSearch?.destroy()
        districtSearch = null
    }
}

suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResult? {
    val geoCoder = GeoCoder.newInstance()
    return suspendCancellableCoroutine { cont ->
        geoCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(result: GeoCodeResult?) {}

            override fun onGetReverseGeoCodeResult(result: ReverseGeoCodeResult?) {
                if (result == null) {
                    cont.resume(null)
                    return
                }
                val addr = result.addressDetail
                cont.resume(
                    ReverseGeocodeResult(
                        province = addr?.province ?: "",
                        city = addr?.city ?: "",
                        district = addr?.district ?: "",
                        address = result.address ?: "",
                        cityCode = result.cityCode.toString()
                    )
                )
            }
        })
        geoCoder.reverseGeoCode(
            ReverseGeoCodeOption()
                .location(LatLng(latitude, longitude))
        )
    }
}

data class ReverseGeocodeResult(
    val province: String,
    val city: String,
    val district: String,
    val address: String,
    val cityCode: String
)
