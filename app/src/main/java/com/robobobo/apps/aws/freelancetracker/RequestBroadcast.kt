package com.robobobo.apps.aws.freelancetracker

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.robobobo.apps.aws.freelancetracker.database.MyDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.Serializable


class RequestBroadcast : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        runBlocking(Dispatchers.IO) {
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
                        putExtra(ClearReceiver.OFFERS, offers as Serializable)
                    },
                    0
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
        }
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
            CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(title)
        bigTextStyle.bigText(body)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
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
        fun start(context: Context) {
            val manager = context.alarmManager
            stop(context)
            manager.setRepeating(
                AlarmManager.RTC_WAKEUP, 5_000, 60_000,
                pendingIntent(context)
            )
        }

        fun stop(context: Context) {
            val manager = context.alarmManager
            val pendingIntent = pendingIntent(context)
            manager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        fun isRunning(context: Context) =
            PendingIntent.getBroadcast(
                context,
                0,
                intent(context),
                PendingIntent.FLAG_NO_CREATE
            ) != null

        private val Context.alarmManager get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        private fun pendingIntent(context: Context): PendingIntent {
            val intent = intent(context)
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        fun intent(context: Context) = Intent(context, RequestBroadcast::class.java)

        const val NOTIFY_ID = 1060896178
        private const val CHANNEL_ID = "freelance-app"
    }
}
