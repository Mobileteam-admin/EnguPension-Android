package com.enugu.pension.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.databinding.ItemProfileBinding
import com.enugu.pension.ui.model.KeyValue

class KinProfileAdapter(private val items: List<KeyValue>) :
    RecyclerView.Adapter<KinProfileAdapter.ItemViewHolder>() {

//    class ItemViewHolder(val binding: ItemKinProfileBinding) : RecyclerView.ViewHolder(binding.root)
    class ItemViewHolder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
//        val binding = ItemKinProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val binding = ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
//        holder.binding.tvKey.text = item.key
//        holder.binding.tvValue.text = item.value
        holder.binding.tilInfo.hint = item.key
        holder.binding.etInfo.setText(item.value)
    }

    override fun getItemCount(): Int = items.size

}
