package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.engu_pension_verification_application.R

class WalletHistoryAdapter(val onItemClicked: () -> Unit) :
    RecyclerView.Adapter<WalletHistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WalletHistoryAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.card_wallet_history,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WalletHistoryAdapter.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 12
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}