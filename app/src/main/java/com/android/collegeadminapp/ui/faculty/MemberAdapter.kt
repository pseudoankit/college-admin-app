package com.android.collegeadminapp.ui.faculty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.collegeadminapp.R

class MemberAdapter: RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    lateinit var mList : MutableList<Faculty>
    fun addMember(list: ArrayList<Faculty>){
        this.mList = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.namefac)
        var email = itemView.findViewById<TextView>(R.id.emailfac)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_faculty,parent,false)
        return MemberAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = mList[position].name
        holder.email.text = mList[position].email
    }

    override fun getItemCount() = mList.size
}