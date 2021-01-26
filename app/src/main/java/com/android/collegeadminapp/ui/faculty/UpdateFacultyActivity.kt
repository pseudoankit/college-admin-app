package com.android.collegeadminapp.ui.faculty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUpdateFacultyBinding
import com.android.collegeadminapp.ui.faculty.AddFacultyActivity.Companion.FB_CHILD_FACULTY
import com.android.collegeadminapp.util.hide
import com.android.collegeadminapp.util.show
import com.android.collegeadminapp.util.toast
import com.google.firebase.database.*

class UpdateFacultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateFacultyBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_faculty)

        init()

        rv()

        binding.fabUpdateFaculty.setOnClickListener {
            startActivity(Intent(this, AddFacultyActivity::class.java))
        }
    }

    private fun rv() {
        //todo in single rv
        setRv(getString(R.string.cse), binding.rvDepartmentCS, binding.CSNoData)
        setRv(getString(R.string.ece), binding.rvDepartmentECE, binding.ECENoData)
        setRv(getString(R.string.mechanical), binding.rvDepartmentME, binding.MENoData)
    }

    private fun setRv(child: String, rv: RecyclerView, noData: View) {
        val adapter = FacultyAdapter()
        var list: MutableList<Faculty>
        val dbPath = databaseReference.child(child)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list = mutableListOf()
                if (!snapshot.exists()) {
                    noData.show()
                    rv.hide()
                } else {
                    noData.hide()
                    rv.show()
                    snapshot.children.forEach { snap ->
                        list.add(snap.getValue(Faculty::class.java)!!)
                    }
                    rv.setHasFixedSize(true)
                    rv.layoutManager = LinearLayoutManager(this@UpdateFacultyActivity)
                    adapter.addItems(list)
                    rv.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                this@UpdateFacultyActivity.toast(error.toString())
            }
        }
        dbPath.addValueEventListener(listener)
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(FB_CHILD_FACULTY)
    }
}