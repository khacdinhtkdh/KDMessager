package com.example.kdmessager.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.kdmessager.ModelClasses.Users

import com.example.kdmessager.R
import com.example.kdmessager.Ultilities.COVER_IMAGE_URL
import com.example.kdmessager.Ultilities.PROFILE_IMAGE_URL
import com.example.kdmessager.Ultilities.USER_IMAGES
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {
    lateinit var userReference: DatabaseReference
    lateinit var firebaseUser: FirebaseUser
    private lateinit var imageUri: Uri
    private lateinit var storageRef: StorageReference
    private lateinit var coverCheck: String
    private lateinit var socialCheck: String
    lateinit var settingListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        view.setting_spinner.visibility = View.VISIBLE

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        userReference = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(firebaseUser.uid)
        storageRef = FirebaseStorage.getInstance().reference
            .child(USER_IMAGES)

        settingListener = userReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(Users::class.java)
                    view.setting_username.text = user!!.username
//                    if (context != null) {
                        Glide.with(view).load(user.cover).into(view.setting_coverImg)
                        Glide.with(view).load(user.profile).into(view.setting_profileImg)
//                    }
                }
            }

        })

        view.setting_profileImg.setOnClickListener {
            coverCheck = "profile"
            pickImage()
        }

        view.setting_coverImg.setOnClickListener {
            coverCheck = "cover"
            pickImage()
        }

        view.setting_fb.setOnClickListener {
            socialCheck = "facebook"
            setSocialLink()
        }

        view.setting_instagram.setOnClickListener {
            socialCheck = "instagram"
            setSocialLink()
        }

        view.setting_website.setOnClickListener {
            socialCheck = "website"
            setSocialLink()
        }

        return view
    }

    private fun setSocialLink() {
        val builder = AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        val editText = EditText(context)
        when (socialCheck) {
            "website" -> {
                builder.setTitle("URL website:")
                editText.hint = "e.g kdshin.com.vn"
            }
            "facebook" -> {
                builder.setTitle("Facebook ID:")
                editText.hint = "e.g facebook.com/kdshin"
            }
            "instagram" -> {
                builder.setTitle("Instagram ID:")
                editText.hint = "e.g facebook.com/kdshin"
            }
        }
        builder.setView(editText)
        builder.setPositiveButton("Create", DialogInterface.OnClickListener { dialogInterface, i ->
            val str = editText.text.toString()
            if (str == "") {
                Toast.makeText(context, "Please fill your information!", Toast.LENGTH_SHORT).show()
            } else {
                saveSocialLink(str)
            }
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.cancel()
        })

        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val map = HashMap<String, Any>()
        when (socialCheck) {
            "facebook" -> {
                map["facebook"] = "https://facebook.com/$str"
            }
            "instagram" -> {
                map["instagram"] = "https://facebook.com/$str"
            }
            "website" -> {
                map["website"] = "$str"
            }
        }
        userReference.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Update successful", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 438)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            view!!.setting_spinner.visibility = View.VISIBLE
            Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val fileRef = storageRef.child(System.currentTimeMillis().toString() + ".jpg")
        var uploadTask: UploadTask
        uploadTask = fileRef.putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            fileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val map = HashMap<String, Any>()
                val url = downloadUri.toString()
                if (coverCheck == "cover") {
                    map["cover"] = url
                    userReference.updateChildren(map)
                    coverCheck = ""
                } else if (coverCheck == "profile") {
                    map["profile"] = url
                    userReference.updateChildren(map)
                    coverCheck = ""
                }
                view!!.setting_spinner.visibility = View.INVISIBLE
            } else {
                // Handle failures
                // ...
            }
        }

    }

    override fun onPause() {
        super.onPause()
        userReference.removeEventListener(settingListener)
    }
}
