package com.example.kdmessager.Controller

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.kdmessager.ModelClasses.Users
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import java.util.*
import kotlin.collections.HashMap

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit = ""
    lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)
        setSupportActionBar(toolbar_msgChat)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_msgChat.setNavigationOnClickListener {
            var intentHome = Intent(this, LoginActivity::class.java)
            startActivity(intentHome)
        }

        msg_progressBar.visibility = View.INVISIBLE
        intent = intent
        userIdVisit = intent.getStringExtra(EXTRA_VISIT_ID).toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val reference = FirebaseDatabase.getInstance().reference
            .child(USERS).child(userIdVisit)

        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(Users::class.java)
                    msg_username.text = user!!.username
                    Picasso.get().load(user.profile).into(msg_profile)
                }
            }

        })

        msg_send.setOnClickListener {
            val message = msg_text.text.toString()
            if (message == "") {

            } else {
                sendMessageToUser(firebaseUser.uid, userIdVisit, message)
                msg_text.setText("")
            }
        }

        msg_attachFile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }
    }

    private fun sendMessageToUser(senderId: String, receiverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any>()
        messageHashMap[SENDER] = senderId
        messageHashMap[MESSAGE] = message
        messageHashMap[RECEIVER] = receiverId
        messageHashMap[SEEN] = false
        messageHashMap[MESSAGE_ID] = messageKey.toString()
        messageHashMap[URL] = ""
        reference.child(CHATS)
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListRef = FirebaseDatabase.getInstance()
                        .reference
                        .child(CHAT_LIST)
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)
                    chatListRef.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                chatListRef.child(ID).setValue(userIdVisit)
                            }
                            val chatListReceiveRef = FirebaseDatabase.getInstance()
                                .reference
                                .child(CHAT_LIST)
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)
                            chatListReceiveRef.child(ID).setValue(firebaseUser!!.uid)
                        }

                    })

                    // implement the push notifications using fcm//
                    val ref = FirebaseDatabase.getInstance().reference
                        .child(USERS).child(firebaseUser.uid)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            msg_progressBar.visibility = View.VISIBLE

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance()
                .reference
                .child(CHAT_IMAGES)

            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")
            var uploadTask: UploadTask

            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                filePath.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val map = HashMap<String, Any>()
                    val url = downloadUri.toString()
                    val messageHashMap = HashMap<String, Any>()
                    messageHashMap[SENDER] = firebaseUser.uid
                    messageHashMap[MESSAGE] = "sent you an image."
                    messageHashMap[RECEIVER] = userIdVisit
                    messageHashMap[SEEN] = false
                    messageHashMap[MESSAGE_ID] = messageId.toString()
                    messageHashMap[URL] = url

                    ref.child(CHATS).child(messageId!!).setValue(messageHashMap)
                    msg_progressBar.visibility = View.INVISIBLE
                } else {
                    // Handle failures
                    // ...
                }
            }

        }
    }
}
