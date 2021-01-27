package com.android.collegeadminapp.ui.faculty

import android.view.View
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
        val faculty = list[position]
        holder.binding.faculty = faculty    //items is list in base rv
        Picasso.get().load(faculty.image).into(holder.binding.facultyImage)
        holder.binding.updateInfoFaculty.setOnClickListener {
            listener?.invoke(it,faculty,position)
        }
    }

}