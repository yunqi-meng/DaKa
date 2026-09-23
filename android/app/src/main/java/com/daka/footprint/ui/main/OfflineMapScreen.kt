package com.daka.footprint.ui.main

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.offline.MKOLSearchRecord
import com.baidu.mapapi.map.offline.MKOLUpdateElement
import com.daka.footprint.DakaApplication
import kotlinx.coroutines.delay

class OfflineMapViewModel(app: Application) : AndroidViewModel(app) {
    private val manager = (app as DakaApplication).offlineMapManager

    private val _cityList = mutableStateOf<List<MKOLSearchRecord>>(emptyList())
    val cityList: State<List<MKOLSearchRecord>> = _cityList

    private val _downloadStates = mutableStateOf<Map<Int, MKOLUpdateElement>>(emptyMap())
    val downloadStates: State<Map<Int, MKOLUpdateElement>> = _downloadStates

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    fun loadCityList() {
        _cityList.value = manager.getCityList()
        refreshStates()
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun downloadCity(cityId: Int) {
        manager.downloadCity(cityId)
        refreshStates()
    }

    fun pauseDownload(cityId: Int) {
        manager.pauseDownload(cityId)
        refreshStates()
    }

    fun removeCity(cityId: Int) {
        manager.removeCity(cityId)
        refreshStates()
    }

    fun refreshStates() {
        val states = mutableMapOf<Int, MKOLUpdateElement>()
        _cityList.value.forEach { city ->
            manager.getUpdateInfo(city.cityID)?.let { states[city.cityID] = it }
        }
        _downloadStates.value = states
    }

    val filteredCities: List<MKOLSearchRecord>
        get() {
            val q = _searchQuery.value.trim()
            if (q.isEmpty()) return _cityList.value
            return _cityList.value.filter {
                it.cityName.contains(q)
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapScreen(
    onBack: () -> Unit
) {
    val viewModel: OfflineMapViewModel = viewModel()
    val cityList by viewModel.cityList
    val downloadStates by viewModel.downloadStates
    val searchQuery by viewModel.searchQuery
    val filteredCities = remember(cityList, searchQuery) { viewModel.filteredCities }

    LaunchedEffect(Unit) {
        viewModel.loadCityList()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            viewModel.refreshStates()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("离线地图管理") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("搜索城市名称或拼音") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        val downloadedCount = downloadStates.values.count {
            it.status == MKOLUpdateElement.FINISHED
        }
        val downloadingCount = downloadStates.values.count {
            it.status == MKOLUpdateElement.DOWNLOADING
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoColumn("已下载", downloadedCount.toString())
                InfoColumn("下载中", downloadingCount.toString())
                InfoColumn("可下载", cityList.size.toString())
            }
        }

        if (cityList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "正在加载城市列表…",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredCities) { city ->
                    OfflineCityItem(
                        city = city,
                        state = downloadStates[city.cityID],
                        onDownload = { viewModel.downloadCity(city.cityID) },
                        onPause = { viewModel.pauseDownload(city.cityID) },
                        onRemove = { viewModel.removeCity(city.cityID) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun OfflineCityItem(
    city: MKOLSearchRecord,
    state: MKOLUpdateElement?,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onRemove: () -> Unit
) {
    val status = state?.status ?: MKOLUpdateElement.UNDEFINED
    val ratio = state?.ratio ?: 0
    val sizeMB = state?.size?.let { it / 1024 / 1024 } ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = city.cityName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                when (status) {
                    MKOLUpdateElement.FINISHED -> {
                        Text(
                            "已下载 ${sizeMB}MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    MKOLUpdateElement.DOWNLOADING -> {
                        Text(
                            "下载中 ${ratio}% (${sizeMB}MB)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = ratio / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MKOLUpdateElement.WAITING -> {
                        Text(
                            "等待中…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    MKOLUpdateElement.SUSPENDED -> {
                        Text(
                            "已暂停 ${ratio}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    else -> {
                        Text(
                            "未下载",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            when (status) {
                MKOLUpdateElement.FINISHED -> {
                    TextButton(onClick = onRemove, colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )) {
                        Text("删除")
                    }
                }
                MKOLUpdateElement.DOWNLOADING -> {
                    TextButton(onClick = onPause) {
                        Text("暂停")
                    }
                }
                    MKOLUpdateElement.SUSPENDED -> {
                        TextButton(onClick = onDownload) {
                            Text("继续")
                        }
                    }
                else -> {
                    TextButton(onClick = onDownload) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("下载")
                    }
                }
            }
        }
    }
}