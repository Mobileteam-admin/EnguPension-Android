package com.enugu.pension.ui.fragment.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.enugu.pension.R
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.FragmentVerificationHistoryBinding
import com.enugu.pension.ui.model.VerificationHistoryItem
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.ui.adapter.VerificationHistoryAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import com.enugu.pension.viewmodel.VerificationHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VerificationHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentVerificationHistoryBinding
    private lateinit var viewModel: VerificationHistoryViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var rvAdapter: VerificationHistoryAdapter

    companion object {
        private const val MAX_RETRY = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerificationHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
        if (confirmInternet()) {
            binding.progressBar.isVisible = true
            viewModel.fetchVerificationHistory()
        }

    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(VerificationHistoryViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        initRvHistory()
        binding.tvEmptyMessage.isGone = true
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initRvHistory() {
        rvAdapter = VerificationHistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            binding.rvHistory.adapter = rvAdapter
        }
    }

    private fun observeLiveData() {
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response?.detail?.status == AppConstants.SUCCESS) {
                populateHeaderViews()
            }
        }
        viewModel.verificationHistoryApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                populateRvHistory()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchVerificationHistory()
                        }
                    }
                } else {
                    binding.progressBar.isGone = true
                    binding.tvEmptyMessage.isVisible = true
                    binding.tvEmptyMessage.text = response.detail?.message?: getString(R.string.common_error_msg)
                }
            }
        }
    }

    private fun populateHeaderViews() {
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            binding.tvWalletAmount.text = it.getWalletBalanceAmount()
            binding.ivNaira.isGone = true
        }
    }
    private fun populateRvHistory() {
        val rvItems = mutableListOf<VerificationHistoryItem>()
        viewModel.verificationHistoryApiResult.value?.detail?.verificationHistory?.forEach {
            rvItems.add(
                VerificationHistoryItem(
                    id = it.bookingId,
                    time = it.verifiedAt ?: "",
                    status = it.verificationStatus ?: "",
                )
            )
        }
        rvAdapter.setList(rvItems)
        binding.progressBar.isGone = true
        binding.tvEmptyMessage.isVisible = rvItems.isEmpty()
    }
}