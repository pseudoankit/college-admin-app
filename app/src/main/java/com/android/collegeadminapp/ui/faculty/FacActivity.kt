package com.android.collegeadminapp.ui.faculty

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.collegeadminapp.R
import com.android.collegeadminapp.util.FireBaseConstants
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_fac.*


class FacActivity : AppCompatActivity() {

    private val TAG = "factest"
    private lateinit var databaseReference: DatabaseReference
    private lateinit var facultyList: ArrayList<String>
    private lateinit var layoutManagerGroup: LinearLayoutManager
    private lateinit var groupAdapter : GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fac)

        init()
        getList()



    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(
            FireBaseConstants.FB_FACULTY
        )
        groupAdapter = GroupAdapter(this,databaseReference)
    }

    private fun getList() {

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                facultyList = arrayListOf()
                val data = snapshot.children
                data.forEach {
                    facultyList.add(0, it.key!!)
                }
                groupAdapter.addItems(facultyList)
                rv_main.layoutManager = LinearLayoutManager(this@FacActivity)
                rv_main.adapter = groupAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        databaseReference.addValueEventListener(listener)

    }
}