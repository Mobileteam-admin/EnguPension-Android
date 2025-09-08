package com.enugu.pension.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.databinding.ItemProfileBinding
import com.enugu.pension.ui.model.ProfileInfo

class ProfileAdapter(private val profileInfo: ProfileInfo) :
    RecyclerView.Adapter<ProfileAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemProfileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = profileInfo.items[position]
        holder.binding.tilInfo.hint = item.key
        holder.binding.etInfo.setText(item.value)
        holder.binding.etInfo.isEnabled = item.isEditable

//        holder.binding.tvKey.text = item.key
//        holder.binding.tvValue.text = item.value
//        holder.binding.etValue.setText(item.value)
//        holder.binding.tvValue.isGone = item.isEditable
//        holder.binding.etValue.isGone = !item.isEditable
    }

    override fun getItemCount(): Int = profileInfo.items.size

}
