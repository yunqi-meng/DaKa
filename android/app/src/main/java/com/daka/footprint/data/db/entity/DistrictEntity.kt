package com.daka.footprint.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "district")
data class DistrictEntity(
    @PrimaryKey val code: String,
    val name: String,
    /** 级别：1=省级，2=市级，3=区县 */
    val level: Int,
    val parentCode: String?,
    /** 边界坐标 JSON（缓存用） */
    val boundary: String? = null,
    /** 是否已点亮 */
    val isActivated: Boolean = false,
    /** 是否已贡献故地重游总进度 */
    val hasRevisitContributed: Boolean = false,
    /** 重游资格是否永久冻结 */
    val isRevisitFrozen: Boolean = false
) {
    companion object {
        const val LEVEL_PROVINCE = 1
        const val LEVEL_CITY = 2
        const val LEVEL_DISTRICT = 3
    }
}