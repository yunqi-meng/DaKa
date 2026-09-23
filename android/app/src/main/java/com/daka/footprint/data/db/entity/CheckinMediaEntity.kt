package com.daka.footprint.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkin_media",
    foreignKeys = [ForeignKey(
        entity = CheckinPointEntity::class,
        parentColumns = ["id"],
        childColumns = ["checkinId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("checkinId")]
)
data class CheckinMediaEntity(
    @PrimaryKey val id: String,
    val checkinId: String,
    /** 类型：1=图片，2=视频 */
    val mediaType: Int,
    val localPath: String?,
    val remoteUrl: String?,
    val createTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
    }
}