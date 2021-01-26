package com.android.collegeadminapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRVAdapter<T : Any, VB : ViewDataBinding> :
    RecyclerView.Adapter<BaseRVAdapter.Companion.BaseViewHolder<VB>>() {

    var items = mutableListOf<T>()

    fun addItems(items: List<T>) {
        this.items = items as MutableList<T>
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    var listener: ((view: View, item: T, position: Int) -> Unit)? = null

    abstract fun layout(): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BaseViewHolder<VB>(
        DataBindingUtil.inflate(LayoutInflater.from(parent.context), layout(), parent, false)
    )

    companion object {
        class BaseViewHolder<VB : ViewDataBinding>(val binding: VB) :
            RecyclerView.ViewHolder(binding.root) {
        }
    }
}