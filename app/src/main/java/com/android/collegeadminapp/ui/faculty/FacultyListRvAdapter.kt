package com.android.collegeadminapp.ui.faculty

import android.content.Context
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.collegeadminapp.R
import com.android.collegeadminapp.adapter.BaseNestedRVAdapter
import com.android.collegeadminapp.databinding.LayoutItemRvListBinding
import com.android.collegeadminapp.util.hide
import com.android.collegeadminapp.util.show
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class FacultyListRvAdapter(val context: Context, private val dbRef: DatabaseReference) :
    BaseNestedRVAdapter<String, LayoutItemRvListBinding>() {

    override fun onBindViewHolder(
        holder: Companion.BaseViewHolder<LayoutItemRvListBinding>,
        position: Int
    ) {
        holder.binding.tvFacultyDepartment.text = nestedList[position]
        holder.binding.progressBar.show()
        showData(nestedList[position], holder.binding.rvFacultyDepartment,holder.binding.progressBar)

    }

    private fun showData(department: String, rv: RecyclerView, progressBar: ProgressBar) {
        var facultyList: ArrayList<Faculty>
        val adapter = FacultyRvAdapter(context)
        val dbPath = dbRef.child(department)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                facultyList = arrayListOf()
                snapshot.children.forEach { snap ->
                    facultyList.add(snap.getValue(Faculty::class.java)!!)
                }
                adapter.addItems(facultyList)
                progressBar.hide()
                rv.layoutManager = LinearLayoutManager(context)
                rv.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.hide()
            }
        }
        dbPath.addValueEventListener(listener)
    }

    override fun layout() = R.layout.layout_item_rv_list
}