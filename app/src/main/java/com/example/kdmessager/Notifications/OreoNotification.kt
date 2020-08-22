package com.example.kdmessager.Notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kdmessager.Ultilities.CHANNEL_ID
import com.example.kdmessager.Ultilities.CHANNEL_NAME

@RequiresApi(Build.VERSION_CODES.O)
class OreoNotification(base: Context?) : ContextWrapper(base){
    private lateinit var notificationManager: NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.enableLights(false)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getManager!!.createNotificationChannel(channel)
    }

    val getManager: NotificationManager? get() {
        if (notificationManager == null) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return  notificationManager
    }

    fun getOreoNotification(
        title: String,
        body: String?,
        pendingIntent: PendingIntent?,
        soundUri: Uri?,
        icon: String?) : Notification.Builder {
        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(soundUri)
            .setAutoCancel(true)
    }
}
