package com.daka.footprint.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.daka.footprint.DakaApplication
import com.daka.footprint.R
import com.daka.footprint.logic.RevisitManager
import com.daka.footprint.map.GeofenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class GeofenceService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var geofenceManager: GeofenceManager
    private lateinit var revisitManager: RevisitManager

    companion object {
        const val CHANNEL_ID = "geofence_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_ENTER = "ENTER"
        const val ACTION_EXIT = "EXIT"
        const val EXTRA_CHECKIN_ID = "checkin_id"
    }

    private lateinit var locationManager: com.daka.footprint.map.LocationManager

    override fun onCreate() {
        super.onCreate()
        val app = application as DakaApplication
        geofenceManager = GeofenceManager()
        locationManager = com.daka.footprint.map.LocationManager(this)
        revisitManager = RevisitManager(
            app.checkinRepository,
            app.districtRepository,
            app.userRepository,
            app.achievementRepository
        )
        createNotificationChannel()
        val notification = buildNotification("正在监打卡点围栏…")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // 加载所有限制模式打卡点作为围栏
        scope.launch {
            val limitCheckins = app.checkinRepository.getAllCheckin()
                .filter { it.unlockMode == com.daka.footprint.data.db.entity.CheckinPointEntity.UNLOCK_MODE_LIMIT && !it.isForceUnlocked }
            for (c in limitCheckins) {
                geofenceManager.addFence(c.id, c.latitude, c.longitude)
            }
        }

        // 监听位置变化，检查围栏
        scope.launch {
            locationManager.currentLocation.collect { loc ->
                loc ?: return@collect
                geofenceManager.checkLocation(loc.latitude, loc.longitude)
            }
        }

        // 监听围栏事件
        scope.launch {
            geofenceManager.fenceEvents.collect { event ->
                event ?: return@collect
                if (event.inside) {
                    revisitManager.onEnterFence(event.fenceId)
                } else {
                    revisitManager.onExitFence(event.fenceId)
                }
            }
        }

        locationManager.startLocation()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val checkinId = it.getStringExtra(EXTRA_CHECKIN_ID) ?: return@let
            when (it.action) {
                ACTION_ENTER -> scope.launch { revisitManager.onEnterFence(checkinId) }
                ACTION_EXIT -> scope.launch { revisitManager.onExitFence(checkinId) }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "地理围栏监听", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("地图足迹打卡")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        locationManager.destroy()
        geofenceManager.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}