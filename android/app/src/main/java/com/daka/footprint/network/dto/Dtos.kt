package com.daka.footprint.network.dto

import com.google.gson.annotations.SerializedName

data class CheckinDto(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val province: String,
    @SerializedName("city_code") val cityCode: String,
    val city: String,
    val district: String,
    val address: String,
    val title: String,
    val content: String,
    @SerializedName("unlock_mode") val unlockMode: Int,
    @SerializedName("revisit_count") val revisitCount: Int,
    @SerializedName("is_force_unlocked") val isForceUnlocked: Boolean,
    @SerializedName("create_time") val createTime: Long,
    @SerializedName("update_time") val updateTime: Long,
    val media: List<MediaDto> = emptyList()
)

data class MediaDto(
    val id: String,
    @SerializedName("checkin_id") val checkinId: String,
    @SerializedName("media_type") val mediaType: Int,
    @SerializedName("remote_url") val remoteUrl: String?
)

data class UserProgressDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("total_checkin_count") val totalCheckinCount: Int,
    @SerializedName("total_revisit_progress") val totalRevisitProgress: Int,
    @SerializedName("activated_province_count") val activatedProvinceCount: Int,
    @SerializedName("activated_city_count") val activatedCityCount: Int
)

data class AchievementDto(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val target: Int,
    @SerializedName("is_unlocked") val isUnlocked: Boolean,
    @SerializedName("unlock_time") val unlockTime: Long?,
    val progress: Int
)

data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "success",
    val data: T? = null
) {
    val isSuccess get() = code == 0
}

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val userId: String)
data class SyncBatchRequest(
    val checkins: List<CheckinDto> = emptyList(),
    val achievements: List<AchievementDto> = emptyList(),
    val progress: UserProgressDto? = null
)