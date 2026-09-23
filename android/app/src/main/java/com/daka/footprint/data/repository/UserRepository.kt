package com.daka.footprint.data.repository

import com.daka.footprint.data.db.dao.UserProgressDao
import com.daka.footprint.data.db.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userProgressDao: UserProgressDao) {

    companion object {
        const val CURRENT_USER_ID = "local_user"
        const val MAX_REVISIT_PROGRESS = 10
    }

    fun getProgressFlow(): Flow<UserProgressEntity?> = userProgressDao.getFlow(CURRENT_USER_ID)

    suspend fun getProgress(): UserProgressEntity? = userProgressDao.get(CURRENT_USER_ID)

    suspend fun initIfNeeded() {
        if (userProgressDao.get(CURRENT_USER_ID) == null) {
            userProgressDao.insert(UserProgressEntity(userId = CURRENT_USER_ID))
        }
    }

    suspend fun incrementCheckinCount() {
        userProgressDao.incrementCheckinCount(CURRENT_USER_ID, System.currentTimeMillis())
    }

    suspend fun decrementCheckinCount() {
        userProgressDao.decrementCheckinCount(CURRENT_USER_ID, System.currentTimeMillis())
    }

    suspend fun incrementRevisitProgress() {
        val current = userProgressDao.getRevisitProgress(CURRENT_USER_ID)
        if (current < MAX_REVISIT_PROGRESS) {
            userProgressDao.incrementRevisitProgress(CURRENT_USER_ID, System.currentTimeMillis())
        }
    }

    suspend fun updateProvinceCount(count: Int) {
        userProgressDao.updateProvinceCount(CURRENT_USER_ID, count, System.currentTimeMillis())
    }

    suspend fun updateCityCount(count: Int) {
        userProgressDao.updateCityCount(CURRENT_USER_ID, count, System.currentTimeMillis())
    }

    suspend fun addPhotoCount(delta: Int) {
        userProgressDao.addPhotoCount(CURRENT_USER_ID, delta, System.currentTimeMillis())
    }

    suspend fun getRevisitProgress(): Int = userProgressDao.getRevisitProgress(CURRENT_USER_ID)
}