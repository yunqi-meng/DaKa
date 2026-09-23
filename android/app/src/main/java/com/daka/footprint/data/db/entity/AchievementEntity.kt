package com.daka.footprint.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val target: Int,
    val isUnlocked: Boolean = false,
    val unlockTime: Long? = null,
    val progress: Int = 0
) {
    companion object {
        const val CAT_BASIC = "基础数量类"
        const val CAT_REGION = "地域探索类"
        const val CAT_REVISIT = "故地重游类"
        const val CAT_CITY = "城市收集类"
        const val CAT_MEDIA = "媒体收藏类"
        const val CAT_SPECIAL = "特殊探索类"

        const val ID_FIRST_CHECKIN = "first_checkin"
        const val ID_HUNDRED = "hundred_checkin"
        const val ID_NORTH_CHINA = "north_china"
        const val ID_ALL_CHINA = "all_china"
        const val ID_REVISIT_ONE = "revisit_one"
        const val ID_REVISIT_FULL = "revisit_full"
        const val ID_TIER1 = "tier1_city"
        const val ID_PHOTO = "photo_master"
        const val ID_BORDER = "border_explorer"
    }
}