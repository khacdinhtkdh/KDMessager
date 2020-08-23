package com.example.kdmessager.Adapter

import android.content.*
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
import com.example.kdmessager.Controller.ViewFullImageActivity
import com.example.kdmessager.ModelClasses.Chat
import com.example.kdmessager.R
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
        var seen = itemView?.findViewById<TextView>(R.id.msg_item_seen)

        fun onBindMessage(chat: Chat, position: Int) {
            Picasso.get().load(imageUrl).into(profileImage)
            // show image //
            if (chat.message == SEND_IMAGE && chat.url != "") {
                showMessage!!.visibility = View.GONE
                // right //
                if (chat.sender == senderId) {
                    rightImage!!.visibility = View.VISIBLE
                    Picasso.get().load(chat.url).into(rightImage)

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
                    Picasso.get().load(chat.url).into(leftImage)

                    leftImage!!.setOnClickListener {
                        Toast.makeText(context, "On click", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, ViewFullImageActivity::class.java)
                        intent.putExtra(EXTRA_URL, chat.url)
                        context.startActivity(intent)
                    }

                    leftImage!!.setOnLongClickListener {
                        // Toast.makeText(context, "Long click", Toast.LENGTH_SHORT).show()

                        val option = arrayOf<CharSequence>(
                            "Download image",
                            "Cancel"
                        )
                        var builder = AlertDialog.Builder(itemView.context)
                        builder.setTitle("What do you want?")
                        builder.setItems(option, DialogInterface.OnClickListener { dialogInterface, i ->
                            if (i == 0) {
                                //deleteSentMessage(position, itemView.context)
                            } else if (i == 1) {
                                downloadSentImage(chat.url)
                            }
                        })
                        builder.show()
                        true

                    }
                }
            } else { // show text //
                showMessage!!.text = chat.message

                showMessage!!.setOnLongClickListener {
                    val option = if (chat.sender == senderId) {
                        arrayOf<CharSequence>(
                            "Copy",
                            "Delete",
                            "Cancel"
                        )
                    } else {
                        arrayOf<CharSequence>(
                            "Copy",
                            "Cancel"
                        )
                    }

                    var builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(option, DialogInterface.OnClickListener { _, i ->
                        when (i) {
                            0 -> {
                                clip = ClipData.newPlainText("text", showMessage!!.text)
                                clipboard.setPrimaryClip(clip!!)
                            }
                            1 -> {
                                if (chat.sender == senderId) {
                                    deleteSentMessage(position, itemView.context, "")
                                }
                            }
                        }
                    })
                    builder.show()
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
        val storage = Firebase.storage
        val storageRef = storage.reference
        val pathReference = storageRef.child(url)
        val rootPath = File(context.externalCacheDir!!.absolutePath, "/KD Messenger")
        val localFile = File(rootPath, System.currentTimeMillis().toString() + ".png")
        //val gsReference = storage.getReferenceFromUrl("gs://")
        //val ONE_MEGABYTE: Long = 1024 * 1024
        pathReference.getFile(localFile).addOnSuccessListener {
            Toast.makeText(context, "Download success", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {

        }
    }
}