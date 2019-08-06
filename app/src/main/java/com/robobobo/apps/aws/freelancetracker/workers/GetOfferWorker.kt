package com.robobobo.apps.aws.freelancetracker.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.robobobo.apps.aws.freelancetracker.*
import com.robobobo.apps.aws.freelancetracker.R
import com.robobobo.apps.aws.freelancetracker.database.MyDataBase
import java.util.concurrent.TimeUnit

class GetOfferWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        log("Getting offers...")
        val context = applicationContext
        val dao = MyDataBase.create(context).offersDao
        dao.clearOld()
        val current = FreelanceMarket.loadAllOffers()
        val offers = dao.addNew(current)
        if (offers.isNotEmpty()) {
            val offer = offers.first()
            val many = offers.size > 1
            val myIntent = if (many) {
                Intent(context, MainActivity::class.java)
            } else {
                Intent(
                    context,
                    OfferActivity::class.java
                ).apply {
                    putExtra(OfferActivity.OFFER, offer)
                    putExtra(OfferActivity.FROM_NOTIFICATION, true)
                }
            }
            val title = if (many) "You got ${offers.size} new offers!" else offer.title
            val description = if (many) "Click to watch" else offer.description
            val buttonIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, ClearReceiver::class.java).apply {
                    val toIntArray = offers.map { it.id }.toIntArray()
                    log("first step", toIntArray.size)
                    putExtra(ClearReceiver.OFFERS, toIntArray)
                },
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            showNotification(
                context,
                title,
                description,
                myIntent,
                "Mark as read",
                buttonIntent
            )
        }

        log("Offers get! Count: ", offers.size)
        return Result.success()
    }


    private fun showNotification(
        context: Context,
        title: String,
        body: String,
        intent: Intent,
        buttonName: String,
        buttonIntent: PendingIntent
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(
                R.string.app_name
            ), NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(title)
        bigTextStyle.bigText(body)

        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.notif)
            .setStyle(bigTextStyle)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(resultPendingIntent)
            .addAction(R.drawable.notif, buttonName, buttonIntent)
            .setAutoCancel(true)


        notificationManager.notify(NOTIFY_ID, builder.build())
    }

    companion object {

        private const val TAG = "GetOffersWorker"

        fun start(context: Context) {
            if (isRunning(context))
                return

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val worker =
                PeriodicWorkRequestBuilder<GetOfferWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueue(worker)
        }

        fun stop(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
        }

        fun isRunning(context: Context): Boolean {
            val manager = WorkManager.getInstance(context).getWorkInfosByTag(TAG)

            val state = manager.get().firstOrNull()?.state ?: return false
            return state != WorkInfo.State.CANCELLED
        }

        const val NOTIFY_ID = 1060896178
        private const val CHANNEL_ID = "freelance-app"
    }
}