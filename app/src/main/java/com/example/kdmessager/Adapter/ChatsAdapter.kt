package com.example.kdmessager.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kdmessager.ModelClasses.Chat
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.SEND_IMAGE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.message_item_chat_left.view.*
import java.text.FieldPosition

class ChatsAdapter(val context: Context, private val chatList: ArrayList<Chat>, val imageUrl: String)
    :RecyclerView.Adapter<ChatsAdapter.ViewHolder>()
{
    var senderId: String = ""

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var profileImage = itemView?.findViewById<CircleImageView>(R.id.msg_item_profile)
        var showMessage = itemView?.findViewById<TextView>(R.id.msg_item_show_message)
        var leftImage = itemView?.findViewById<ImageView>(R.id.msg_item_left_image)
        var rightImage = itemView?.findViewById<ImageView>(R.id.msg_item_right_image)
        var seen = itemView?.findViewById<TextView>(R.id.msg_item_seen)

        fun onBindMessage(chat: Chat, i: Int) {
            Picasso.get().load(imageUrl).into(profileImage)
            // show image //
            if (chat.message == SEND_IMAGE && chat.url != "") {
                showMessage!!.visibility = View.GONE
                // right //
                if (chat.sender == senderId) {
                    rightImage!!.visibility = View.VISIBLE
                    Picasso.get().load(chat.url).into(rightImage)
                } else { // left //
                    leftImage!!.visibility = View.VISIBLE
                    Picasso.get().load(chat.url).into(leftImage)
                }
            } else { // show text //
                showMessage!!.text = chat.message
            }

            // sent and seen //
            if (i == chatList.size - 1) {
                if (chat.message == SEND_IMAGE && chat.url != "") {
                    val lp = seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 245, 10, 0)
                    seen!!.layoutParams =lp
                }
                if (chat.seen) {
                    seen!!.text = "seen"
                } else {
                    seen!!.text = "sent"
                }
            } else {
                seen!!.visibility = View.GONE
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        holder?.onBindMessage(chatList[i], i)
    }

    override fun getItemCount(): Int {
        return chatList.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(context).inflate(R.layout.message_item_chat_left, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.message_item_chat_right, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemViewType(i: Int): Int {
        senderId = FirebaseAuth.getInstance().currentUser!!.uid

        return if (chatList[i].sender == senderId) {
            1
        } else {
            0
        }
    }
}