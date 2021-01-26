package com.android.collegeadminapp.ui.faculty

import android.content.ContentValues.TAG
import android.util.Log
import com.android.collegeadminapp.R
import com.android.collegeadminapp.adapter.BaseRVAdapter
import com.android.collegeadminapp.databinding.LayoutItemFacultyBinding
import com.squareup.picasso.Picasso

class FacultyAdapter : BaseRVAdapter<Faculty,LayoutItemFacultyBinding>() {
    override fun layout() = R.layout.layout_item_faculty
    override fun onBindViewHolder(
        holder: Companion.BaseViewHolder<LayoutItemFacultyBinding>,
        position: Int
    ) {
        val item = items[position]
        holder.binding.faculty = item    //items is list in base rv
        Picasso.get().load(item.image).into(holder.binding.facultyImage)
        holder.binding.updateInfoFaculty.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: no")
        }
    }


}