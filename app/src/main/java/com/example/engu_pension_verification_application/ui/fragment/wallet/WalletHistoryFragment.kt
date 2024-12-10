package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentWalletHistoryBinding
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.adapter.WalletHistoryAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.viewmodel.DashboardViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.example.engu_pension_verification_application.viewmodel.WalletHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentWalletHistoryBinding
    private lateinit var viewModel: WalletHistoryViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val adapter = WalletHistoryAdapter()
    private var retryCount = 0

    companion object {
        private const val MAX_RETRY = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalletHistoryBinding.inflate(inflater, container, false)
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
        ).get(WalletHistoryViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.imgWallethistoryBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rvWalletHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWalletHistory.adapter = adapter

    }

    private fun observeLiveData() {
        lifecycleScope.launch {
            viewModel.transactionFlow.collectLatest { pagingData ->
                retryCount = 0
                adapter.submitData(pagingData)
            }
        }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                val errorState = loadStates.refresh as? LoadState.Error
                    ?: loadStates.append as? LoadState.Error
                    ?: loadStates.prepend as? LoadState.Error
                if (errorState?.error?.message == AppConstants.TOKEN_EXPIRED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            withContext(Dispatchers.Main) {
                                if (retryCount < MAX_RETRY) {
                                    retryCount++
                                    adapter.retry()
                                } else {
                                    showToast(R.string.common_error_msg_2)
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    }
                } else {
                    if (errorState?.error != null) showToast(R.string.common_error_msg_2)
                    binding.progressBar.visibility =
                        if (loadStates.refresh is LoadState.Loading) View.VISIBLE else View.GONE
                }
            }
        }
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
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