package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentWalletBinding
import com.example.engu_pension_verification_application.model.input.TopUpRequest
import com.example.engu_pension_verification_application.model.response.ListBanksItem
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.WebView.StripeWebViewActivity
import com.example.engu_pension_verification_application.ui.adapter.BankAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils.Companion.isValidNumber
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.DashboardViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.example.engu_pension_verification_application.viewmodel.WalletViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WalletFragment : BaseFragment() {
    companion object {
        const val BANK_ITEM_SELECT_ID = -1
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
        initApiCall()
    }

    private fun initApiCall() {
        if (viewModel.bankListApiResult.value == null) {
            showLoader()
            viewModel.fetchBankList()
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
        binding.imgWalletBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.llWalletBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.txtWallethistory.setOnClickListener {
            navigate(R.id.action_wallet_to_wallet_history)
        }
        binding.llWalletTopup.setOnClickListener {
            if (validateInputs()) {
                val topUpRequest = TopUpRequest(
                    userId = SharedPref.user_id?.toInt()!!,
                    bankId = binding.spWalletBank.selectedItemPosition,
                    amount = binding.etTopUpWalletAmount.text.toString().toFloat(),
                    currency = AppConstants.DEFAULT_CURRENCY_CODE,
                )
                viewModel.fetchTopUp(topUpRequest)
                showLoader()
            }
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
    }

    private fun observeLiveData() {
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                populateViews()
            }
        }
        viewModel.bankListApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                viewModel.bankItems.clear()
                viewModel.bankItems.add(
                    ListBanksItem(
                        name = " - Select Bank - ",
                        id = BANK_ITEM_SELECT_ID,
                    )
                )
                response.detail.banks?.let { viewModel.bankItems.addAll(it) }
                binding.spWalletBank.adapter = BankAdapter(context, viewModel.bankItems)
                viewModel.selectedBankItemPosition?.let {
                    binding.spWalletBank.setSelection(it)
                }
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchBankList()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
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
        val amount = binding.etTopUpWalletAmount.text.toString()
        if (!amount.isValidNumber()) {
            errorResId = R.string.invalid_amount_msg
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