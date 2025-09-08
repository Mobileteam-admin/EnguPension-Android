package com.enugu.pension.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.databinding.ItemVerificationHistoryBinding
import com.enugu.pension.ui.model.VerificationHistoryItem

class VerificationHistoryAdapter :
    RecyclerView.Adapter<VerificationHistoryAdapter.ItemViewHolder>() {
    private val items = mutableListOf<VerificationHistoryItem>()

    class ItemViewHolder(val binding: ItemVerificationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemVerificationHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTime.text = item.time
        holder.binding.tvStatus.text = item.status
    }

    override fun getItemCount(): Int = items.size

    fun setList(items: MutableList<VerificationHistoryItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

}
