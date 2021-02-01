package com.android.collegeadminapp.ui.faculty

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUpdateFacultyBinding
import com.android.collegeadminapp.ui.faculty.AddUpdateFacultyActivity.Companion.IS_ADD
import com.android.collegeadminapp.ui.faculty.AddUpdateFacultyActivity.Companion.UPDATE_OBJ
import com.android.collegeadminapp.util.FireBaseConstants.FB_FACULTY
import com.android.collegeadminapp.util.hide
import com.android.collegeadminapp.util.show
import com.google.firebase.database.*

class UpdateFacultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateFacultyBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var facultyNameList: ArrayList<String>
    private lateinit var facultyListRvAdapter: FacultyListRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_faculty)

        init()

        showList()

        binding.fabAddFaculty.setOnClickListener {
            Intent(this, AddUpdateFacultyActivity::class.java).apply {
                this.putExtra(IS_ADD, true)
                startActivity(this)
            }
        }
    }

    private fun showList() {
        binding.progressBar.show()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                facultyNameList = arrayListOf()
                val data = snapshot.children
                data.forEach {
                    facultyNameList.add(0, it.key!!)
                }

                binding.progressBar.hide()
                facultyListRvAdapter.addItems(facultyNameList)
                binding.rvFacultyList.apply {
                    layoutManager = LinearLayoutManager(this@UpdateFacultyActivity)
                    adapter = facultyListRvAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.hide()
            }
        }
        databaseReference.addValueEventListener(listener)

    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(FB_FACULTY)
        facultyListRvAdapter = FacultyListRvAdapter(this, databaseReference)
    }
}