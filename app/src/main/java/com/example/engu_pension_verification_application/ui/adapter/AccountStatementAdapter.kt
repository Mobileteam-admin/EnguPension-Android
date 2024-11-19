package com.example.engu_pension_verification_application.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.engu_pension_verification_application.R

class AccountStatementAdapter(val onItemClicked: () -> Unit) :
    RecyclerView.Adapter<AccountStatementAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.card_wallet_history,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 6
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}