package com.enugu.pension.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.enugu.pension.databinding.ItemWalletHistoryBinding
import com.enugu.pension.data.remote.dto.response.TransactionHistoryResponse
import com.enugu.pension.common.util.CalendarUtils
import com.enugu.pension.common.util.capitalizeFirstLetter


class WalletHistoryAdapter :
    PagingDataAdapter<TransactionHistoryResponse.Detail.Data.Transaction, WalletHistoryAdapter.TransactionViewHolder>(
        TransactionDiffCallback()
    ) {

    class TransactionViewHolder(private val binding: ItemWalletHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: TransactionHistoryResponse.Detail.Data.Transaction) {
            binding.apply {
                tvTransactionId.text = transaction.stripeTransactionId
                tvAmount.text = "${transaction.amount}"
                tvTransactionType.text = transaction.type.capitalizeFirstLetter()
                tvDescription.text = "Wallet Top up"
                tvDate.text =
                    CalendarUtils.getFormattedString(
                        CalendarUtils.DATE_TIME_FORMAT_1,
                        CalendarUtils.DATE_FORMAT_3,
                        transaction.transactionDate
                    )
            }
        }
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        transaction?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding =
            ItemWalletHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    class TransactionDiffCallback :
        DiffUtil.ItemCallback<TransactionHistoryResponse.Detail.Data.Transaction>() {
        override fun areItemsTheSame(
            oldItem: TransactionHistoryResponse.Detail.Data.Transaction,
            newItem: TransactionHistoryResponse.Detail.Data.Transaction
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: TransactionHistoryResponse.Detail.Data.Transaction,
            newItem: TransactionHistoryResponse.Detail.Data.Transaction
        ): Boolean {
            return oldItem == newItem && oldItem.id == newItem.id && oldItem.amount == newItem.amount && oldItem.transactionDate == newItem.transactionDate && oldItem.description == newItem.description && oldItem.stripeTransactionId == newItem.stripeTransactionId
        }
    }
}