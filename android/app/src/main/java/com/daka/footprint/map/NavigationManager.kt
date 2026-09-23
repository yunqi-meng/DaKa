package com.daka.footprint.map

import android.content.Context
import android.content.Intent
import android.net.Uri

class NavigationManager(private val context: Context) {

    fun navigateInApp(latitude: Double, longitude: Double, mode: NavMode = NavMode.WALK) {
        // 百度导航 SDK APP 内导航
        // 需集成百度导航 SDK，此处为入口封装
        // 实际调用：NaviInitManager.getInstance().init() 后启动导航组件
        try {
            val clazz = Class.forName("com.baidu.navi.api.BaiduNaviManager")
            // 导航 SDK 已集成时通过反射启动导航
            // 具体实现依赖导航 SDK 版本
        } catch (e: Exception) {
            // 未集成导航 SDK，降级为调起百度地图 APP
            openBaiduMapApp(latitude, longitude, mode)
        }
    }

    fun openBaiduMapApp(latitude: Double, longitude: Double, mode: NavMode = NavMode.WALK) {
        val modeStr = when (mode) {
            NavMode.WALK -> "walking"
            NavMode.DRIVE -> "driving"
            NavMode.RIDE -> "riding"
        }
        val uri = Uri.parse("baidumap://map/direction?destination=$latitude,$longitude&mode=$modeStr&coord_type=bd09ll&src=com.daka.footprint")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // 未安装百度地图，跳转下载页
            val downloadUri = Uri.parse("https://map.baidu.com/mobile/webapp/index/index.html")
            context.startActivity(Intent(Intent.ACTION_VIEW, downloadUri))
        }
    }

    enum class NavMode { WALK, DRIVE, RIDE }
}