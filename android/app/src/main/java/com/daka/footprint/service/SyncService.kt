package com.daka.footprint.service

import android.content.Context
import android.content.Intent
import androidx.work.*
import com.daka.footprint.DakaApplication
import com.daka.footprint.network.SyncManager
import java.util.concurrent.TimeUnit

class SyncService {

    companion object {
        private const val WORK_NAME = "daka_sync_work"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun triggerImmediateSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            SyncManager.syncCheckins(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}