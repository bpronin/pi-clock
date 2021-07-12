package com.bopr.piclock

import android.app.Activity
import android.app.Notification.CATEGORY_SERVICE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import java.lang.System.currentTimeMillis
import kotlin.reflect.KClass

/**
 * Produces notifications.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Notifications(private val context: Context) {

    private val channelId by lazy {
        val name = "com.bopr.piclock.notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(
                    NotificationChannel(name, context.getString(R.string.status), IMPORTANCE_LOW)
                )
        }
        name
    }

    private val builder by lazy {
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(activityIntent(MainActivity::class))
            .setOngoing(true).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setCategory(CATEGORY_SERVICE)
                }
            }
    }

    val serviceNotification
        get() = builder
            .setWhen(currentTimeMillis())
            .setContentTitle(context.getString(R.string.service_running))
            .build()

    private fun activityIntent(activityClass: KClass<out Activity>): PendingIntent {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(context, activityClass.java))
            .getPendingIntent(0, FLAG_UPDATE_CURRENT)!!
    }

    companion object {

        const val SERVICE_NOTIFICATION_ID = 14558
    }

}