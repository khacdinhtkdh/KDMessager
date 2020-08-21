package com.example.kdmessager.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kdmessager.Controller.MainActivity
import com.example.kdmessager.Controller.MessageChatActivity
import com.example.kdmessager.ModelClasses.Users
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.EXTRA_VISIT_ID
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.user_search_item_layout.view.*

class UserAdapter(private val context: Context, private val mUser: ArrayList<Users>, private val isChatCheck: Boolean) :
    RecyclerView.Adapter<UserAdapter.ViewHolder?>() {

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var userNameTxt = itemView?.findViewById<TextView>(R.id.search_username)
        var profileImage = itemView?.findViewById<CircleImageView>(R.id.search_profile_image)
        var onlineImageView = itemView?.findViewById<CircleImageView>(R.id.search_online_image)
        var lastMessageTxt = itemView?.findViewById<TextView>(R.id.search_last_message)

        fun bindUser(user: Users) {
            userNameTxt?.text = user.username
            Log.d("KDSHIN", user.username)
            Picasso.get().load(user.profile).placeholder(R.drawable.profile).into(profileImage)
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
        holder.bindUser(mUser[i])
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageChatActivity::class.java)
            intent.putExtra(EXTRA_VISIT_ID, mUser[i].uid)
            context.startActivity(intent)

        }

        holder.itemView.setOnLongClickListener {
            Toast.makeText(context, "Long click", Toast.LENGTH_SHORT).show()
            true
        }
    }
}