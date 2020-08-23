package com.example.kdmessager.Controller

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kdmessager.Adapter.ChatsAdapter
import com.example.kdmessager.ModelClasses.*
import com.example.kdmessager.R
import com.example.kdmessager.Service.APIService
import com.example.kdmessager.Ultilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit = ""
    lateinit var firebaseUser: FirebaseUser
    lateinit var chatsAdapter: ChatsAdapter
    lateinit var chatList: ArrayList<Chat>
    lateinit var recyclerViewChats: RecyclerView
    lateinit var seenListener: ValueEventListener
    lateinit var referenceChats: DatabaseReference
    var notify = false
    var apiService: APIService? = null

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

        apiService = Client.Client.getClient(FCM_URL)!!.create(APIService::class.java)

        msg_progressBar.visibility = View.INVISIBLE
        intent = intent
        userIdVisit = intent.getStringExtra(EXTRA_VISIT_ID).toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        recyclerViewChats = findViewById(R.id.msg_list)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerViewChats.layoutManager = linearLayoutManager

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
                    Picasso.get().load(user!!.profile).into(msg_profile)

                    retrieveMessages(firebaseUser!!.uid, userIdVisit, user!!.profile)
                }
            }

        })

        msg_send.setOnClickListener {
            notify = true
            val message = msg_text.text.toString()
            if (message == "") {

            } else {
                sendMessageToUser(firebaseUser.uid, userIdVisit, message)
                msg_text.setText("")
            }
        }

        msg_attachFile.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        //seenMessage(userIdVisit)
    }

    private fun retrieveMessages(senderId: String, receiverId: String, imageUrl: String) {
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child(CHATS)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                chatList.clear()
                for (snapshot in p0.children) {
                    val chat = snapshot.getValue(Chat::class.java)!!
                    if (chat.receiver == senderId && chat.sender == receiverId
                        || chat.sender == senderId && chat.receiver == receiverId) {
                        chatList.add(chat)
                    }
                }
                chatsAdapter = ChatsAdapter(this@MessageChatActivity, chatList, imageUrl)
                recyclerViewChats.adapter = chatsAdapter
            }

        })

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
                }
            }
        // implement the push notifications using fcm//
        val ref = FirebaseDatabase.getInstance().reference
            .child(USERS).child(firebaseUser.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Users::class.java)
                if (notify) {
                    sendNotification(receiverId, user!!.username, message)
                }
                notify = false
            }

        })
    }

    private fun sendNotification(receiverId: String, username: String, message: String) {
        Log.d("SHIN", "sendNotification")
        val ref = FirebaseDatabase.getInstance().reference.child(TOKENS)
        val query = ref.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object  : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (snapshot in p0.children) {
                    val token = snapshot.getValue(Token::class.java)
                    val data = Data(firebaseUser!!.uid,
                        R.drawable.yasuo,
                        "$username:  $message",
                        "New Message",
                        userIdVisit
                    )

                    val notiContent = Content("$username:  $message", "abc")

                    //val sender = Sender(data,notiContent, token!!.token)
                    val sender = Sender(data, token!!.token)
                    Log.d("SHIN", "${sender.to}, ${sender.data.user}")
                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse> {
                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                TODO("Not yet implemented")
                            }

                            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                                Log.d("SHIN", response.code().toString())
                                if (response.code() == 200) {
                                    if (response.body()!!.success !== 1) {
                                        Toast.makeText(this@MessageChatActivity, "Failed, Nothing happen", Toast.LENGTH_SHORT).show()

                                    }
                                }
                            }

                        })
                }
            }

        })
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
                    messageHashMap[MESSAGE] = SEND_IMAGE
                    messageHashMap[RECEIVER] = userIdVisit
                    messageHashMap[SEEN] = false
                    messageHashMap[MESSAGE_ID] = messageId.toString()
                    messageHashMap[URL] = url

                    ref.child(CHATS).child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                msg_progressBar.visibility = View.INVISIBLE
                                // implement the push notifications using fcm//
                                val ref = FirebaseDatabase.getInstance().reference
                                    .child(USERS).child(firebaseUser.uid)
                                ref.addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(Users::class.java)
                                        if (notify) {
                                            sendNotification(userIdVisit, user!!.username, SEND_IMAGE)
                                        }
                                        notify = false
                                    }

                                })
                            }
                        }
                } else {
                    // Handle failures
                    // ...
                }
            }

        }
    }



    private fun seenMessage(userId: String) {
        referenceChats = FirebaseDatabase.getInstance().reference.child(CHATS)
        seenListener = referenceChats.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (snapshot in p0.children) {
                    val chat = snapshot.getValue(Chat::class.java)!!
                    if (chat.receiver == firebaseUser.uid && chat.sender == userId) {
                        val hashMap = HashMap<String, Any>()
                        hashMap[SEEN] = true
                        snapshot.ref.updateChildren(hashMap)
                    }
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        //Log.d("KD", "messageChatActivity: onResume")
        seenMessage(userIdVisit)
    }

    override fun onPause() {
        super.onPause()
        //Log.d("KD", "messageChatActivity: onPause")
        referenceChats.removeEventListener(seenListener)
    }
}
