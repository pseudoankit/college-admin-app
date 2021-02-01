package com.android.collegeadminapp.ui.faculty

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.collegeadminapp.R
import com.android.collegeadminapp.adapter.BaseRVAdapter
import com.android.collegeadminapp.databinding.LayoutItemFacultyListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class GroupAdapter(val activity: Activity, val dbRef: DatabaseReference) :
    BaseRVAdapter<String, LayoutItemFacultyListBinding>() {

    private lateinit var facultyList: ArrayList<Faculty>

    override fun layout() = R.layout.layout_item_faculty_list

    override fun onBindViewHolder(
        holder: Companion.BaseViewHolder<LayoutItemFacultyListBinding>,
        position: Int
    ) {
        holder.binding.tvFacultyDepartment.text = list[position]
        showData(list[position], holder.binding.rvFacultyDepartment)

    }

    private fun showData(department: String, rv: RecyclerView) {
        val adapter = MemberAdapter()
        val dbPath = dbRef.child(department)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                facultyList = arrayListOf()
                snapshot.children.forEach { snap ->
                    facultyList.add(snap.getValue(Faculty::class.java)!!)
                }
                adapter.addMember(facultyList)
                rv.layoutManager = LinearLayoutManager(activity)
                rv.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        dbPath.addValueEventListener(listener)
    }
}