package com.robobobo.apps.aws.freelancetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.robobobo.apps.aws.freelancetracker.workers.ClearOffersWorker

class ClearReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val offers = intent.extras?.get(OFFERS) as? IntArray ?: error("Offers ids needed")
        log("second step", offers.size)
        val worker = OneTimeWorkRequestBuilder<ClearOffersWorker>()
            .setInputData(
                Data.Builder()
                    .putIntArray(ClearOffersWorker.OFFERS_IDS, offers)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(worker)
    }

    companion object {
        const val OFFERS = "offers"
    }
}
