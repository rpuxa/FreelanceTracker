package com.robobobo.apps.aws.freelancetracker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.robobobo.apps.aws.freelancetracker.database.MyDataBase
import com.robobobo.apps.aws.freelancetracker.database.Offer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ClearReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        runBlocking(Dispatchers.IO) {
            @Suppress("UNCHECKED_CAST")
            val offers = intent.extras?.get(OFFERS) as? List<Offer> ?: error("Offers needed")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(RequestBroadcast.NOTIFY_ID)
            MyDataBase.create(context).offersDao.markAsNotNew(offers)
        }
    }

    companion object {
        const val OFFERS = "offers"
    }
}
