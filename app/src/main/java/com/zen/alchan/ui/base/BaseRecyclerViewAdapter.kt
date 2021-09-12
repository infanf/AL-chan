package com.zen.alchan.ui.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.zen.alchan.data.entitiy.AppSetting

abstract class BaseRecyclerViewAdapter<T, VB: ViewBinding>(
    protected var list: List<T>,
) : RecyclerView.Adapter<BaseRecyclerViewAdapter<T, VB>.ViewHolder>() {

    protected var appSetting = AppSetting()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(list: List<T>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun applyAppSetting(appSetting: AppSetting) {
        this.appSetting = appSetting
    }

    abstract inner class ViewHolder(private val binding: VB) : RecyclerView.ViewHolder(binding.root), ViewHolderContract<T>
}