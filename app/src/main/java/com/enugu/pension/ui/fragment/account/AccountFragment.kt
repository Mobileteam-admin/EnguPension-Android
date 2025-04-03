package com.enugu.pension.ui.fragment.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentAccountBinding
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.viewmodel.AccountViewModel
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2


class AccountFragment : BaseFragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var viewModel: AccountViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(AccountViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.imgAccountBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.txtStatement.setOnClickListener {
            if (confirmInternet()) navigate(R.id.action_account_to_accountstatement)
        }
        binding.txtKinprofile.setOnClickListener {
            if (confirmInternet()) navigate(R.id.action_account_to_kinprofile)
        }
    }

    private fun observeLiveData() {
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response?.detail?.status == AppConstants.SUCCESS) {
                populateViews()
            }
        }
    }

    private fun populateViews() {
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            val walletText = "${it.walletBalanceCurrency} ${it.walletBalanceAmount.toString()}"
            binding.tvWalletAmount.text = walletText
            binding.ivNaira.isGone = true
        }
    }

}