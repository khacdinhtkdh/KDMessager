package com.example.kdmessager.Adapter

import android.content.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kdmessager.Controller.ViewFullImageActivity
import com.example.kdmessager.ModelClasses.Chat
import com.example.kdmessager.R
import com.example.kdmessager.Service.DownloadImage
import com.example.kdmessager.Ultilities.CHATS
import com.example.kdmessager.Ultilities.EXTRA_URL
import com.example.kdmessager.Ultilities.SEND_IMAGE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ChatsAdapter(val context: Context, private val chatList: ArrayList<Chat>, val imageUrl: String) :
    RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {
    var senderId: String = ""
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var clip: ClipData? = null

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var profileImage = itemView?.findViewById<CircleImageView>(R.id.msg_item_profile)
        var showMessage = itemView?.findViewById<TextView>(R.id.msg_item_show_message)
        var leftImage = itemView?.findViewById<ImageView>(R.id.msg_item_left_image)
        var rightImage = itemView?.findViewById<ImageView>(R.id.msg_item_right_image)
        var likeImage = itemView?.findViewById<ImageView>(R.id.msg_emoji_heart)
        var seen = itemView?.findViewById<TextView>(R.id.msg_item_seen)

        fun onBindMessage(chat: Chat, position: Int) {
            Picasso.get().load(imageUrl).into(profileImage)
            // show image //
            if (chat.message == SEND_IMAGE && chat.url != "") {
                showMessage!!.visibility = View.GONE
                // right //
                if (chat.sender == senderId) {
                    rightImage!!.visibility = View.VISIBLE
                    Glide.with(itemView).load(chat.url).into(rightImage!!)

                    rightImage!!.setOnClickListener {
                        //Toast.makeText(context, "On click", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, ViewFullImageActivity::class.java)
                        intent.putExtra(EXTRA_URL, chat.url)
                        context.startActivity(intent)
                    }

                    rightImage!!.setOnLongClickListener {
                        // Toast.makeText(context, "Long click", Toast.LENGTH_SHORT).show()

                        val option = arrayOf<CharSequence>(
                            "Delete image",
                            "Download image",
                            "Cancel"
                        )
                        var builder = AlertDialog.Builder(itemView.context)
                        builder.setTitle("What do you want?")
                        builder.setItems(option, DialogInterface.OnClickListener { _, i ->
                            if (i == 0) {
                                deleteSentMessage(position, itemView.context, chat.url)
                            } else if (i == 1) {
                                downloadSentImage(chat.url)
                            }
                        })
                        builder.show()
                        true

                    }

                } else { // left //
                    leftImage!!.visibility = View.VISIBLE
                    Glide.with(itemView).load(chat.url).into(leftImage!!)

                    leftImage!!.setOnClickListener {
                        val intent = Intent(context, ViewFullImageActivity::class.java)
                        intent.putExtra(EXTRA_URL, chat.url)
                        context.startActivity(intent)
                    }

                    leftImage!!.setOnLongClickListener {
                        val option = arrayOf<CharSequence>(
                            "Download image",
                            "Cancel"
                        )
                        var builder = AlertDialog.Builder(itemView.context)
                        builder.setTitle("What do you want?")
                        builder.setItems(option, DialogInterface.OnClickListener { _, i ->
                            if (i == 0) {
                                downloadSentImage(chat.url)
                            } else if (i == 1) {

                            }
                        })
                        builder.show()
                        true

                    }
                }
            } else { // show text //
                showMessage!!.visibility = View.VISIBLE
                showMessage!!.text = chat.message
                if (leftImage != null) {
                    leftImage!!.visibility = View.GONE
                }

                if (rightImage != null) {
                    rightImage!!.visibility = View.GONE
                }

                if (chat.sender == senderId && profileImage != null) {
                    profileImage!!.visibility = View.GONE
                }

                showMessage!!.setOnLongClickListener {
                    val option = if (chat.sender == senderId) {
                        arrayOf<CharSequence>(
                            "Copy",
                            "Like",
                            "Delete",
                            "Cancel"
                        )
                    } else {
                        arrayOf<CharSequence>(
                            "Copy",
                            "Like",
                            "Cancel"
                        )
                    }
                    likeImage!!.visibility = View.VISIBLE
                    showReactionMessage(likeImage!!)
//                    var builder = AlertDialog.Builder(itemView.context)
//                    builder.setTitle("What do you want?")
//                    builder.setItems(option, DialogInterface.OnClickListener { _, i ->
//                        when (i) {
//                            0 -> {
//                                clip = ClipData.newPlainText("text", showMessage!!.text)
//                                clipboard.setPrimaryClip(clip!!)
//                            }
//                            2 -> {
//                                if (chat.sender == senderId) {
//                                    deleteSentMessage(position, itemView.context, "")
//                                }
//                            }
//                            1 -> {
//                                if (likeImage != null) {
//                                    likeImage!!.visibility = View.VISIBLE
//                                    Glide.with(itemView).asGif().load(R.drawable.animation_300_kebgkwu4)
//                                        .into(likeImage!!)
//                                }
//                            }
//                        }
//                    })
//                    builder.show()
                    true
                }

            }

            // sent and seen //
            if (position == chatList.size - 1) {
                if (chat.message == SEND_IMAGE && chat.url != "") {
                    val lp = seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 245, 10, 0)
                    seen!!.layoutParams = lp
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

    private fun showReactionMessage(react: ImageView) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.message_emoji, null)
        builder.setView(dialogView)
        builder.setCancelable(true)
        val alertDialog  = builder.show()

        val reactLove = dialogView.findViewById<ImageView>(R.id.react_love)
        val reactCare = dialogView.findViewById<ImageView>(R.id.react_care)
        val reactLike = dialogView.findViewById<ImageView>(R.id.react_like)
        val reactHaha = dialogView.findViewById<ImageView>(R.id.react_haha)
        val reactSad = dialogView.findViewById<ImageView>(R.id.react_sad)
        val reactWow = dialogView.findViewById<ImageView>(R.id.react_wow)


        reactLove.setOnClickListener {
            Log.d("KDSHIN", "click on heart")
            Glide.with(context).load(R.drawable.react_love).into(react)
            alertDialog.dismiss()
        }
        reactCare.setOnClickListener {
            Log.d("KDSHIN", "click on care")
            Glide.with(context).load(R.drawable.react_care).into(react)
            alertDialog.dismiss()
        }
        reactLike.setOnClickListener {
            Log.d("KDSHIN", "click on like")
            Glide.with(context).load(R.drawable.react_like).into(react)
            alertDialog.dismiss()
        }
        reactHaha.setOnClickListener {
            Log.d("KDSHIN", "click on haha")
            Glide.with(context).load(R.drawable.react_haha).into(react)
            alertDialog.dismiss()
        }
        reactSad.setOnClickListener {
            Log.d("KDSHIN", "click on sad")
            Glide.with(context).load(R.drawable.react_sad).into(react)
            alertDialog.dismiss()
        }
        reactWow.setOnClickListener {
            Log.d("KDSHIN", "click on wow")
            Glide.with(context).load(R.drawable.react_wow).into(react)
            alertDialog.dismiss()
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

    private fun deleteSentMessage(position: Int, context: Context, url: String) {

        if (url != "") {
            val refUrl = FirebaseStorage.getInstance().getReferenceFromUrl(url).delete()
        }

        val ref = FirebaseDatabase.getInstance().reference
            .child(CHATS).child(chatList[position].messageId).removeValue()
        ref.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun downloadSentImage(url: String) {
        DownloadImage(context).downloadImage(url)
    }
}