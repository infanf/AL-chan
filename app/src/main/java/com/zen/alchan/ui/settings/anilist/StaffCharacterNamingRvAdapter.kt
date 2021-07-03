package com.zen.alchan.ui.settings.anilist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zen.alchan.R
import com.zen.alchan.databinding.ListTextBinding
import com.zen.alchan.helper.extensions.clicks
import com.zen.alchan.helper.extensions.convertFromSnakeCase
import com.zen.alchan.ui.base.BaseRecyclerViewAdapter
import type.UserStaffNameLanguage

class StaffCharacterNamingRvAdapter(
    private val context: Context,
    list: List<UserStaffNameLanguage>,
    private val listener: StaffCharacterNamingListener
) : BaseRecyclerViewAdapter<UserStaffNameLanguage, ListTextBinding>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(private val binding: ListTextBinding) : ViewHolder(binding) {
        override fun bind(item: UserStaffNameLanguage, index: Int) {
            binding.itemText.text = when (item) {
                UserStaffNameLanguage.ROMAJI_WESTERN -> context.getString(R.string.use_staff_character_romaji_western_name_format)
                UserStaffNameLanguage.ROMAJI -> context.getString(R.string.use_staff_character_romaji_name_format)
                UserStaffNameLanguage.NATIVE -> context.getString(R.string.use_staff_character_native_name_format)
                else -> item.name.convertFromSnakeCase()
            }
            binding.itemLayout.clicks { listener.getSelectedNaming(item) }
        }
    }

    interface StaffCharacterNamingListener {
        fun getSelectedNaming(userStaffNameLanguage: UserStaffNameLanguage)
    }
}