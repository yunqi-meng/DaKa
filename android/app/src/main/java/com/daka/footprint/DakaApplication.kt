package com.daka.footprint

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.daka.footprint.data.db.DakaDatabase
import com.daka.footprint.data.repository.AchievementRepository
import com.daka.footprint.data.repository.CheckinRepository
import com.daka.footprint.data.repository.DistrictRepository
import com.daka.footprint.data.repository.UserRepository
import com.daka.footprint.map.OfflineMapManager
import com.daka.footprint.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DakaApplication : Application() {

    val database by lazy { DakaDatabase.getInstance(this) }

    val checkinRepository by lazy { CheckinRepository(database.checkinDao(), database.mediaDao()) }
    val districtRepository by lazy { DistrictRepository(database.districtDao()) }
    val userRepository by lazy { UserRepository(database.userProgressDao()) }
    val achievementRepository by lazy { AchievementRepository(database.achievementDao()) }

    val offlineMapManager by lazy { OfflineMapManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化百度地图 SDK
        try {
            SDKInitializer.setAgreePrivacy(this, true)
            SDKInitializer.initialize(this)
            SDKInitializer.setCoordType(CoordType.BD09LL)
        } catch (e: Exception) {
            android.util.Log.e("DakaApp", "Map SDK init failed", e)
        }

        // 初始化百度定位 SDK 隐私政策
        try {
            com.baidu.location.LocationClient.setAgreePrivacy(true)
        } catch (e: Exception) {
            android.util.Log.e("DakaApp", "Location SDK privacy failed", e)
        }

        // 初始化网络客户端
        try {
            ApiClient.init()
        } catch (e: Exception) {
            android.util.Log.e("DakaApp", "ApiClient init failed", e)
        }

        // 初始化离线地图
        try {
            offlineMapManager.init()
        } catch (e: Exception) {
            android.util.Log.e("DakaApp", "OfflineMap init failed", e)
        }

        // 初始化用户进度与成就数据（首次启动）
        appScope.launch {
            try {
                userRepository.initIfNeeded()
                achievementRepository.initAchievementsIfNeeded()
            } catch (e: Exception) {
                android.util.Log.e("DakaApp", "Init data failed", e)
            }
        }
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        lateinit var instance: DakaApplication
            private set
    }
}