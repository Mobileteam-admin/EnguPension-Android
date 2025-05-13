package com.enugu.pension.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.databinding.ItemKinProfileBinding
import com.enugu.pension.databinding.ItemProfileBinding
import com.enugu.pension.model.misc.KeyValue
import com.enugu.pension.model.ui.ProfileInfo

class KinProfileAdapter(private val items: List<KeyValue>) :
    RecyclerView.Adapter<KinProfileAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemKinProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemKinProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvKey.text = item.key
        holder.binding.tvValue.text = item.value
    }

    override fun getItemCount(): Int = items.size

}
