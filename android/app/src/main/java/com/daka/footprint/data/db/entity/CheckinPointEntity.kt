package com.daka.footprint.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkin_point")
data class CheckinPointEntity(
    @PrimaryKey val id: String,
    val latitude: Double,
    val longitude: Double,
    val province: String,
    val cityCode: String,
    val city: String,
    val district: String,
    val address: String,
    val title: String,
    val content: String,
    /** 解锁模式：0=无限制，1=500米限制 */
    val unlockMode: Int = 0,
    /** 打卡点自身重游次数（仅展示） */
    val revisitCount: Int = 0,
    /** 上次进入围栏时间戳，防重复触发 */
    val lastEnterTime: Long = 0L,
    /** 是否为强制解除限制 */
    val isForceUnlocked: Boolean = false,
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis(),
    /** 同步状态：0=未同步，1=已同步，2=已修改 */
    val syncStatus: Int = 0
) {
    companion object {
        const val UNLOCK_MODE_FREE = 0
        const val UNLOCK_MODE_LIMIT = 1
    }
}