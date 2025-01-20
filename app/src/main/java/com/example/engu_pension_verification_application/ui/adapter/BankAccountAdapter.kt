package com.example.engu_pension_verification_application.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.databinding.ItemBankAccountBinding
import com.example.engu_pension_verification_application.network.BankAccountItem


class BankAccountAdapter(private val onItemClick: ((BankAccountItem) -> Unit)? = null) :
    RecyclerView.Adapter<BankAccountAdapter.ViewHolder>() {

    private val items = mutableListOf<BankAccountItem>()

    class ViewHolder(private val binding: ItemBankAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: BankAccountItem,
            onItemClick: ((BankAccountItem) -> Unit)? = null
        ) {
            binding.root.setOnClickListener { onItemClick?.invoke(item) }
            if (item.logoUrl != null) {
                Glide.with(binding.root.context).load(item.logoUrl)
                    .into(binding.ivBankIcon)
            }
            binding.tvBankName.text = item.bankName
            binding.tvBankType.text = item.accountType
            binding.tvPrimary.isVisible = item.isPrimary
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBankAccountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<BankAccountItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }
}
