package com.enugu.pension.ui.fragment.wallet

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentWalletBinding
import com.enugu.pension.model.request.TopUpRequest
import com.enugu.pension.model.response.ListBanksItem
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.activity.StripeWebViewActivity
import com.enugu.pension.ui.adapter.BankAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.util.SharedPref
import com.enugu.pension.util.isValidNumber
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import com.enugu.pension.viewmodel.WalletViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WalletFragment : BaseFragment() {
    companion object {
        const val BANK_ITEM_SELECT_ID = -1
        const val MIN_TOP_UP_AMOUNT = 1400f
    }
    private lateinit var binding:FragmentWalletBinding
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var viewModel: WalletViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var stripeActivityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVars()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
        initBanks()
    }

    private fun initBanks() {
        viewModel.bankItems.clear()
        viewModel.bankItems.add(
            ListBanksItem(
                name = " - Select Bank - ",
                id = BANK_ITEM_SELECT_ID,
            )
        )
        dashboardViewModel.bankAccounts?.forEach {
                viewModel.bankItems.add(ListBanksItem(
                    code = it.bankCode,
                    name = it.bankName,
                    id = it.bankId
                ))
        }
        binding.spWalletBank.adapter = BankAdapter(context, viewModel.bankItems)
        viewModel.selectedBankItemPosition?.let {
            binding.spWalletBank.setSelection(it)

        }
    }

    private fun initVars() {
        stripeActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val sessionId = result.data?.getStringExtra(AppConstants.SESSION_ID)!!
                    viewModel.fetchPaymentStatus(sessionId)
                    showLoader()
                }
            }
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(WalletViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.tvAmountError.isInvisible = true
        binding.imgWalletBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.llWalletBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvWalletHistory.setOnClickListener {
            if (confirmInternet()) navigate(R.id.action_wallet_to_wallet_history)
        }
        binding.llWalletTopup.setOnClickListener {
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                if (validateInputs()) {
                    val topUpRequest = TopUpRequest(
                        userId = SharedPref.user_id?.toInt()!!,
                        bankId = viewModel.bankItems[binding.spWalletBank.selectedItemPosition]?.id!!,
                        amount = binding.etTopUpWalletAmount.text.toString().toFloat(),
                        currency = AppConstants.DEFAULT_CURRENCY_CODE,
                    )
                    viewModel.fetchTopUp(topUpRequest)
                    showLoader()
                }
            } else showToast(R.string.no_internet_error)
        }
        binding.spWalletBank.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.selectedBankItemPosition = position
                refreshBankImage(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.etTopUpWalletAmount.addTextChangedListener {
            val amount = it.toString().toFloatOrNull() ?: 0f
            binding.tvAmountError.isInvisible = it.isNullOrEmpty() || amount >= MIN_TOP_UP_AMOUNT
        }
    }

    private fun observeLiveData() {
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response?.detail?.status == AppConstants.SUCCESS) {
                populateViews()
            }
        }

        viewModel.topUpApiResult.observe(viewLifecycleOwner) { pair ->
            dismissLoader()
            if (pair != null) {
                val request = pair.first
                val response = pair.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    val intent = Intent(requireActivity(), StripeWebViewActivity::class.java)
                    intent.putExtra(StripeWebViewActivity.EXTRA_URL, response.detail.checkoutUrl)
                    stripeActivityResultLauncher.launch(intent)
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        showLoader()
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchTopUp(request)
                            }
                        }
                    } else {
                        showToast(response.detail?.message ?: getString(R.string.common_error_msg_2))
                    }
                }
                viewModel.resetTopUpApiResult()
            }
        }
        viewModel.paymentResult.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                val sessionId = pair.first
                val response = pair.second
                dismissLoader()
                if (response.detail?.status == AppConstants.SUCCESS) {
                    response.detail.message?.let { showToast(it) }
                    showLoader()
                    dashboardViewModel.fetchDashboardDetails()
                    findNavController().navigateUp()
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        showLoader()
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchPaymentStatus(sessionId)
                            }
                        }
                    } else {
                        showToast(response.detail?.message ?: getString(R.string.common_error_msg))
                    }
                }
                viewModel.resetPaymentResult()
            }
        }
    }

    private fun refreshBankImage(position: Int) {
        binding.imgActivebank.setImageResource(R.drawable.ic_bank_green)
        viewModel.bankItems[position]?.let {
            Glide.with(requireContext())
                .load(it.logo)
                .placeholder(R.drawable.ic_bank_green)
                .into(binding.imgActivebank)
        }
    }

    private fun validateInputs(): Boolean {
        var errorResId: Int? = null
        val amountText = binding.etTopUpWalletAmount.text.toString()
        if (!amountText.isValidNumber()) {
            errorResId = R.string.invalid_amount_msg
        } else if ((amountText.toFloatOrNull() ?: 0f) < MIN_TOP_UP_AMOUNT) {
            errorResId = R.string.top_up_minimum_amount_error
        } else if (binding.spWalletBank.selectedItemPosition !in viewModel.bankItems.indices
            || viewModel.bankItems[binding.spWalletBank.selectedItemPosition]?.id == BANK_ITEM_SELECT_ID
        ) {
            errorResId = R.string.no_bank_selected_msg
        }
        if (errorResId != null) {
            showToast(errorResId)
            return false
        }
        return true
    }
    private fun populateViews() {
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            val walletText = "${it.walletBalanceCurrency} ${it.walletBalanceAmount.toString()}"
            binding.tvWalletAmount.text = walletText
            binding.ivNaira.isGone = true
        }
    }
}