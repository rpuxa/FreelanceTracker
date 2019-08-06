package com.robobobo.apps.aws.freelancetracker.workers

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.robobobo.apps.aws.freelancetracker.database.MyDataBase
import com.robobobo.apps.aws.freelancetracker.log

class ClearOffersWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        log("Clearing started...")
        val offersIds = inputData.getIntArray(OFFERS_IDS) ?: error("Offers id needed")
        log("last step", offersIds.size)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(GetOfferWorker.NOTIFY_ID)
        val offersDao = MyDataBase.create(applicationContext).offersDao
        val before = offersDao.newCount()
        offersDao.markAsNotNew(offersIds)
        val after = offersDao.newCount()
        log("Cleared! Ids ", offersIds.joinToString(", "))
        log("Before   ", before)
        log("After   ", after)
        return Result.success()
    }

    companion object {
        const val OFFERS_IDS = "offers_ids"
    }
}