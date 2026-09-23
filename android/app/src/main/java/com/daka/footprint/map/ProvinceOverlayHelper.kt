package com.daka.footprint.map

import android.graphics.Color
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.PolygonOptions
import com.baidu.mapapi.model.LatLng
import com.daka.footprint.logic.DistrictLighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProvinceOverlayHelper(
    private val baiduMap: BaiduMap,
    private val districtLighter: DistrictLighter,
    private val districtSearchManager: DistrictSearchManager
) {

    companion object {
        const val COLOR_LIT = 0x334CAF50
        const val COLOR_LIT_DEEP = 0x664CAF50
        const val COLOR_STROKE = 0xFF4CAF50.toInt()
    }

    private val drawnOverlays = mutableListOf<com.baidu.mapapi.map.Overlay>()

    suspend fun drawActivatedProvinces() {
        // Overlay 操作必须在主线程,只有边界数据查询放 IO
        withContext(Dispatchers.Main) { clearOverlays() }
        val provinces = withContext(Dispatchers.IO) { districtLighter.getActivatedProvinces() }

        for (province in provinces) {
            val isFullyLit = withContext(Dispatchers.IO) { districtLighter.isProvinceFullyLit(province.code) }
            drawProvinceBoundary(province.name, isFullyLit)
        }
    }

    private suspend fun drawProvinceBoundary(provinceName: String, isFullyLit: Boolean) {
        val boundary = withContext(Dispatchers.IO) {
            districtSearchManager.searchDistrict(provinceName)
        } ?: return

        val fillColor = if (isFullyLit) COLOR_LIT_DEEP else COLOR_LIT

        withContext(Dispatchers.Main) {
            for (polygon in boundary.polygonPoints) {
                if (polygon.size < 3) continue
                val options = PolygonOptions()
                    .points(polygon)
                    .fillColor(fillColor)
                    .stroke(com.baidu.mapapi.map.Stroke(2f, COLOR_STROKE))
                baiduMap.addOverlay(options)?.let { drawnOverlays.add(it) }
            }
        }
    }

    fun clearOverlays() {
        drawnOverlays.forEach { it.remove() }
        drawnOverlays.clear()
    }
}