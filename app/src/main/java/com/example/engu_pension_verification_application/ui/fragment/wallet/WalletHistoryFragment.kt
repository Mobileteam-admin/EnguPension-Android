package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.adapter.WalletHistoryAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_wallet_history.*


class WalletHistoryFragment : BaseFragment() {
    private lateinit var walletHistory_lm: LinearLayoutManager
    lateinit var walletHistoryAdapter: WalletHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onAdapterset()
        onClicked()
    }

    private fun onAdapterset() {
        walletHistoryAdapter = WalletHistoryAdapter() {}
        walletHistory_lm = LinearLayoutManager(requireContext())
        rv_wallethistory.layoutManager = walletHistory_lm
        rv_wallethistory.adapter = walletHistoryAdapter
    }

    private fun onClicked() {
        img_wallethistory_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}