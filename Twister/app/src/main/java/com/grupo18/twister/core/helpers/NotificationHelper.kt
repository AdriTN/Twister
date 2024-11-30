package com.grupo18.twister.core.helpers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.grupo18.twister.R

object NotificationHelper {
    fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "twister_channel"
        val notificationId = System.currentTimeMillis().toInt() // Genera un ID único

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification) // Asegúrate de tener este ícono en res/drawable
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}