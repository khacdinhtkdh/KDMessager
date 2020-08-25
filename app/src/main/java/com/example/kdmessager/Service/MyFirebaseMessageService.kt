package com.example.kdmessager.Service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kdmessager.Controller.MessageChatActivity
import com.example.kdmessager.ModelClasses.Token
import com.example.kdmessager.Notifications.OreoNotification
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException


class MyFirebaseMessageService : FirebaseMessagingService() {
    private lateinit var firebaseUser: FirebaseUser
    override fun onNewToken(token: String) {
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
        var receiver: String? = ""
        var sender: String? = ""
        try {
            receiver = remoteMessage.data["receiver"]
            sender = remoteMessage.data["sender"]
            if (sender != null) {
                SENDER_NOTIFICATION = sender
            }
        } catch (e: JSONException) {

        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (CURRENT_CHATTING != sender) {
            var user: String? = ""
            var icon: String? = ""
            var title: String? = ""
            var body: String? = ""
            try {
                user = remoteMessage.data["sender"]
                icon = remoteMessage.data["icon"]
                title = remoteMessage.data["title"]
                body = remoteMessage.data["body"]
            } catch (e: JSONException) {

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendOreoNotification(user, icon, title, body)
            } else {
                //sendNotification(user, icon, title, body)
            }
        } else {
            ringStoneMessage()
        }
    }

    private fun ringStoneMessage() {
        val mediaPlayer = MediaPlayer.create(applicationContext, R.raw.text)
        mediaPlayer.start()

    }

    private fun sendNotification(user: String?, icon: String?, title: String?, body: String?) {
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
    private fun sendOreoNotification(user: String?, icon: String?, title: String?, body: String?) {
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()

        val intent = Intent(this, MessageChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString(EXTRA_VISIT_ID, user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)

        val oreoNotification = OreoNotification(this)
        val builder: NotificationCompat.Builder = oreoNotification
            .getOreoNotification(title!!, body, pendingIntent, icon)

        var i = 0
        if (j > 0) {
            i = j
        }

        oreoNotification.getManager!!.notify(i, builder.build())
    }
}