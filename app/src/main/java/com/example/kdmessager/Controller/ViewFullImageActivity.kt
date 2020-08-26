package com.example.kdmessager.Controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.EXTRA_URL
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {
    lateinit var fullImageView: ImageView
    lateinit var imageUrl: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)
        fullImageView = findViewById(R.id.view_full_image)
        imageUrl = intent.getStringExtra(EXTRA_URL).toString()


        Picasso.get().load(imageUrl).into(fullImageView)
        //Glide.with(this).asGif().load(R.drawable.animation_300_kebgkwu4).into(fullImageView)
    }
}