package com.daka.footprint.ui.achievement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daka.footprint.data.db.entity.AchievementEntity
import com.daka.footprint.viewmodel.AchievementViewModel

@Composable
fun AchievementScreen() {
    val viewModel: AchievementViewModel = viewModel()
    val achievements by viewModel.achievements.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()

    val grouped = achievements.groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度概览
        item {
            ProgressOverviewCard(userProgress)
        }

        // 分分类展示成就
        grouped.forEach { (category, list) ->
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(list) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
private fun ProgressOverviewCard(
    progress: com.daka.footprint.data.db.entity.UserProgressEntity?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("探索进度", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("打卡点", progress?.totalCheckinCount ?: 0)
                StatItem("点亮省份", progress?.activatedProvinceCount ?: 0)
                StatItem("点亮城市", progress?.activatedCityCount ?: 0)
                StatItem("重游进度", progress?.totalRevisitProgress ?: 0, suffix = "/10")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, suffix: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value$suffix",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AchievementCard(achievement: AchievementEntity) {
    val cardColor = if (achievement.isUnlocked)
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    else CardDefaults.cardColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = cardColor
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (achievement.isUnlocked) Icons.Filled.EmojiEvents
                else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (achievement.isUnlocked) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (!achievement.isUnlocked && achievement.target > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = (achievement.progress.toFloat() / achievement.target).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${achievement.progress}/${achievement.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            if (achievement.isUnlocked) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "已解锁",
                    tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}