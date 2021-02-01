package com.android.collegeadminapp.ui.faculty

import android.content.Context
import android.content.Intent
import com.android.collegeadminapp.R
import com.android.collegeadminapp.adapter.BaseRVAdapter
import com.android.collegeadminapp.databinding.LayoutItemFacultyBinding
import com.squareup.picasso.Picasso

class FacultyRvAdapter(private val context: Context) : BaseRVAdapter<Faculty, LayoutItemFacultyBinding>() {
    override fun onBindViewHolder(
        holder: Companion.BaseViewHolder<LayoutItemFacultyBinding>,
        position: Int
    ) {
        val faculty = list[position]
        holder.binding.faculty = faculty    //items is list in base rv
        Picasso.get().load(faculty.image).into(holder.binding.facultyImage)
        holder.binding.updateInfoFaculty.setOnClickListener {
            onUpdateClicked(faculty)
        }
    }

    private fun onUpdateClicked(faculty: Faculty) {
        Intent(context, AddUpdateFacultyActivity::class.java).apply {
            this.putExtra(AddUpdateFacultyActivity.IS_ADD, false)
            this.putExtra(AddUpdateFacultyActivity.UPDATE_OBJ, faculty)
            context.startActivity(this)
        }
    }

    override fun layout() = R.layout.layout_item_faculty

}