package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.viewmodel.WalletFragmentViewModel
import kotlinx.android.synthetic.main.fragment_wallet.*


class WalletFragment : BaseFragment() {
    private lateinit var walletFragmentViewModel: WalletFragmentViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        walletFragmentViewModel =  ViewModelProvider(this).get(WalletFragmentViewModel::class.java)

        walletFragmentViewModel.getBankList()

        onClicked()
    }

    private fun onClicked() {
        img_wallet_back.setOnClickListener {
            findNavController().navigateUp()
        }
        ll_wallet_back.setOnClickListener {
            findNavController().navigateUp()
        }

        txt_wallethistory.setOnClickListener {
            navigate(R.id.action_wallet_to_wallet_history)
        }
    }

}