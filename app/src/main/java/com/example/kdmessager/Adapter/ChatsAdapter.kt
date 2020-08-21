package com.example.kdmessager.Adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kdmessager.ModelClasses.Chat

class ChatsAdapter(val context: Context, val chatList: ArrayList<Chat>, val imageUrl: String)
    :RecyclerView.Adapter<ChatsAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }


    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }
}