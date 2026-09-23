package com.daka.footprint.map

import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GeofenceManager {

    private val fences = mutableMapOf<String, FenceInfo>()
    // 记录每个围栏上一次的进出状态,只有状态发生变化才发事件,避免重复触发
    private val lastInsideState = mutableMapOf<String, Boolean>()

    private val _fenceEvents = MutableStateFlow<FenceEvent?>(null)
    val fenceEvents: StateFlow<FenceEvent?> = _fenceEvents

    data class FenceInfo(
        val fenceId: String,
        val latitude: Double,
        val longitude: Double,
        val radius: Double = FENCE_RADIUS
    )

    data class FenceEvent(
        val fenceId: String,
        val status: Int,
        val inside: Boolean
    )

    companion object {
        const val FENCE_RADIUS = 500.0
        const val STATUS_INSIDE = 1
        const val STATUS_OUTSIDE = 0
    }

    fun addFence(fenceId: String, latitude: Double, longitude: Double) {
        fences[fenceId] = FenceInfo(fenceId, latitude, longitude)
        lastInsideState.remove(fenceId)
    }

    fun removeFence(fenceId: String) {
        fences.remove(fenceId)
        lastInsideState.remove(fenceId)
    }

    fun removeAllFences() {
        fences.clear()
        lastInsideState.clear()
    }

    fun startMonitoring() {}

    fun stopMonitoring() {}

    fun isWithinFence(currentLat: Double, currentLng: Double, fenceLat: Double, fenceLng: Double): Boolean {
        val current = LatLng(currentLat, currentLng)
        val fence = LatLng(fenceLat, fenceLng)
        return DistanceUtil.getDistance(current, fence) <= FENCE_RADIUS
    }

    fun checkLocation(currentLat: Double, currentLng: Double) {
        val current = LatLng(currentLat, currentLng)
        for (fence in fences.values) {
            val distance = DistanceUtil.getDistance(current, LatLng(fence.latitude, fence.longitude))
            val inside = distance <= fence.radius
            if (lastInsideState[fence.fenceId] == inside) continue
            lastInsideState[fence.fenceId] = inside
            _fenceEvents.value = FenceEvent(
                fenceId = fence.fenceId,
                status = if (inside) STATUS_INSIDE else STATUS_OUTSIDE,
                inside = inside
            )
        }
    }

    fun destroy() {
        fences.clear()
        lastInsideState.clear()
    }
}
