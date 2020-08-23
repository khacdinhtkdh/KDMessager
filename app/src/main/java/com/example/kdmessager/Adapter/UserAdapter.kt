package com.example.kdmessager.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kdmessager.Controller.MainActivity
import com.example.kdmessager.Controller.MessageChatActivity
import com.example.kdmessager.Controller.VisitProfileActivity
import com.example.kdmessager.ModelClasses.Chat
import com.example.kdmessager.ModelClasses.Users
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.CHATS
import com.example.kdmessager.Ultilities.EXTRA_VISIT_ID
import com.example.kdmessager.Ultilities.SEND_IMAGE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.user_search_item_layout.view.*

class UserAdapter(private val context: Context, private val mUser: ArrayList<Users>, private val isChatCheck: Boolean) :
    RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    var lastMsg: String = ""
    var isSeen = false
    var lastMessageCheckSeen = false

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var userNameTxt = itemView?.findViewById<TextView>(R.id.search_username)
        var profileImage = itemView?.findViewById<CircleImageView>(R.id.search_profile_image)
        private var onlineImageView = itemView?.findViewById<CircleImageView>(R.id.search_online_image)
        var offlineImageView = itemView?.findViewById<CircleImageView>(R.id.search_offline_image)
        var lastMessageTxt = itemView?.findViewById<TextView>(R.id.search_last_message)
        var userLayout = itemView?.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.user_search_layout)
        fun bindUser(user: Users, holder: ViewHolder, i: Int) {
            userNameTxt?.text = user.username
            Picasso.get().load(user.profile).placeholder(R.drawable.profile).into(profileImage)

            if (user.status == "online") {
                onlineImageView!!.visibility = View.VISIBLE
                offlineImageView!!.visibility = View.GONE
            } else {
                onlineImageView!!.visibility = View.GONE
                offlineImageView!!.visibility = View.VISIBLE
            }

            if (isChatCheck) {
                lastMessageTxt!!.visibility = View.VISIBLE
                retrieveLastMessage(user.uid, lastMessageTxt)
            } else {
                lastMessageTxt!!.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_search_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        holder.bindUser(mUser[i], holder, i)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageChatActivity::class.java)
            intent.putExtra(EXTRA_VISIT_ID, mUser[i].uid)
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            val intent = Intent(context, VisitProfileActivity::class.java)
            intent.putExtra(EXTRA_VISIT_ID, mUser[i].uid)
            context.startActivity(intent)

            true
        }
    }

    private fun retrieveLastMessage(chatUserId: String, lastMessageTxt: TextView?) {
        lastMsg = "defaultMsg"
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child(CHATS)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (snapshot in p0.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (firebaseUser != null && chat != null) {
                        if (chat.receiver == firebaseUser.uid && chat.sender == chatUserId
                            || chat.receiver == chatUserId && chat.sender == firebaseUser.uid
                        ) {
                            lastMsg = chat.message
                            isSeen = chat.seen
                        }
                    }
                }

                when (lastMsg) {
                    "defaultMsg" -> lastMessageTxt!!.text = ""
                    SEND_IMAGE -> lastMessageTxt!!.text = "Image"
                    else -> lastMessageTxt!!.text = lastMsg
                }
                if (!isSeen) {
                    //lastMessageTxt!!.setTypeface(lastMessageTxt!!.typeface, Typeface.BOLD);
                    lastMessageTxt!!.typeface = Typeface.DEFAULT_BOLD
                }
                isSeen = false
                lastMsg = "defaultMsg"

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}