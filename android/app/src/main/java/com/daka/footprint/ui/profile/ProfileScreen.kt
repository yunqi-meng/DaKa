package com.daka.footprint.ui.profile

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
import com.daka.footprint.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = viewModel()
    val userProgress by viewModel.userProgress.collectAsState()
    val activatedProvinces by viewModel.activatedProvinces.collectAsState()
    val activatedCities by viewModel.activatedCities.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 用户信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Person, contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("足迹探索者", style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("记录每一处足迹", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }

        // 统计数据
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("足迹统计", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                StatRow("累计打卡", "${userProgress?.totalCheckinCount ?: 0} 个")
                StatRow("点亮省份", "$activatedProvinces 个")
                StatRow("点亮城市", "$activatedCities 个")
                StatRow("故地重游进度", "${userProgress?.totalRevisitProgress ?: 0}/10")
                StatRow("上传图片", "${userProgress?.totalPhotoCount ?: 0} 张")
            }
        }

        // 重游进度条
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("故地重游・十城", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (userProgress?.totalRevisitProgress ?: 0)
                LinearProgressIndicator(
                    progress = (progress.toFloat() / 10f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("$progress/10 城", style = MaterialTheme.typography.labelSmall)
            }
        }

        // 功能选项
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("数据同步") },
                    leadingContent = { Icon(Icons.Filled.Sync, contentDescription = null) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Divider()
                ListItem(
                    headlineContent = { Text("离线地图管理") },
                    leadingContent = { Icon(Icons.Filled.CloudDownload, contentDescription = null) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Divider()
                ListItem(
                    headlineContent = { Text("关于") },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 立即同步按钮
        Button(
            onClick = { viewModel.triggerSync() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.CloudSync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("立即同步数据")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary)
    }
}