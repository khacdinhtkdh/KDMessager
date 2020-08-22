package com.example.kdmessager.Service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kdmessager.Controller.MessageChatActivity
import com.example.kdmessager.ModelClasses.Token
import com.example.kdmessager.Notifications.OreoNotification
import com.example.kdmessager.Ultilities.CHANNEL_ID
import com.example.kdmessager.Ultilities.EXTRA_VISIT_ID
import com.example.kdmessager.Ultilities.TOKENS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessageService: FirebaseMessagingService() {
    private lateinit var firebaseUser: FirebaseUser
    override fun onNewToken(token: String) {
        Log.d("SHIN", "New TOKEN: $token")
        //super.onNewToken(token)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        if (firebaseUser != null) {
            updateToken(token)
        }
    }

    private fun updateToken(newToken: String) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val ref = FirebaseDatabase.getInstance().reference.child(TOKENS)
        val token = Token(newToken)
        ref.child(firebaseUser.uid).setValue(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val sented = remoteMessage.data["sented"]
        val user = remoteMessage.data["user"]

        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPref.getString("currentUser", "none")

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        Log.d("SHIN", "receiver notification: ${firebaseUser.uid} $sented")
        if (firebaseUser != null && sented == firebaseUser!!.uid) {
            if (currentOnlineUser != user) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    sendNotification(remoteMessage)
                }
            }
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString(EXTRA_VISIT_ID, user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(defaultSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationCompat = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if (j > 0) {
            i = j
        }
        notificationCompat.notify(i, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString(EXTRA_VISIT_ID, user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)
        val builder: Notification.Builder = oreoNotification
            .getOreoNotification(title!!, body, pendingIntent, defaultSound, icon)

        var i = 0
        if (j > 0) {
            i = j
        }

        oreoNotification.getManager!!.notify(i, builder.build())
    }
}