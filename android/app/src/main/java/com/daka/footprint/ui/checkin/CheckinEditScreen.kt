package com.daka.footprint.ui.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.viewmodel.CheckinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinEditScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    latitude: Double? = null,
    longitude: Double? = null,
    editCheckin: CheckinPointEntity? = null
) {
    val viewModel: CheckinViewModel = viewModel()

    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val unlockMode by viewModel.unlockMode.collectAsState()
    val addressInfo by viewModel.addressInfo.collectAsState()
    val mediaList by viewModel.mediaList.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    LaunchedEffect(editCheckin) {
        editCheckin?.let { viewModel.loadForEdit(it) }
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null && editCheckin == null) {
            viewModel.setLocation(latitude, longitude)
        }
    }

    LaunchedEffect(viewModel.saveResult) {
        viewModel.saveResult.collect { onSaved() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editCheckin == null) "新建打卡" else "编辑打卡") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 位置信息
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("位置信息", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedLocation?.let { loc ->
                        Text("经纬度: ${loc.latitude}, ${loc.longitude}", style = MaterialTheme.typography.bodySmall)
                    }
                    addressInfo?.let { addr ->
                        Text("${addr.province} ${addr.city} ${addr.district}", style = MaterialTheme.typography.bodyMedium)
                        if (addr.address.isNotEmpty()) {
                            Text(addr.address, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // 标题
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 文字描述
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("记录这一刻…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // 媒体上传
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("图片/视频", style = MaterialTheme.typography.titleSmall)
                        Row {
                            IconButton(onClick = { /* 打开相机/相册选择图片 */ }) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = "拍照")
                            }
                            IconButton(onClick = { /* 打开相册选择视频 */ }) {
                                Icon(Icons.Filled.VideoLibrary, contentDescription = "视频")
                            }
                        }
                    }
                    if (mediaList.isEmpty()) {
                        Text("暂无媒体，点击图标添加", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    } else {
                        Text("已选 ${mediaList.size} 个媒体", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 解锁模式选择
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("解锁模式", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    UnlockModeOption(
                        selected = unlockMode == CheckinPointEntity.UNLOCK_MODE_FREE,
                        title = "无限制模式",
                        desc = "任何地点均可查看全部内容",
                        onClick = { viewModel.setUnlockMode(CheckinPointEntity.UNLOCK_MODE_FREE) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnlockModeOption(
                        selected = unlockMode == CheckinPointEntity.UNLOCK_MODE_LIMIT,
                        title = "500米限制模式",
                        desc = "进入500米范围自动解锁，支持故地重游",
                        onClick = { viewModel.setUnlockMode(CheckinPointEntity.UNLOCK_MODE_LIMIT) }
                    )
                }
            }

            // 保存按钮
            Button(
                onClick = { viewModel.saveCheckin() },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotEmpty() && selectedLocation != null && addressInfo != null
            ) {
                Text("保存打卡")
            }
            if (selectedLocation != null && addressInfo == null) {
                Text(
                    "正在解析位置信息，解析成功后才能保存…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun UnlockModeOption(
    selected: Boolean,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp, borderColor
        ),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = onClick)
                Text(title, style = MaterialTheme.typography.bodyLarge)
            }
            Text(desc, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 36.dp))
        }
    }
}