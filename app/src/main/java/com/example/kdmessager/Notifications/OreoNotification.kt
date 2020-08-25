package com.example.kdmessager.Notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.CHANNEL_ID
import com.example.kdmessager.Ultilities.CHANNEL_NAME


@RequiresApi(Build.VERSION_CODES.O)
class OreoNotification(base: Context?) : ContextWrapper(base){
    private var notificationManager: NotificationManager? = null
    private val soundUri: Uri = Uri.parse(
        "android.resource://" +
                applicationContext.packageName +
                "/" +
                R.raw.melodic
    )
    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        channel.enableLights(false)
        channel.enableVibration(true)
        channel.setSound(soundUri, audioAttributes)
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
        icon: String?
    ) : NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(soundUri)
            .setAutoCancel(true)
    }
}
