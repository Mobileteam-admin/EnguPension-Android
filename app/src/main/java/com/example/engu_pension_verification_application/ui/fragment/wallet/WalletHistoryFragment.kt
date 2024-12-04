package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.FragmentWalletHistoryBinding
import com.example.engu_pension_verification_application.ui.adapter.WalletHistoryAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment

class WalletHistoryFragment : BaseFragment() {
    private lateinit var binding:FragmentWalletHistoryBinding
    private lateinit var walletHistory_lm: LinearLayoutManager
    lateinit var walletHistoryAdapter: WalletHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalletHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onAdapterset()
        onClicked()
    }

    private fun onAdapterset() {
        walletHistoryAdapter = WalletHistoryAdapter() {}
        walletHistory_lm = LinearLayoutManager(requireContext())
        binding.rvWallethistory.layoutManager = walletHistory_lm
        binding.rvWallethistory.adapter = walletHistoryAdapter
    }

    private fun onClicked() {
        binding.imgWallethistoryBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}