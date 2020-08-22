package com.example.kdmessager.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kdmessager.Adapter.UserAdapter
import com.example.kdmessager.ModelClasses.ChatList
import com.example.kdmessager.ModelClasses.Token
import com.example.kdmessager.ModelClasses.Users
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.CHAT_LIST
import com.example.kdmessager.Ultilities.TOKENS
import com.example.kdmessager.Ultilities.USERS
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult


class ChatsFragment : Fragment() {
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var userAdapter: UserAdapter
    private var mUser: ArrayList<Users> = ArrayList()
    private var userChatList: ArrayList<ChatList> = ArrayList()
    private lateinit var recyclerViewChatList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        recyclerViewChatList = view.findViewById(R.id.chat_chat_list)
        recyclerViewChatList.setHasFixedSize(true)
        recyclerViewChatList.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val refChatList = FirebaseDatabase.getInstance().reference
            .child(CHAT_LIST)
            .child(firebaseUser.uid)

        refChatList.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                userChatList.clear()
                for (snapshot in p0.children) {
                    val chatList = snapshot.getValue(ChatList::class.java)
                    userChatList.add(chatList!!)
                }
                retrieveChatList()
            }

        })

        val refToken = FirebaseInstanceId.getInstance().instanceId
        refToken.addOnSuccessListener { instanceIdResult ->
            val mToken = instanceIdResult.token
            updateToken(mToken)
        }
        //updateToken(FirebaseInstanceId.getInstance().token)
        return view
    }

    private fun updateToken(newToken: String?) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val ref = FirebaseDatabase.getInstance().reference.child(TOKENS)
        val token = Token(newToken!!)
        ref.child(firebaseUser.uid).setValue(token)

    }

    private fun retrieveChatList() {
        val ref = FirebaseDatabase.getInstance().reference.child(USERS)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                mUser.clear()
                for (snapshot in p0.children) {
                    val user = snapshot.getValue(Users::class.java)!!
                    for (eachChatList in userChatList) {
                        if (user.uid == eachChatList.id) {
                            mUser.add(user)
                        }
                    }
                }

                userAdapter = UserAdapter(context!!, mUser, true)
                recyclerViewChatList.adapter = userAdapter
            }

        })
    }
}