package com.daka.footprint.ui.main

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapMainScreen(
    onOfflineMapClick: () -> Unit = {},
    onCheckinClick: (Double, Double) -> Unit = { _, _ -> }
) {
    val viewModel: MapViewModel = viewModel()
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val checkinPoints by viewModel.checkinPoints.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val selectedCheckin by viewModel.selectedCheckin.collectAsState()
    val mapType by viewModel.mapType.collectAsState()

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            viewModel.startLocation()
        }
        viewModel.refreshLighting()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BaiduMapView(
            checkinPoints = checkinPoints,
            currentLocation = currentLocation,
            mapType = mapType,
            onCheckinClick = { viewModel.selectCheckin(it) },
            onMapLongClick = { lat, lng ->
                onCheckinClick(lat, lng)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 顶部图层切换按钮
        FloatingActionButton(
            onClick = { viewModel.toggleMapType() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(
                if (mapType == MapViewModel.MapType.NORMAL) Icons.Filled.Satellite else Icons.Filled.Map,
                contentDescription = "切换图层"
            )
        }

        // 离线地图管理入口
        FloatingActionButton(
            onClick = onOfflineMapClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(Icons.Filled.CloudDownload, contentDescription = "离线地图")
        }

        // 离线状态提示
        val offlineCount = remember {
            try {
                com.daka.footprint.DakaApplication.instance.offlineMapManager
                    .getCityList().count { city ->
                        com.daka.footprint.DakaApplication.instance.offlineMapManager
                            .getUpdateInfo(city.cityID)?.status ==
                            com.baidu.mapapi.map.offline.MKOLUpdateElement.FINISHED
                    }
            } catch (e: Exception) { 0 }
        }
        if (offlineCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 72.dp, start = 16.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "离线地图：${offlineCount}个城市",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // 右下角当前位置打卡按钮
        FloatingActionButton(
            onClick = {
                currentLocation?.let { loc: com.daka.footprint.map.LocationManager.LatLng ->
                    onCheckinClick(loc.latitude, loc.longitude)
                } ?: run {
                    android.widget.Toast.makeText(context, "正在获取定位，请长按地图选择位置", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.AddLocationAlt, contentDescription = "当前位置打卡")
        }

        // 打卡点气泡预览
        selectedCheckin?.let { checkin ->
            CheckinPreviewCard(
                checkin = checkin,
                onDismiss = { viewModel.selectCheckin(null) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
    }
}

@Composable
fun BaiduMapView(
    checkinPoints: List<CheckinPointEntity>,
    currentLocation: com.daka.footprint.map.LocationManager.LatLng?,
    mapType: MapViewModel.MapType,
    onCheckinClick: (CheckinPointEntity) -> Unit,
    onMapLongClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        factory = { ctx ->
            val mapView = MapView(ctx)
            mapViewRef.value = mapView
            try {
                val baiduMap = mapView.map
                baiduMap.uiSettings.apply {
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                    isRotateGesturesEnabled = true
                    isOverlookingGesturesEnabled = true
                }
                baiduMap.setMapType(
                    if (mapType == MapViewModel.MapType.NORMAL) BaiduMap.MAP_TYPE_NORMAL
                    else BaiduMap.MAP_TYPE_SATELLITE
                )
                baiduMap.setOnMapLongClickListener { latLng ->
                    onMapLongClick(latLng.latitude, latLng.longitude)
                }
            } catch (e: Exception) {
                android.util.Log.e("BaiduMapView", "Map init failed", e)
            }
            mapView
        },
        update = { view ->
            try {
                view.map.setMapType(
                    if (mapType == MapViewModel.MapType.NORMAL) BaiduMap.MAP_TYPE_NORMAL
                    else BaiduMap.MAP_TYPE_SATELLITE
                )

                view.map.clear()

                // 当前位置标记
                currentLocation?.let { loc: com.daka.footprint.map.LocationManager.LatLng ->
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    view.map.addOverlay(
                        MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation))
                            .zIndex(9)
                    )
                }

                // 打卡点标记
                checkinPoints.forEach { checkin ->
                    val latLng = LatLng(checkin.latitude, checkin.longitude)
                    val iconRes = when {
                        checkin.unlockMode == CheckinPointEntity.UNLOCK_MODE_FREE ->
                            android.R.drawable.ic_menu_mapmode
                        checkin.isForceUnlocked ->
                            android.R.drawable.ic_menu_view
                        else ->
                            android.R.drawable.ic_menu_compass
                    }
                    view.map.addOverlay(
                        MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(iconRes))
                            .title(checkin.id)
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("BaiduMapView", "Map update failed", e)
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef.value?.onDestroy()
        }
    }

}

@Composable
fun CheckinPreviewCard(
    checkin: CheckinPointEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = checkin.title,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭")
                }
            }
            Text(
                text = "${checkin.province} ${checkin.city} ${checkin.district}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            val modeText = if (checkin.unlockMode == CheckinPointEntity.UNLOCK_MODE_FREE)
                "无限制模式" else "500米限制模式"
            Text(
                text = modeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            if (checkin.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = checkin.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
        }
    }
}