package com.enugu.pension.ui.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.R
import com.enugu.pension.databinding.ItemGratuityBinding
import com.enugu.pension.databinding.ItemProfileBinding
import com.enugu.pension.databinding.ItemStatementBinding
import com.enugu.pension.model.ui.GratuityItem
import com.enugu.pension.model.ui.ProfileInfo
import com.enugu.pension.model.ui.StatementItem
import com.enugu.pension.util.CalendarUtils

class StatementAdapter(private val resources: Resources) :
    RecyclerView.Adapter<StatementAdapter.ItemViewHolder>() {
    private val items = mutableListOf<StatementItem>()

    class ItemViewHolder(val binding: ItemStatementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemStatementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvAmount.text = item.amount.toString()
        holder.binding.tvDateLabel.isGone = true
        holder.binding.tvDateLabel.text =
            resources.getString(if (item.isPension) R.string.salary_disbursed_on else R.string.pension_withdrawn_on)
        holder.binding.tvDate.text = CalendarUtils.getFormattedString(
            CalendarUtils.DATE_TIME_FORMAT_1,
            CalendarUtils.DATE_FORMAT_4,
            item.date
        )
        if (item.description.isNullOrEmpty()) {
            holder.binding.tvDescription.isGone = true
        } else {
            holder.binding.tvDescription.text = item.description
        }
    }

    override fun getItemCount(): Int = items.size

    fun refreshList(newList: List<StatementItem>) {
        this.items.clear()
        this.items.addAll(newList)
        notifyDataSetChanged()
    }

}
