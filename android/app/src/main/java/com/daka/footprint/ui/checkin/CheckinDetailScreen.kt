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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daka.footprint.data.db.entity.CheckinPointEntity
import com.daka.footprint.map.NavigationManager
import com.daka.footprint.viewmodel.CheckinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinDetailScreen(
    checkin: CheckinPointEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val viewModel: CheckinViewModel = viewModel()
    val forceUnlockConfirm by viewModel.forceUnlockConfirm.collectAsState()
    var showForceUnlockDialog by remember { mutableStateOf(false) }

    val navManager = remember { NavigationManager(com.daka.footprint.DakaApplication.instance) }

    val isLocked = checkin.unlockMode == CheckinPointEntity.UNLOCK_MODE_LIMIT && !checkin.isForceUnlocked

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(checkin.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑")
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
                    Text("位置", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${checkin.province} ${checkin.city} ${checkin.district}", style = MaterialTheme.typography.bodyMedium)
                    Text(checkin.address, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val modeText = if (checkin.unlockMode == CheckinPointEntity.UNLOCK_MODE_FREE)
                            "无限制模式" else "500米限制模式"
                        AssistChip(onClick = {}, label = { Text(modeText) })
                        if (checkin.revisitCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(onClick = {}, label = { Text("重游 ${checkin.revisitCount} 次") })
                        }
                    }
                }
            }

            // 内容展示
            if (isLocked) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("内容已锁定", style = MaterialTheme.typography.titleMedium)
                        Text("进入500米范围自动解锁", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            } else {
                if (checkin.content.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(checkin.content, modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge)
                    }
                }
                // 媒体内容占位
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("图片/视频", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("媒体内容区域", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }

            // 导航按钮
            Button(
                onClick = { navManager.openBaiduMapApp(checkin.latitude, checkin.longitude) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("前往这里")
            }

            // 强制解除限制按钮（仅限制模式）
            if (checkin.unlockMode == CheckinPointEntity.UNLOCK_MODE_LIMIT && !checkin.isForceUnlocked) {
                OutlinedButton(
                    onClick = { showForceUnlockDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("解除位置限制")
                }
            }
        }
    }

    // 强制解除限制确认弹窗
    if (showForceUnlockDialog) {
        ForceUnlockDialog(
            cityName = checkin.city,
            onConfirm = {
                viewModel.forceUnlock(checkin.id)
                showForceUnlockDialog = false
            },
            onDismiss = { showForceUnlockDialog = false }
        )
    }
}

@Composable
private fun ForceUnlockDialog(
    cityName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("解除位置限制")
            }
        },
        text = {
            Column {
                Text("解除后该打卡点将永久取消500米查看限制")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ 惩罚：【$cityName】故地重游资格将永久冻结，无法再贡献全局进度",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                    Text("我已知晓后果，确认解除", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = checked,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("确认解除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}