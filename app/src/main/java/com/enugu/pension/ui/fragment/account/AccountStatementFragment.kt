package com.enugu.pension.ui.fragment.account

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentAccountStatementBinding
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.activity.PermissionRequestActivity
import com.enugu.pension.ui.adapter.WalletHistoryAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.CalendarUtils
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.viewmodel.AccountStatementViewModel
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountStatementFragment : BaseFragment() {
    private lateinit var binding: FragmentAccountStatementBinding
    private lateinit var viewModel: AccountStatementViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val adapter = WalletHistoryAdapter()
    private var retryCount = 0

    companion object {
        private const val MAX_RETRY = 3
    }
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                showLoader()
                viewModel.fetchStatementLink()
            } else {
                showToast(R.string.no_internet_error)
            }
        }
        else showToast("Write permission denied. Cannot download Account statement.")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountStatementBinding.inflate(inflater, container, false)
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
        ).get(AccountStatementViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.tvEmptyMessage.isGone = true
        binding.clDownload.isGone = true
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rvWalletHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWalletHistory.adapter = adapter
        binding.clDownload.setOnClickListener {
            val intent = Intent(requireActivity(), PermissionRequestActivity::class.java)
            intent.putExtra(PermissionRequestActivity.EXTRA_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissionResultLauncher.launch(intent)
        }
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
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
                binding.clDownload.isInvisible = isEmpty
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
        viewModel.statementApiResult.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            if (response.downloadLink == null) {
                showToast(R.string.Statement_download_error_msg)
            } else {
                val downloadManager =
                    context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val url = "${AppConstants.BASE_URL}/${response.downloadLink}"
                val fileName = getString(R.string.statement_file_name, CalendarUtils.getFormattedNow())
                val downloadDescription = getString(R.string.downloading_statement)
                viewModel.downloadPdf(downloadManager, url, fileName, downloadDescription)
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