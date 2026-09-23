package com.daka.footprint.network

import com.daka.footprint.network.dto.AchievementDto
import com.daka.footprint.network.dto.ApiResponse
import com.daka.footprint.network.dto.CheckinDto
import com.daka.footprint.network.dto.LoginRequest
import com.daka.footprint.network.dto.LoginResponse
import com.daka.footprint.network.dto.MediaDto
import com.daka.footprint.network.dto.SyncBatchRequest
import com.daka.footprint.network.dto.UserProgressDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("api/checkin/list")
    suspend fun getCheckinList(@Header("Authorization") token: String): ApiResponse<List<CheckinDto>>

    @POST("api/checkin/save")
    suspend fun saveCheckin(
        @Header("Authorization") token: String,
        @Body checkin: CheckinDto
    ): ApiResponse<CheckinDto>

    @POST("api/checkin/delete/{id}")
    suspend fun deleteCheckin(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Unit>

    @Multipart
    @POST("api/media/upload")
    suspend fun uploadMedia(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ApiResponse<MediaDto>

    @POST("api/achievement/list")
    suspend fun getAchievements(@Header("Authorization") token: String): ApiResponse<List<AchievementDto>>

    @POST("api/achievement/update")
    suspend fun updateAchievement(
        @Header("Authorization") token: String,
        @Body achievement: AchievementDto
    ): ApiResponse<Unit>

    @GET("api/progress")
    suspend fun getProgress(@Header("Authorization") token: String): ApiResponse<UserProgressDto>

    @POST("api/sync/batch")
    suspend fun syncBatch(
        @Header("Authorization") token: String,
        @Body request: SyncBatchRequest
    ): ApiResponse<SyncBatchRequest>
}