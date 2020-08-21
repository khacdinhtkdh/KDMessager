package com.example.kdmessager.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kdmessager.Adapter.UserAdapter
import com.example.kdmessager.ModelClasses.Users

import com.example.kdmessager.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private lateinit var mUser: ArrayList<Users>
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = view.findViewById(R.id.search_list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.searchUserET)

        mUser = ArrayList()
        retrieveAllUsers()

        searchEditText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForUser(charSequence.toString().toLowerCase())
            }

        })

        return view
    }

    private fun retrieveAllUsers() {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                mUser.clear()
                if (searchEditText!!.text.toString() == "") {
                    for (snapshot in p0.children) {
                        val user = snapshot.getValue(Users::class.java)
                        if (user!!.uid != firebaseUserId) {
                            mUser.add(user)
                        }
                    }
                    if (context != null) {
                        userAdapter = UserAdapter(context!!, mUser, false)
                        recyclerView!!.adapter = userAdapter
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun searchForUser(str: String) {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUser = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                mUser.clear()
                for (snapshot in p0.children) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user!!.uid != firebaseUserId) {
                        mUser.add(user)
                    }
                }
                if (context != null) {
                    userAdapter = UserAdapter(context!!, mUser, false)
                    recyclerView!!.adapter = userAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
