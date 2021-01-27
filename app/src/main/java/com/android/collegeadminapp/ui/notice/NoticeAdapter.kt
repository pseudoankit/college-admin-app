package com.android.collegeadminapp.ui.notice

import com.android.collegeadminapp.R
import com.android.collegeadminapp.adapter.BaseRVAdapter
import com.android.collegeadminapp.databinding.LayoutItemNewsfeedBinding
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.lang.Exception

class NoticeAdapter : BaseRVAdapter<Notice, LayoutItemNewsfeedBinding>() {
    override fun layout() = R.layout.layout_item_newsfeed
    override fun onBindViewHolder(
        holder: Companion.BaseViewHolder<LayoutItemNewsfeedBinding>,
        position: Int
    ) {
        val notice = list[position]
        holder.binding.notice = notice
        try {
            Picasso.get().load(notice.image).into(holder.binding.noticeImage)
        } catch (e: Exception){
            e.printStackTrace()
        }
        holder.binding.deleteNotice.setOnClickListener {
            listener?.invoke(it,notice,position)

        }
    }


}