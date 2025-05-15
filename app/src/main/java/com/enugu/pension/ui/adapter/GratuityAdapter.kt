package com.enugu.pension.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.databinding.ItemGratuityBinding
import com.enugu.pension.databinding.ItemProfileBinding
import com.enugu.pension.model.ui.GratuityItem
import com.enugu.pension.model.ui.ProfileInfo

class GratuityAdapter(private val items: List<GratuityItem>) :
    RecyclerView.Adapter<GratuityAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemGratuityBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemGratuityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvAmount.text = (item.amount?:"").toString()
        holder.binding.tvDate.text = item.paymentDate
        holder.binding.tvDescription.text = item.description
        holder.binding.tvTransactionType.text = item.paymentStatus
        holder.binding.tvTransactionId.isGone = true
    }

    override fun getItemCount(): Int = items.size

}
