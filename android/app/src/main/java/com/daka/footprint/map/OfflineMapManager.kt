package com.daka.footprint.map

import android.content.Context
import com.baidu.mapapi.map.offline.MKOLUpdateElement
import com.baidu.mapapi.map.offline.MKOLSearchRecord
import com.baidu.mapapi.map.offline.MKOfflineMap
import com.baidu.mapapi.map.offline.MKOfflineMapListener

class OfflineMapManager(private val context: Context) {

    private var offlineMap: MKOfflineMap? = null

    fun init() {
        offlineMap = MKOfflineMap()
        offlineMap?.init(object : MKOfflineMapListener {
            override fun onGetOfflineMapState(state: Int, type: Int) {
            }
        })
    }

    fun getCityList(): List<MKOLSearchRecord> {
        return offlineMap?.offlineCityList ?: emptyList()
    }

    fun downloadCity(cityId: Int) {
        offlineMap?.start(cityId)
    }

    fun pauseDownload(cityId: Int) {
        offlineMap?.pause(cityId)
    }

    fun removeCity(cityId: Int) {
        offlineMap?.remove(cityId)
    }

    fun getUpdateInfo(cityId: Int): MKOLUpdateElement? {
        return offlineMap?.getUpdateInfo(cityId)
    }

    fun destroy() {
        offlineMap?.destroy()
        offlineMap = null
    }
}
