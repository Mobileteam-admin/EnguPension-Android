package com.enugu.pension.ui.fragment.wallet

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.FragmentWalletHistoryBinding
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.ui.adapter.TransactionLoadStateAdapter
import com.enugu.pension.ui.adapter.WalletHistoryAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.common.util.CalendarUtils
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import com.enugu.pension.viewmodel.WalletHistoryViewModel
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

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.fileLocationUri = uri
                    showLoader()
                    viewModel.fetchStatementPdfLinkN()
                }
            }
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
        initRvWalletHistory()
        binding.tvEmptyMessage.isGone = true
        binding.imgWallethistoryBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initRvWalletHistory() {
        binding.rvWalletHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWalletHistory.adapter = adapter.withLoadStateFooter(
            footer = TransactionLoadStateAdapter { adapter.retry() }
        )
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
                val isEmpty = adapter.itemCount == 0 &&
                        loadStates.refresh is LoadState.NotLoading &&
                        loadStates.append.endOfPaginationReached
                binding.tvEmptyMessage.isVisible = isEmpty
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
            if (response?.detail?.status == AppConstants.SUCCESS) {
                populateViews()
            }
        }
        viewModel.linkApiResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.detail.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    viewModel.downloadStatementPdf(response.detail.fileUrl, requireContext().contentResolver)
                } else {
                    if (response.detail.tokenStatus == AppConstants.EXPIRED) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.downloadStatementPdf(response.detail.fileUrl, requireContext().contentResolver)
                            }
                        }
                    } else {
                        dismissLoader()
                        response.detail.message?.let { showToast(it) }
                    }
                }
            }
        }
        viewModel.statementDownloadApiResult.observe(viewLifecycleOwner) { message ->
            dismissLoader()
            showToast(message)
        }
    }

    private fun populateViews() {
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            binding.tvWalletAmount.text = it.getWalletBalanceAmount()
            binding.ivNaira.isGone = true
        }
        binding.clDownload.setOnClickListener {
            if (confirmInternet()) {
                val fileName = "Account Statement ${CalendarUtils.getFormattedToday()}.pdf"
                openPathPicker(fileName)
            }
        }
    }

    private fun openPathPicker(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createFileLauncher.launch(intent)
    }
}