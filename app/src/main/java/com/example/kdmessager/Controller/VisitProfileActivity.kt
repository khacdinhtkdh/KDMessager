package com.example.kdmessager.Controller

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kdmessager.ModelClasses.Users
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.EXTRA_VISIT_ID
import com.example.kdmessager.Ultilities.USER_IMAGES
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_profile.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

class VisitProfileActivity : AppCompatActivity() {
    private var userVisitId: String? = null
    private var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_profile)

        userVisitId = intent.getStringExtra(EXTRA_VISIT_ID)

        val userReference = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userVisitId!!)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    user = p0.getValue(Users::class.java)
                    visit_profile_username.text = user!!.username
                    Picasso.get().load(user!!.cover).into(visit_profile_coverImg)
                    Picasso.get().load(user!!.profile).into(visit_profile_profileImg)
                }
            }

        })

        visit_profile_fb.setOnClickListener {
            if (user != null) {
                val uri = Uri.parse(user!!.facebook)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        visit_profile_instagram.setOnClickListener {
            if (user != null) {
                val uri = Uri.parse(user!!.instagram)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        visit_profile_website.setOnClickListener {
            if (user != null) {
                val uri = Uri.parse(user!!.website)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        visit_profile_send_msg.setOnClickListener {
            if (user != null) {
                val uri = Uri.parse(user!!.facebook)
                val intent = Intent(this, MessageChatActivity::class.java)
                intent.putExtra(EXTRA_VISIT_ID, user!!.uid)
                startActivity(intent)
            }
        }
    }
}